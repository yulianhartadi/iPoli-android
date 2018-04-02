package io.ipoli.android.quest.timer.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.note.usecase.SaveQuestNoteUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.subquest.usecase.*
import io.ipoli.android.quest.timer.QuestAction
import io.ipoli.android.quest.timer.QuestReducer
import io.ipoli.android.quest.timer.QuestViewState
import io.ipoli.android.quest.timer.usecase.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/22/2018.
 */

data class TimerStartedAction(val otherTimerStopped: Boolean = false) : Action

class QuestSideEffectHandler : AppSideEffectHandler() {

    private val questRepository by required { questRepository }

    private val splitDurationForPomodoroTimerUseCase by required { splitDurationForPomodoroTimerUseCase }
    private val addTimerToQuestUseCase by required { addTimerToQuestUseCase }
    private val completeTimeRangeUseCase by required { completeTimeRangeUseCase }
    private val cancelTimerUseCase by required { cancelTimerUseCase }
    private val addPomodoroUseCase by required { addPomodoroUseCase }
    private val removePomodoroUseCase by required { removePomodoroUseCase }
    private val completeSubQuestUseCase by required { completeSubQuestUseCase }
    private val undoCompletedSubQuestUseCase by required { undoCompletedSubQuestUseCase }
    private val saveSubQuestNameUseCase by required { saveSubQuestNameUseCase }
    private val addSubQuestUseCase by required { addSubQuestUseCase }
    private val removeSubQuestUseCase by required { removeSubQuestUseCase }
    private val reorderSubQuestUseCase by required { reorderSubQuestUseCase }
    private val saveQuestNoteUseCase by required { saveQuestNoteUseCase }

    private var questChannel: ReceiveChannel<Quest?>? = null

    override fun canHandle(action: Action) = action is QuestAction

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {

            is QuestAction.Load -> {
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

            QuestAction.Start -> {

                val timerState = questState(state)
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
                            isPomodoro = timerState.timerType == QuestViewState.TimerType.POMODORO
                        )
                    )

                    dispatch(TimerStartedAction(result.otherTimerStopped))
                }
            }

            QuestAction.Stop ->
                cancelTimerUseCase.execute(CancelTimerUseCase.Params(questId(state)))

            QuestAction.CompletePomodoro ->
                completeTimeRangeUseCase.execute(
                    CompleteTimeRangeUseCase.Params(
                        questId = questId(state)
                    )
                )

            QuestAction.CompleteQuest ->
                completeTimeRangeUseCase.execute(CompleteTimeRangeUseCase.Params(questId(state)))

            QuestAction.AddPomodoro ->
                addPomodoroUseCase.execute(AddPomodoroUseCase.Params(questId(state)))

            QuestAction.RemovePomodoro -> removePomodoroUseCase.execute(
                RemovePomodoroUseCase.Params(
                    questId = questId(state)
                )
            )

            is QuestAction.CompleteSubQuest ->
                completeSubQuestUseCase.execute(
                    CompleteSubQuestUseCase.Params(
                        subQuestIndex = action.position,
                        questId = questId(state)
                    )
                )

            is QuestAction.UndoCompletedSubQuest ->
                undoCompletedSubQuestUseCase.execute(
                    UndoCompletedSubQuestUseCase.Params(
                        subQuestIndex = action.position,
                        questId = questId(state)
                    )
                )

            is QuestAction.SaveSubQuestName ->
                saveSubQuestNameUseCase.execute(
                    SaveSubQuestNameUseCase.Params(
                        newName = action.name,
                        questId = questId(state),
                        index = action.position
                    )
                )

            is QuestAction.AddSubQuest ->
                addSubQuestUseCase.execute(
                    AddSubQuestUseCase.Params(
                        name = action.name,
                        questId = questId(state)
                    )
                )

            is QuestAction.RemoveSubQuest ->
                removeSubQuestUseCase.execute(
                    RemoveSubQuestUseCase.Params(
                        subQuestIndex = action.position,
                        questId = questId(state)
                    )
                )

            is QuestAction.ReorderSubQuest ->
                reorderSubQuestUseCase.execute(
                    ReorderSubQuestUseCase.Params(
                        oldPosition = action.oldPosition,
                        newPosition = action.newPosition,
                        questId = questId(state)
                    )
                )

            is QuestAction.SaveNote -> {
                saveQuestNoteUseCase.execute(
                    SaveQuestNoteUseCase.Params(
                        questId = questId(state),
                        note = action.note
                    )
                )
            }
        }
    }

    private fun isPomodoroQuest(q: Quest) =
        q.hasPomodoroTimer || q.duration >= QuestReducer.MIN_INITIAL_POMODORO_TIMER_DURATION

    private fun questId(state: AppState) =
        questState(state).quest!!.id

    private fun quest(state: AppState) =
        questState(state).quest!!

    private fun questState(state: AppState) =
        state.stateFor(QuestViewState::class.java)

}