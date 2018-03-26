package mypoli.android.challenge.sideeffect

import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import mypoli.android.challenge.QuestPickerAction
import mypoli.android.challenge.QuestPickerViewState
import mypoli.android.challenge.add.AddChallengeSummaryAction
import mypoli.android.challenge.add.AddChallengeViewState
import mypoli.android.challenge.complete.CompleteChallengePopup
import mypoli.android.challenge.edit.EditChallengeAction
import mypoli.android.challenge.edit.EditChallengeViewState
import mypoli.android.challenge.predefined.PersonalizeChallengeAction
import mypoli.android.challenge.predefined.PersonalizeChallengeViewState
import mypoli.android.challenge.predefined.entity.PredefinedChallengeData
import mypoli.android.challenge.predefined.usecase.SchedulePredefinedChallengeUseCase
import mypoli.android.challenge.show.ChallengeAction
import mypoli.android.challenge.show.ChallengeViewState
import mypoli.android.challenge.usecase.*
import mypoli.android.common.AppSideEffectHandler
import mypoli.android.common.AppState
import mypoli.android.common.redux.Action
import mypoli.android.common.view.asThemedWrapper
import mypoli.android.myPoliApp
import mypoli.android.quest.Quest
import mypoli.android.quest.RepeatingQuest
import org.threeten.bp.LocalDate
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/7/18.
 */
class ChallengeSideEffectHandler : AppSideEffectHandler() {

    private val saveQuestsForChallengeUseCase by required { saveQuestsForChallengeUseCase }
    private val saveChallengeUseCase by required { saveChallengeUseCase }
    private val removeChallengeUseCase by required { removeChallengeUseCase }
    private val removeQuestFromChallengeUseCase by required { removeQuestFromChallengeUseCase }
    private val loadQuestPickerQuestsUseCase by required { loadQuestPickerQuestsUseCase }
    private val completeChallengeUseCase by required { completeChallengeUseCase }
    private val schedulePredefinedChallengeUseCase by required { schedulePredefinedChallengeUseCase }

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

            is AddChallengeSummaryAction.Save -> {
                val s = state.stateFor(AddChallengeViewState::class.java)
                saveChallengeUseCase.execute(
                    SaveChallengeUseCase.Params.WithExistingQuests(
                        name = s.name,
                        color = s.color,
                        icon = s.icon,
                        difficulty = s.difficulty,
                        end = s.end,
                        motivations = s.motivationList,
                        allQuests = s.allQuests,
                        selectedQuestIds = s.selectedQuestIds
                    )
                )
            }

            is EditChallengeAction.Save -> {
                val s = state.stateFor(EditChallengeViewState::class.java)
                saveChallengeUseCase.execute(
                    SaveChallengeUseCase.Params.WithExistingQuests(
                        name = s.name,
                        color = s.color,
                        icon = s.icon,
                        difficulty = s.difficulty,
                        end = s.end,
                        motivations = listOf(s.motivation1, s.motivation2, s.motivation3)
                    )
                )
            }

            is ChallengeAction.Remove ->
                removeChallengeUseCase.execute(
                    RemoveChallengeUseCase.Params(
                        action.challengeId
                    )
                )

            is ChallengeAction.RemoveQuestFromChallenge -> {
                val s = state.stateFor(ChallengeViewState::class.java) as ChallengeViewState.Changed
                val q = s.quests[action.questIndex]
                val params = if (q is Quest) {
                    RemoveQuestFromChallengeUseCase.Params.WithQuestId(q.id)
                } else if (q is RepeatingQuest) {
                    RemoveQuestFromChallengeUseCase.Params.WithRepeatingQuestId(q.id)
                } else {
                    throw IllegalArgumentException("Unknown quest type ${q}")
                }

                removeQuestFromChallengeUseCase.execute(params)
            }

            is ChallengeAction.Complete -> {
                val c =
                    completeChallengeUseCase.execute(CompleteChallengeUseCase.Params(action.challengeId))
                launch(UI) {
                    CompleteChallengePopup(c).show(myPoliApp.instance.asThemedWrapper())
                }
            }

            is PersonalizeChallengeAction.AcceptChallenge -> {
                val s = state.stateFor(PersonalizeChallengeViewState::class.java)
                val predefinedChallenge = s.challenge!!

                val challenge =
                    PredefinedChallengeData(
                        predefinedChallenge.category,
                        predefinedChallenge.quests.filter { s.selectedQuests.contains(it) },
                        predefinedChallenge.durationDays
                    )
                val baseQuests = schedulePredefinedChallengeUseCase.execute(
                    SchedulePredefinedChallengeUseCase.Params(challenge)
                )
                saveChallengeUseCase.execute(
                    SaveChallengeUseCase.Params.WithNewQuests(
                        id = "",
                        name = predefinedChallenge.title,
                        color = predefinedChallenge.color,
                        icon = predefinedChallenge.icon,
                        difficulty = predefinedChallenge.difficulty,
                        motivations = predefinedChallenge.motivations,
                        end = LocalDate.now().plusDays((predefinedChallenge.durationDays - 1).toLong()),
                        quests = baseQuests
                    )
                )
            }

        }

    }

    override fun canHandle(action: Action) =
        action is QuestPickerAction
            || action is AddChallengeSummaryAction
            || action is EditChallengeAction
            || action is ChallengeAction
            || action is PersonalizeChallengeAction

}