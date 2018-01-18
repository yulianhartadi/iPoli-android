package mypoli.android.timer

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.common.datetime.minutes
import mypoli.android.common.datetime.seconds
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.TimeRange
import mypoli.android.quest.usecase.CancelQuestTimerUseCase
import mypoli.android.quest.usecase.ListenForQuestChangeUseCase
import mypoli.android.quest.usecase.SaveQuestActualDurationUseCase
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationSplit
import mypoli.android.timer.TimerViewState.StateType.*
import mypoli.android.timer.view.formatter.TimerFormatter
import timber.log.Timber
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
                val quest = intent.quest
                val parameters = SplitDurationForPomodoroTimerUseCase.Params(quest)
                val result = splitDurationForPomodoroTimerUseCase.execute(parameters)
                Timber.d("AAAA $result")
                val showTimerTypeSwitch =
                    quest.actualStart == null && quest.pomodoroTimeRanges.isEmpty()

                when (result) {
                    is DurationNotSplit -> {
                        state.copy(
                            type = SHOW_COUNTDOWN,
                            questName = quest.name,
                            timerType = TimerViewState.TimerType.COUNTDOWN,
                            showTimerTypeSwitch = showTimerTypeSwitch,
                            timerLabel = TimerFormatter.format(quest.duration.minutes.asMilliseconds.longValue),
                            remainingTime = quest.duration.minutes.asSeconds
                        )
                    }

                    is DurationSplit -> {
                        val pomodoroProgress = result.timeRanges.map(this::createPomodoroProgress)
                        val duration =
                            if (quest.pomodoroTimeRanges.isEmpty()) Constants.DEFAULT_POMODORO_WORK_DURATION
                            else quest.pomodoroTimeRanges.last().duration
                        val currentProgressIndicator =
                            findCurrentProgressIndicator(result.timeRanges)

                        state.copy(
                            type = SHOW_POMODORO,
                            questName = quest.name,
                            timerType = TimerViewState.TimerType.POMODORO,
                            showTimerTypeSwitch = showTimerTypeSwitch,
                            pomodoroProgress = pomodoroProgress,
                            timerLabel = TimerFormatter.format(duration.minutes.asMilliseconds.longValue),
//                            remainingTime = duration.minutes.asSeconds,
                            remainingTime = 5.seconds,
                            currentProgressIndicator = currentProgressIndicator
                        )
                    }
                }
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

    private fun findCurrentProgressIndicator(timeRanges: List<TimeRange>): Int =
        timeRanges.indexOfFirst { it.start != null && it.end == null }

    private fun createPomodoroProgress(timeRange: TimeRange): PomodoroProgress {
        return when (timeRange.type) {
            TimeRange.Type.BREAK -> {
                if (timeRange.duration == Constants.DEFAULT_POMODORO_BREAK_DURATION) {
                    if (timeRange.end == null) {
                        PomodoroProgress.INCOMPLETE_SHORT_BREAK
                    } else {
                        PomodoroProgress.COMPLETE_SHORT_BREAK
                    }
                } else {
                    if (timeRange.end == null) {
                        PomodoroProgress.INCOMPLETE_LONG_BREAK
                    } else {
                        PomodoroProgress.COMPLETE_LONG_BREAK
                    }
                }
            }

            TimeRange.Type.WORK -> {
                if (timeRange.end == null) {
                    PomodoroProgress.INCOMPLETE_WORK
                } else {
                    PomodoroProgress.COMPLETE_WORK
                }
            }
        }
    }

}