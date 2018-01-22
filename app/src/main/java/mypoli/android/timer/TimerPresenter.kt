package mypoli.android.timer

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.common.datetime.milliseconds
import mypoli.android.common.datetime.minus
import mypoli.android.common.datetime.minutes
import mypoli.android.common.datetime.seconds
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.Quest
import mypoli.android.quest.TimeRange
import mypoli.android.quest.usecase.CompleteQuestUseCase
import mypoli.android.quest.usecase.ListenForQuestChangeUseCase
import mypoli.android.timer.TimerViewState.StateType.*
import mypoli.android.timer.usecase.*
import mypoli.android.timer.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit
import mypoli.android.timer.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationSplit
import mypoli.android.timer.view.formatter.TimerFormatter
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
    private val saveQuestActualDurationUseCase: SaveQuestActualDurationUseCase,
    private val cancelTimerUseCase: CancelTimerUseCase,
    private val completeQuestUseCase: CompleteQuestUseCase,
    private val addPomodoroUseCase: AddPomodoroUseCase,
    private val removePomodoroUseCase: RemovePomodoroUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<TimerViewState>, TimerViewState, TimerIntent>(
    TimerViewState(LOADING),
    coroutineContext
) {
    override fun reduceState(intent: TimerIntent, state: TimerViewState) =

        when (intent) {
            is TimerIntent.LoadData -> {
                launch {
                    listenForQuestChangeUseCase.execute(ListenForQuestChangeUseCase.Params(intent.questId))
                        .consumeEach {
                            sendChannel.send(TimerIntent.QuestChanged(it))
                        }
                }
                state.copy(
                    type = LOADING
                )
            }

            is TimerIntent.QuestChanged -> {
                createQuestChangedState(state.copy(quest = intent.quest))
            }

            is TimerIntent.Start -> {
                val quest = saveQuestActualDurationUseCase.execute(
                    SaveQuestActualDurationUseCase.Params(
                        questId = state.quest!!.id,
                        isPomodoro = state.timerType == TimerViewState.TimerType.POMODORO
                    )
                )

                val currentProgressIndicator =
                    if (state.timerType == TimerViewState.TimerType.POMODORO)
                        findCurrentProgressIndicator(quest.pomodoroTimeRanges)
                    else state.currentProgressIndicator

                state.copy(
                    type = TIMER_STARTED,
                    timerProgress = 0,
                    maxTimerProgress = state.remainingTime!!.asSeconds.longValue.toInt(),
                    currentProgressIndicator = currentProgressIndicator
                )
            }

            is TimerIntent.Stop -> {
                cancelTimerUseCase.execute(CancelTimerUseCase.Params(state.quest!!.id))
                state.copy(
                    type = TIMER_STOPPED
                )
            }

            is TimerIntent.Tick -> {
                val remainingTime = state.remainingTime!! - 1.seconds
                val isOverdue = remainingTime < 0.seconds
                val label = if (!isOverdue) {
                    TimerFormatter.format(remainingTime.asMilliseconds.longValue)
                } else {
                    "+" + TimerFormatter.format(Math.abs(remainingTime.asMilliseconds.longValue))
                }

                state.copy(
                    type = RUNNING,
                    timerProgress = state.timerProgress + 1,
                    timerLabel = label,
                    remainingTime = remainingTime,
                    showCompletePomodoroButton = state.timerType == TimerViewState.TimerType.POMODORO && isOverdue
                )
            }

            is TimerIntent.CompletePomodoro -> {
                saveQuestActualDurationUseCase.execute(
                    SaveQuestActualDurationUseCase.Params(
                        questId = state.quest!!.id,
                        isPomodoro = true
                    )
                )
                state.copy(
                    type = TIMER_STOPPED
                )
            }

            is TimerIntent.ShowPomodoroTimer -> {
                createStateForInitialPomodoroTimer(state, state.quest!!)
            }

            is TimerIntent.ShowCountDownTimer -> {
                createStateForInitialCountDownTimer(state, state.quest!!)
            }

            is TimerIntent.CompleteQuest -> {
                completeQuestUseCase.execute(state.quest!!.id)
                state.copy(
                    type = TIMER_STOPPED
                )
            }

            is TimerIntent.AddPomodoro -> {
                addPomodoroUseCase.execute(AddPomodoroUseCase.Params(state.quest!!.id))
                state.copy(
                    type = POMODORO_ADDED
                )
            }

            is TimerIntent.RemovePomodoro -> {
                removePomodoroUseCase.execute(RemovePomodoroUseCase.Params(state.quest!!.id))
                state.copy(
                    type = POMODORO_REMOVED
                )
            }
        }

    private fun createQuestChangedState(
        state: TimerViewState
    ): TimerViewState {
        val quest = state.quest
        if (quest!!.actualStart != null) {
            return createStateForRunningOrCompletedCountdownTimer(quest, state)
        }

        if (quest.pomodoroTimeRanges.isNotEmpty()) {
            return createStateForRunningOrCompletedPomodoroTimer(quest, state)
        }

        return if (quest.duration < MIN_POMODORO_TIMER_DURATION) {
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
            quest.pomodoroTimeRanges
        } else {
            (result as DurationSplit).timeRanges
        }

        return state.copy(
            type = SHOW_POMODORO,
            questName = quest.name,
            timerType = TimerViewState.TimerType.POMODORO,
            showTimerTypeSwitch = true,
            pomodoroProgress = timeRanges.map { createPomodoroProgress(it) },
            timerLabel = TimerFormatter.format(Constants.DEFAULT_POMODORO_WORK_DURATION.minutes.asMilliseconds.longValue),
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
            showTimerTypeSwitch = true,
            timerLabel = TimerFormatter.format(quest.duration.minutes.asMilliseconds.longValue),
            remainingTime = quest.duration.minutes.asSeconds,
            timerProgress = 0,
            maxTimerProgress = quest.duration.minutes.asSeconds.intValue
        )
    }

    private fun createStateForRunningOrCompletedPomodoroTimer(
        quest: Quest,
        state: TimerViewState
    ): TimerViewState {
        val questPomodoroTimeRanges = quest.pomodoroTimeRanges

        if (quest.isCompleted) {
            return state.copy(
                type = SHOW_POMODORO,
                questName = quest.name,
                timerType = TimerViewState.TimerType.POMODORO,
                showTimerTypeSwitch = false,
                pomodoroProgress = questPomodoroTimeRanges.map { createPomodoroProgress(it) },
                timerLabel = TimerFormatter.format(0),
                remainingTime = 0.seconds,
                timerProgress = 0,
                maxTimerProgress = 0
            )
        } else {

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
            val passed = Instant.now() - currentTimeRange.start!!
            val remainingTime = duration.minutes - passed.milliseconds

            val type =
                if (questPomodoroTimeRanges.isNotEmpty() && questPomodoroTimeRanges.last().end == null) RESUMED
                else SHOW_POMODORO

            return state.copy(
                type = type,
                questName = quest.name,
                timerType = TimerViewState.TimerType.POMODORO,
                showTimerTypeSwitch = false,
                pomodoroProgress = timeRanges.map { createPomodoroProgress(it) },
                timerLabel = TimerFormatter.format(remainingTime.asMilliseconds.longValue),
                remainingTime = remainingTime.asSeconds,
                currentProgressIndicator = currentProgressIndicator,
                timerProgress = passed.milliseconds.asSeconds.intValue,
                maxTimerProgress = duration.minutes.asSeconds.intValue
            )
        }
    }

    private fun createStateForRunningOrCompletedCountdownTimer(
        quest: Quest,
        state: TimerViewState
    ): TimerViewState {
        if (quest.isCompleted) {
            return state.copy(
                type = SHOW_COUNTDOWN,
                questName = quest.name,
                timerType = TimerViewState.TimerType.COUNTDOWN,
                showTimerTypeSwitch = false,
                timerLabel = TimerFormatter.format(0),
                remainingTime = 0.seconds,
                timerProgress = quest.actualDuration,
                maxTimerProgress = quest.actualDuration
            )
        } else {
            val passed = Instant.now() - quest.actualStart!!
            val remainingTime =
                quest.duration.minutes - passed.milliseconds
            return state.copy(
                type = RESUMED,
                questName = quest.name,
                timerType = TimerViewState.TimerType.COUNTDOWN,
                showTimerTypeSwitch = false,
                timerLabel = TimerFormatter.format(remainingTime.asMilliseconds.longValue),
                remainingTime = remainingTime.asSeconds,
                timerProgress = passed.milliseconds.asSeconds.intValue,
                maxTimerProgress = quest.duration.minutes.asSeconds.intValue
            )
        }
    }

    private fun findCurrentProgressIndicator(timeRanges: List<TimeRange>): Int =
        timeRanges.indexOfFirst { it.start != null && it.end == null }

    private fun createPomodoroProgress(timeRange: TimeRange): PomodoroProgress {
        return when (timeRange.type) {
            TimeRange.Type.BREAK -> {
                if (timeRange.duration == Constants.DEFAULT_POMODORO_BREAK_DURATION) {
                    if (timeRange.end != null) {
                        PomodoroProgress.COMPLETE_SHORT_BREAK
                    } else {
                        PomodoroProgress.INCOMPLETE_SHORT_BREAK
                    }
                } else {
                    if (timeRange.end != null) {
                        PomodoroProgress.COMPLETE_LONG_BREAK
                    } else {
                        PomodoroProgress.INCOMPLETE_LONG_BREAK
                    }
                }
            }

            TimeRange.Type.WORK -> {
                if (timeRange.end != null) {
                    PomodoroProgress.COMPLETE_WORK
                } else {
                    PomodoroProgress.INCOMPLETE_WORK
                }
            }
        }
    }

    companion object {
        const val MIN_POMODORO_TIMER_DURATION =
            Constants.DEFAULT_POMODORO_WORK_DURATION * 2 + Constants.DEFAULT_POMODORO_BREAK_DURATION * 2
    }
}