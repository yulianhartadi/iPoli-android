package mypoli.android.timer

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.TimeRange
import mypoli.android.quest.usecase.ListenForQuestChangeUseCase
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationSplit
import mypoli.android.timer.TimerViewState.StateType.*
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 6.01.18.
 */
class TimerPresenter(
    private val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase,
    private val listenForQuestChangeUseCase: ListenForQuestChangeUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<TimerViewState>, TimerViewState, TimerIntent>(
    TimerViewState(LOADING),
    coroutineContext
) {
    override fun reduceState(intent: TimerIntent, state: TimerViewState) =

        when (intent) {
            is TimerIntent.LoadData -> {
                launch {
                    listenForQuestChangeUseCase.execute(ListenForQuestChangeUseCase.Params(intent.questId)).consumeEach {
                        sendChannel.send(TimerIntent.QuestChanged(it))
                    }
                }
                state.copy(
                    type = LOADING
                )
            }

            is TimerIntent.QuestChanged -> {
                val quest = intent.quest
                val parameters = SplitDurationForPomodoroTimerUseCase.Params(quest)
                val result = splitDurationForPomodoroTimerUseCase.execute(parameters)
                when (result) {
                    is DurationNotSplit -> {
                        state.copy(
                            type = SHOW_COUNTDOWN,
                            questName = quest.name,
                            timerType = TimerViewState.TimerType.COUNTDOWN,
                            showTimerTypeSwitch = false
                        )
                    }

                    is DurationSplit -> {
                        val pomodoroProgress = result.timeRanges.map {
                            when (it.type) {
                                TimeRange.Type.BREAK -> {
                                    if (it.duration == Constants.DEFAULT_POMODORO_BREAK_DURATION) {
                                        if (it.end == null) {
                                            PomodoroProgress.INCOMPLETE_SHORT_BREAK
                                        } else {
                                            PomodoroProgress.COMPLETE_SHORT_BREAK
                                        }
                                    } else {
                                        if (it.end == null) {
                                            PomodoroProgress.INCOMPLETE_LONG_BREAK
                                        } else {
                                            PomodoroProgress.COMPLETE_LONG_BREAK
                                        }
                                    }
                                }

                                TimeRange.Type.WORK -> {
                                    if (it.end == null) {
                                        PomodoroProgress.INCOMPLETE_WORK
                                    } else {
                                        PomodoroProgress.COMPLETE_WORK
                                    }
                                }
                            }
                        }

                        state.copy(
                            type = SHOW_POMODORO,
                            questName = quest.name,
                            timerType = TimerViewState.TimerType.POMODORO,
                            showTimerTypeSwitch = true,
                            pomodoroProgress = pomodoroProgress
                        )
                    }
                }
            }

            is TimerIntent.Start -> {
                state
            }

            is TimerIntent.Stop -> {
                state
            }
        }

}