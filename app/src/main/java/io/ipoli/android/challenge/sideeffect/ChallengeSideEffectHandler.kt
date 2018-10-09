package io.ipoli.android.challenge.sideeffect

import io.ipoli.android.MyPoliApp
import io.ipoli.android.challenge.QuestPickerAction
import io.ipoli.android.challenge.QuestPickerViewState
import io.ipoli.android.challenge.add.EditChallengeAction
import io.ipoli.android.challenge.add.EditChallengeViewState
import io.ipoli.android.challenge.complete.CompleteChallengePopup
import io.ipoli.android.challenge.show.ChallengeAction
import io.ipoli.android.challenge.show.ChallengeViewState
import io.ipoli.android.challenge.usecase.*
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.view.asThemedWrapper
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.RepeatingQuest
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/7/18.
 */
object ChallengeSideEffectHandler : AppSideEffectHandler() {

    private val saveQuestsForChallengeUseCase by required { saveQuestsForChallengeUseCase }
    private val saveChallengeUseCase by required { saveChallengeUseCase }
    private val removeChallengeUseCase by required { removeChallengeUseCase }
    private val removeQuestFromChallengeUseCase by required { removeQuestFromChallengeUseCase }
    private val removeHabitFromChallengeUseCase by required { removeHabitFromChallengeUseCase }
    private val logDataUseCase by required { logDataUseCase }
    private val loadQuestPickerQuestsUseCase by required { loadQuestPickerQuestsUseCase }
    private val completeChallengeUseCase by required { completeChallengeUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is QuestPickerAction.Load -> {
                val result = loadQuestPickerQuestsUseCase.execute(
                    LoadQuestPickerQuestsUseCase.Params(action.challengeId)
                )
                dispatch(
                    QuestPickerAction.Loaded(result.quests, result.repeatingQuests)
                )
            }

            is QuestPickerAction.Save -> {
                val pickerState = state.stateFor(QuestPickerViewState::class.java)
                val challengeId = pickerState.challengeId

                val quests = pickerState.allQuests.map {
                    it.baseQuest
                }

                saveQuestsForChallengeUseCase.execute(
                    SaveQuestsForChallengeUseCase.Params.WithExistingQuests(
                        challengeId,
                        quests,
                        pickerState.selectedQuests
                    )
                )
            }

            is EditChallengeAction.SaveNew -> {
                val s = state.stateFor(EditChallengeViewState::class.java)
                val params = SaveChallengeUseCase.Params.WithExistingQuests(
                    name = s.name,
                    tags = s.challengeTags,
                    color = s.color,
                    icon = s.icon,
                    difficulty = s.difficulty,
                    end = s.end,
                    trackedValues = s.trackedValues,
                    motivations = listOf(s.motivation1, s.motivation2, s.motivation3),
                    allQuests = s.allQuests,
                    selectedQuestIds = s.selectedQuestIds,
                    note = s.note
                )
                saveChallengeUseCase.execute(
                    params
                )
            }

            is EditChallengeAction.Save -> {
                val s = state.stateFor(EditChallengeViewState::class.java)
                val params =
                    SaveChallengeUseCase.Params.WithExistingQuests(
                        id = s.id,
                        name = s.name,
                        tags = s.challengeTags,
                        color = s.color,
                        icon = s.icon,
                        difficulty = s.difficulty,
                        end = s.end,
                        trackedValues = s.trackedValues,
                        motivations = listOf(s.motivation1, s.motivation2, s.motivation3),
                        note = s.note
                    )

                saveChallengeUseCase.execute(
                    params
                )
            }

            is ChallengeAction.Remove ->
                removeChallengeUseCase.execute(
                    RemoveChallengeUseCase.Params(
                        action.challengeId
                    )
                )

            is ChallengeAction.RemoveQuestFromChallenge -> {
                val s = state.stateFor(ChallengeViewState::class.java)
                val q = s.quests[action.questIndex]
                val params = when (q) {
                    is Quest -> RemoveQuestFromChallengeUseCase.Params.WithQuestId(q.id)
                    is RepeatingQuest -> RemoveQuestFromChallengeUseCase.Params.WithRepeatingQuestId(
                        q.id
                    )
                }

                removeQuestFromChallengeUseCase.execute(params)
            }

            is ChallengeAction.RemoveHabitFromChallenge -> {
                removeHabitFromChallengeUseCase.execute(
                    RemoveHabitFromChallengeUseCase.Params(
                        action.habitId
                    )
                )
            }

            is ChallengeAction.LogValue ->
                logDataUseCase.execute(
                    LogDataUseCase.Params(
                        action.challengeId,
                        action.trackValueId,
                        action.log
                    )
                )


            is ChallengeAction.Complete -> {
                val c =
                    completeChallengeUseCase.execute(CompleteChallengeUseCase.Params(action.challengeId))
                GlobalScope.launch(Dispatchers.Main) {
                    CompleteChallengePopup(c).show(MyPoliApp.instance.asThemedWrapper())
                }
            }
        }

    }

    override fun canHandle(action: Action) =
        action is QuestPickerAction
            || action is EditChallengeAction
            || action is ChallengeAction
}