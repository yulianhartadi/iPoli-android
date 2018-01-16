package mypoli.android.timer

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.usecase.ListenForQuestChangeUseCase
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit
import mypoli.android.quest.usecase.SplitDurationForPomodoroTimerUseCase.Result.DurationSplit
import mypoli.android.timer.TimerViewState.StateType.LOADING
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
                val parameters = SplitDurationForPomodoroTimerUseCase.Params(intent.quest)
                val result = splitDurationForPomodoroTimerUseCase.execute(parameters)
                when (result) {
                    is DurationNotSplit -> {
                        state.copy(
                            timerType = TimerViewState.TimerType.COUNTDOWN,
                            showTimerTypeSwitch = false
                        )
                    }

                    is DurationSplit -> {
                        state.copy(
                            timerType = TimerViewState.TimerType.POMODORO,
                            showTimerTypeSwitch = true
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