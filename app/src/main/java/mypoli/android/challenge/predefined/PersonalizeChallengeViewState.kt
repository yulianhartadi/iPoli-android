package mypoli.android.challenge.predefined

import mypoli.android.challenge.predefined.PersonalizeChallengeViewController.ChallengeQuestViewModel
import mypoli.android.challenge.predefined.PersonalizeChallengeViewState.StateType.*
import mypoli.android.challenge.predefined.entity.PredefinedChallenge
import mypoli.android.challenge.predefined.entity.PredefinedChallengeData
import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */

sealed class PersonalizeChallengeIntent : Intent {
    data class LoadData(val challenge: PredefinedChallenge) : PersonalizeChallengeIntent()
    data class AcceptChallenge(val challenge: PredefinedChallenge) : PersonalizeChallengeIntent()
    data class ToggleSelected(val quest: ChallengeQuestViewModel) : PersonalizeChallengeIntent()
}

sealed class PersonalizeChallengeAction : Action {
    data class Load(val challenge: PredefinedChallenge) : PersonalizeChallengeAction()
    object AcceptChallenge : PersonalizeChallengeAction()
    data class ToggleSelected(val quest: PredefinedChallengeData.Quest) :
        PersonalizeChallengeAction()

    object Validate : PersonalizeChallengeAction()
}

object PersonalizeChallengeReducer : BaseViewStateReducer<PersonalizeChallengeViewState>() {
    override val stateKey = key<PersonalizeChallengeViewState>()

    override fun reduce(
        state: AppState,
        subState: PersonalizeChallengeViewState,
        action: Action
    ): PersonalizeChallengeViewState {
        return when (action) {
            is PersonalizeChallengeAction.Load -> {
                subState.copy(
                    type = DATA_LOADED,
                    challenge = action.challenge,
                    selectedQuests = action.challenge.quests.filter { it.isSelected }.toSet()
                )
            }

            is PersonalizeChallengeAction.ToggleSelected -> {
                val quest = action.quest
                val selectedQuests = if (subState.selectedQuests.contains(quest)) {
                    subState.selectedQuests - quest
                } else {
                    subState.selectedQuests + quest
                }
                subState.copy(
                    type = TOGGLE_SELECTED_QUEST,
                    challenge = subState.challenge,
                    selectedQuests = selectedQuests
                )
            }

            is PersonalizeChallengeAction.Validate -> {
                subState.copy(
                    type = if (subState.selectedQuests.isEmpty()) {
                        VALIDATION_ERROR_NO_QUESTS_SELECTED
                    } else {
                        VALIDATION_SUCCESSFUL
                    }
                )
            }
            else -> subState
        }
    }

    override fun defaultState() = PersonalizeChallengeViewState(
        type = LOADING,
        challenge = null,
        selectedQuests = setOf()
    )
}

data class PersonalizeChallengeViewState(
    val type: StateType,
    val challenge: PredefinedChallenge?,
    val selectedQuests: Set<PredefinedChallengeData.Quest>
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        VALIDATION_SUCCESSFUL,
        VALIDATION_ERROR_NO_QUESTS_SELECTED,
        TOGGLE_SELECTED_QUEST
    }
}