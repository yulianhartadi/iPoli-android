package mypoli.android.quest.timer

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.common.datetime.*
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.Quest
import mypoli.android.quest.TimeRange
import mypoli.android.quest.usecase.ListenForQuestChangeUseCase
import mypoli.android.quest.timer.TimerViewState.StateType.*
import mypoli.android.quest.timer.usecase.*
import mypoli.android.quest.timer.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit
import mypoli.android.quest.timer.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationSplit
import mypoli.android.quest.timer.view.formatter.TimerFormatter
import org.threeten.bp.Instant
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 6.01.18.
 */
class TimerPresenter(
    private val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase,
    private val listenForQuestChangeUseCase: ListenForQuestChangeUseCase,
    private val addTimerToQuestUseCase: AddTimerToQuestUseCase,
    private val completeTimeRangeUseCase: CompleteTimeRangeUseCase,
    private val cancelTimerUseCase: CancelTimerUseCase,
    private val addPomodoroUseCase: AddPomodoroUseCase,
    private val removePomodoroUseCase: RemovePomodoroUseCase,
    private val coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<TimerViewState>, TimerViewState, TimerIntent>(
    TimerViewState(LOADING),
    coroutineContext
) {

    override fun reduceState(intent: TimerIntent, state: TimerViewState) =

        when (intent) {
            is TimerIntent.LoadData -> {

                launch(coroutineContext) {
                    listenForQuestChangeUseCase.listen(
                        ListenForQuestChangeUseCase.Params(intent.questId)
                    ).consumeEach {
                        sendChannel.send(TimerIntent.QuestChanged(it))
                    }
                }
                state.copy(
                    type = LOADING
                )
            }

            is TimerIntent.QuestChanged -> {

                if (intent.quest.isCompleted) {
                    state.copy(
                        type = QUEST_COMPLETED
                    )
                } else {
                    createQuestChangedState(state.copy(quest = intent.quest))
                }
            }

            TimerIntent.Start -> {
                val (quest, stateType) = if (state.quest!!.hasTimer) {
                    val quest = completeTimeRangeUseCase.execute(
                        CompleteTimeRangeUseCase.Params(
                            questId = state.quest.id
                        )
                    )
                    Pair(quest, TIMER_STARTED)
                } else {
                    val result = addTimerToQuestUseCase.execute(
                        AddTimerToQuestUseCase.Params(
                            questId = state.quest.id,
                            isPomodoro = state.timerType == TimerViewState.TimerType.POMODORO
                        )
                    )

                    val type = if (result.otherTimerStopped) TIMER_REPLACED else TIMER_STARTED
                    Pair(result.quest, type)
                }

                val currentProgressIndicator =
                    if (state.timerType == TimerViewState.TimerType.POMODORO)
                        findCurrentProgressIndicator(quest.timeRanges)
                    else state.currentProgressIndicator

                state.copy(
                    type = stateType,
                    timerProgress = 0,
                    maxTimerProgress = state.remainingTime!!.asSeconds.longValue.toInt(),
                    currentProgressIndicator = currentProgressIndicator
                )
            }

            TimerIntent.Stop -> {
                cancelTimerUseCase.execute(CancelTimerUseCase.Params(state.quest!!.id))
                state.copy(
                    type = TIMER_STOPPED
                )
            }

            TimerIntent.Tick -> {
                val remainingTime = state.remainingTime!! - 1.seconds
                state.copy(
                    type = RUNNING,
                    timerProgress = state.timerProgress + 1,
                    timerLabel = formatDuration(remainingTime),
                    remainingTime = remainingTime,
                    showCompletePomodoroButton = state.timerType == TimerViewState.TimerType.POMODORO && remainingTime < 0.seconds
                )
            }

            TimerIntent.CompletePomodoro -> {
                completeTimeRangeUseCase.execute(
                    CompleteTimeRangeUseCase.Params(
                        questId = state.quest!!.id
                    )
                )
                state.copy(
                    type = TIMER_STOPPED
                )
            }

            TimerIntent.ShowPomodoroTimer -> {
                createStateForInitialPomodoroTimer(state, state.quest!!)
            }

            TimerIntent.ShowCountDownTimer -> {
                createStateForInitialCountDownTimer(state, state.quest!!)
            }

            TimerIntent.CompleteQuest -> {
                completeTimeRangeUseCase.execute(CompleteTimeRangeUseCase.Params(state.quest!!.id))
                state.copy(
                    type = TIMER_STOPPED
                )
            }

            TimerIntent.AddPomodoro -> {
                addPomodoroUseCase.execute(AddPomodoroUseCase.Params(state.quest!!.id))
                state.copy(
                    type = POMODORO_ADDED
                )
            }

            TimerIntent.RemovePomodoro -> {
                removePomodoroUseCase.execute(RemovePomodoroUseCase.Params(state.quest!!.id))
                state.copy(
                    type = POMODORO_REMOVED
                )
            }
        }

    private fun formatDuration(duration: Duration<Second>): String {
        return if (duration >= 0.seconds) {
            TimerFormatter.format(duration.millisValue)
        } else {
            "+" + TimerFormatter.format(Math.abs(duration.millisValue))
        }
    }

    private fun createQuestChangedState(
        state: TimerViewState
    ): TimerViewState {
        val quest = state.quest
        if (quest!!.hasCountDownTimer) {
            return createStateForRunningCountdownTimer(quest, state)
        }

        if (quest.hasPomodoroTimer) {
            return createStateForRunningPomodoroTimer(quest, state)
        }

        return if (quest.duration < MIN_INITIAL_POMODORO_TIMER_DURATION) {
            createStateForInitialCountDownTimer(state, quest)
        } else {
            createStateForInitialPomodoroTimer(state, quest)
        }
    }

    private fun createStateForInitialPomodoroTimer(
        state: TimerViewState,
        quest: Quest
    ): TimerViewState {
        val result = splitDurationForPomodoroTimerUseCase.execute(
            SplitDurationForPomodoroTimerUseCase.Params(quest)
        )
        val timeRanges = if (result == DurationNotSplit) {
            quest.timeRanges
        } else {
            (result as DurationSplit).timeRanges
        }

        return state.copy(
            type = SHOW_POMODORO,
            questName = quest.name,
            timerType = TimerViewState.TimerType.POMODORO,
            showTimerTypeSwitch = true,
            pomodoroProgress = timeRanges.map { createPomodoroProgress(it) },
            timerLabel = formatDuration(Constants.DEFAULT_POMODORO_WORK_DURATION.minutes.asSeconds),
            remainingTime = Constants.DEFAULT_POMODORO_WORK_DURATION.minutes.asSeconds,
            currentProgressIndicator = 0,
            timerProgress = 0,
            maxTimerProgress = Constants.DEFAULT_POMODORO_WORK_DURATION.minutes.asSeconds.intValue
        )
    }

    private fun createStateForInitialCountDownTimer(
        state: TimerViewState,
        quest: Quest
    ): TimerViewState {
        return state.copy(
            type = SHOW_COUNTDOWN,
            questName = quest.name,
            timerType = TimerViewState.TimerType.COUNTDOWN,
            showTimerTypeSwitch = quest.duration >= MIN_POMODORO_TIMER_DURATION,
            timerLabel = formatDuration(quest.duration.minutes.asSeconds),
            remainingTime = quest.duration.minutes.asSeconds,
            timerProgress = 0,
            maxTimerProgress = quest.duration.minutes.asSeconds.intValue
        )
    }

    private fun createStateForRunningPomodoroTimer(
        quest: Quest,
        state: TimerViewState
    ): TimerViewState {
        val questPomodoroTimeRanges = quest.timeRanges

        val result = splitDurationForPomodoroTimerUseCase.execute(
            SplitDurationForPomodoroTimerUseCase.Params(quest)
        )
        val timeRanges = if (result == DurationNotSplit) {
            questPomodoroTimeRanges
        } else {
            (result as DurationSplit).timeRanges
        }

        val currentProgressIndicator =
            findCurrentProgressIndicator(timeRanges)

        val currentTimeRange = if (currentProgressIndicator >= 0) {
            questPomodoroTimeRanges.last()
        } else {
            timeRanges[questPomodoroTimeRanges.size]
        }

        val duration = currentTimeRange.duration
        val passed: Duration<Millisecond> = if (currentTimeRange.start != null)
            (Instant.now() - currentTimeRange.start).milliseconds
        else
            0.milliseconds

        val remainingTime = duration.minutes - passed

        val type =
            if (questPomodoroTimeRanges.last().end == null) RESUMED
            else SHOW_POMODORO

        return state.copy(
            type = type,
            questName = quest.name,
            timerType = TimerViewState.TimerType.POMODORO,
            showTimerTypeSwitch = false,
            pomodoroProgress = timeRanges.map { createPomodoroProgress(it) },
            timerLabel = formatDuration(remainingTime.asSeconds),
            remainingTime = remainingTime.asSeconds,
            currentProgressIndicator = currentProgressIndicator,
            timerProgress = passed.asSeconds.intValue,
            maxTimerProgress = duration.minutes.asSeconds.intValue
        )
    }

    private fun createStateForRunningCountdownTimer(
        quest: Quest,
        state: TimerViewState
    ): TimerViewState {
//        val timeRange = quest.timeRanges.first()
//        val (type, passed) = if (timeRange.end == null) {
//            Pair(RESUMED, (Instant.now() - quest.actualStart!!).milliseconds)
//        } else {
//            Pair(SHOW_COUNTDOWN, timeRange.actualDuration().asMilliseconds)
//        }

        val passed = Instant.now() - quest.actualStart!!
        val remainingTime =
            quest.duration.minutes - passed.milliseconds
        return state.copy(
            type = RESUMED,
            questName = quest.name,
            timerType = TimerViewState.TimerType.COUNTDOWN,
            showTimerTypeSwitch = false,
            timerLabel = formatDuration(remainingTime.asSeconds),
            remainingTime = remainingTime.asSeconds,
            timerProgress = passed.milliseconds.asSeconds.intValue,
            maxTimerProgress = quest.duration.minutes.asSeconds.intValue
        )
    }

    private fun findCurrentProgressIndicator(timeRanges: List<TimeRange>): Int =
        timeRanges.indexOfFirst { it.start != null && it.end == null }

    private fun createPomodoroProgress(timeRange: TimeRange): PomodoroProgress {
        return when (timeRange.type) {
            TimeRange.Type.POMODORO_SHORT_BREAK -> {
                if (timeRange.end != null) {
                    PomodoroProgress.COMPLETE_SHORT_BREAK
                } else {
                    PomodoroProgress.INCOMPLETE_SHORT_BREAK
                }
            }

            TimeRange.Type.POMODORO_LONG_BREAK -> {
                if (timeRange.end != null) {
                    PomodoroProgress.COMPLETE_LONG_BREAK
                } else {
                    PomodoroProgress.INCOMPLETE_LONG_BREAK
                }
            }

            TimeRange.Type.POMODORO_WORK -> {
                if (timeRange.end != null) {
                    PomodoroProgress.COMPLETE_WORK
                } else {
                    PomodoroProgress.INCOMPLETE_WORK
                }
            }
            else -> PomodoroProgress.COMPLETE_WORK
        }
    }

    companion object {
        const val MIN_INITIAL_POMODORO_TIMER_DURATION =
            Constants.DEFAULT_POMODORO_WORK_DURATION * 2 + Constants.DEFAULT_POMODORO_BREAK_DURATION * 2

        const val MIN_POMODORO_TIMER_DURATION =
            Constants.DEFAULT_POMODORO_WORK_DURATION + Constants.DEFAULT_POMODORO_BREAK_DURATION
    }
}