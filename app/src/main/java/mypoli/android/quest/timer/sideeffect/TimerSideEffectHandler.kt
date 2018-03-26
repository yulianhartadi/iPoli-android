package mypoli.android.quest.timer.sideeffect

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import mypoli.android.common.AppSideEffectHandler
import mypoli.android.common.AppState
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.redux.Action
import mypoli.android.quest.Quest
import mypoli.android.quest.timer.TimerAction
import mypoli.android.quest.timer.TimerReducer
import mypoli.android.quest.timer.TimerViewState
import mypoli.android.quest.timer.usecase.*
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/22/2018.
 */

data class TimerStartedAction(val otherTimerStopped: Boolean = false) : Action

class TimerSideEffectHandler : AppSideEffectHandler() {

    private val questRepository by required { questRepository }

    private val splitDurationForPomodoroTimerUseCase by required { splitDurationForPomodoroTimerUseCase }
    private val addTimerToQuestUseCase by required { addTimerToQuestUseCase }
    private val completeTimeRangeUseCase by required { completeTimeRangeUseCase }
    private val cancelTimerUseCase by required { cancelTimerUseCase }
    private val addPomodoroUseCase by required { addPomodoroUseCase }
    private val removePomodoroUseCase by required { removePomodoroUseCase }

    private var questChannel: ReceiveChannel<Quest?>? = null

    override fun canHandle(action: Action) = action is TimerAction

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {

            is TimerAction.Load -> {
                questChannel?.cancel()
                questChannel = questRepository.listenById(action.questId)
                questChannel!!.consumeEach {

                    val q = it!!

                    if (isPomodoroQuest(q)) {
                        val timeRanges = q.timeRanges

                        val result = splitDurationForPomodoroTimerUseCase.execute(
                            SplitDurationForPomodoroTimerUseCase.Params(q)
                        )
                        val timeRangesToComplete =
                            if (result == SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit) {
                                timeRanges
                            } else {
                                (result as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges
                            }

                        dispatch(
                            DataLoadedAction.QuestChanged(
                                q.copy(
                                    timeRangesToComplete = timeRangesToComplete
                                )
                            )
                        )
                    } else {
                        dispatch(DataLoadedAction.QuestChanged(q))
                    }
                }
            }

            TimerAction.Start -> {

                val timerState = timerState(state)
                val q = timerState.quest!!

                if (q.hasTimer) {
                    completeTimeRangeUseCase.execute(
                        CompleteTimeRangeUseCase.Params(
                            questId = q.id
                        )
                    )
                    dispatch(TimerStartedAction())
                } else {
                    val result = addTimerToQuestUseCase.execute(
                        AddTimerToQuestUseCase.Params(
                            questId = q.id,
                            isPomodoro = timerState.timerType == TimerViewState.TimerType.POMODORO
                        )
                    )

                    dispatch(TimerStartedAction(result.otherTimerStopped))
                }
            }

            TimerAction.Stop ->
                cancelTimerUseCase.execute(CancelTimerUseCase.Params(questId(state)))

            TimerAction.CompletePomodoro ->
                completeTimeRangeUseCase.execute(
                    CompleteTimeRangeUseCase.Params(
                        questId = questId(state)
                    )
                )

            TimerAction.CompleteQuest ->
                completeTimeRangeUseCase.execute(CompleteTimeRangeUseCase.Params(questId(state)))

            TimerAction.AddPomodoro ->
                addPomodoroUseCase.execute(AddPomodoroUseCase.Params(questId(state)))

            TimerAction.RemovePomodoro -> removePomodoroUseCase.execute(
                RemovePomodoroUseCase.Params(
                    questId(state)
                )
            )
        }
    }

    private fun isPomodoroQuest(q: Quest) =
        q.hasPomodoroTimer || q.duration >= TimerReducer.MIN_INITIAL_POMODORO_TIMER_DURATION

    private fun questId(state: AppState) =
        timerState(state).quest!!.id

    private fun timerState(state: AppState) =
        state.stateFor(TimerViewState::class.java)

}