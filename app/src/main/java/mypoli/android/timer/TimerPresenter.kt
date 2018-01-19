package mypoli.android.timer

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.common.datetime.milliseconds
import mypoli.android.common.datetime.minutes
import mypoli.android.common.datetime.seconds
import mypoli.android.common.datetime.toMillis
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.Quest
import mypoli.android.quest.TimeRange
import mypoli.android.quest.usecase.CancelQuestTimerUseCase
import mypoli.android.quest.usecase.ListenForQuestChangeUseCase
import mypoli.android.quest.usecase.SaveQuestActualDurationUseCase
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationSplit
import mypoli.android.timer.TimerViewState.StateType.*
import mypoli.android.timer.view.formatter.TimerFormatter
import org.threeten.bp.LocalDateTime
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 6.01.18.
 */
class TimerPresenter(
    private val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase,
    private val listenForQuestChangeUseCase: ListenForQuestChangeUseCase,
    private val saveQuestActualDurationUseCase: SaveQuestActualDurationUseCase,
    private val cancelQuestTimerUseCase: CancelQuestTimerUseCase,
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
                    type = LOADING,
                    questId = intent.questId
                )
            }

            is TimerIntent.QuestChanged -> {
                createQuestChangedState(intent.quest, state)
            }

            is TimerIntent.Start -> {
                val quest = saveQuestActualDurationUseCase.execute(
                    SaveQuestActualDurationUseCase.Params(
                        questId = state.questId,
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
                cancelQuestTimerUseCase.execute(CancelQuestTimerUseCase.Params(state.questId))
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
                        questId = state.questId,
                        isPomodoro = true
                    )
                )
                state.copy(
                    type = TIMER_STOPPED
                )
            }
        }

    private fun createQuestChangedState(
        quest: Quest,
        state: TimerViewState
    ): TimerViewState {

        if (quest.actualStart != null) {
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

        val pomodoroProgress = timeRanges.map { createPomodoroProgress(it) }

        return state.copy(
            type = SHOW_POMODORO,
            questName = quest.name,
            timerType = TimerViewState.TimerType.POMODORO,
            showTimerTypeSwitch = true,
            pomodoroProgress = pomodoroProgress,
            timerLabel = TimerFormatter.format(Constants.DEFAULT_POMODORO_WORK_DURATION.minutes.asMilliseconds.longValue),
            //                            remainingTime = duration.minutes.asSeconds,
            remainingTime = 15.seconds,
            currentProgressIndicator = 0
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
            remainingTime = quest.duration.minutes.asSeconds
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
                //                            remainingTime = duration.minutes.asSeconds,
                remainingTime = 15.seconds
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

            val pomodoroProgress = timeRanges.map { createPomodoroProgress(it) }

            val currentProgressIndicator =
                findCurrentProgressIndicator(timeRanges)


            val duration =
                if (questPomodoroTimeRanges.isEmpty()) Constants.DEFAULT_POMODORO_WORK_DURATION
                else {
                    if (currentProgressIndicator >= 0) {
                        questPomodoroTimeRanges.last().duration
                    } else {
                        timeRanges[questPomodoroTimeRanges.size].duration
                    }
                }


            val type =
                if (questPomodoroTimeRanges.isNotEmpty() && questPomodoroTimeRanges.last().end == null) RESUMED
                else SHOW_POMODORO

            return state.copy(
                type = type,
                questName = quest.name,
                timerType = TimerViewState.TimerType.POMODORO,
                showTimerTypeSwitch = false,
                pomodoroProgress = pomodoroProgress,
                timerLabel = TimerFormatter.format(duration.minutes.asMilliseconds.longValue),
                //                            remainingTime = duration.minutes.asSeconds,
                remainingTime = 15.seconds,
                currentProgressIndicator = currentProgressIndicator,
                timerProgress = 0,
                //                    maxTimerProgress = state.remainingTime!!.asSeconds.longValue.toInt()
                maxTimerProgress = 15.seconds.longValue.toInt()
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
                remainingTime = 0.seconds
            )
        } else {
            val remainingTime =
                quest.duration.minutes - (LocalDateTime.now().toMillis() - quest.actualStart!!.toMillis()).milliseconds
            return state.copy(
                type = RESUMED,
                questName = quest.name,
                timerType = TimerViewState.TimerType.COUNTDOWN,
                showTimerTypeSwitch = false,
                timerLabel = TimerFormatter.format(remainingTime.asMilliseconds.longValue),
                remainingTime = remainingTime.asSeconds
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