package io.ipoli.android.challenge.predefined

import io.ipoli.android.challenge.predefined.PersonalizeChallengeViewState.StateType.*
import io.ipoli.android.challenge.predefined.entity.PredefinedChallenge
import io.ipoli.android.challenge.predefined.entity.PredefinedChallengeData
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */

sealed class PersonalizeChallengeAction : Action {
    data class Load(val challenge: PredefinedChallenge) : PersonalizeChallengeAction() {
        override fun toMap() = mapOf("challenge" to challenge)
    }
    object AcceptChallenge : PersonalizeChallengeAction()
    data class ToggleSelected(val quest: PredefinedChallengeData.Quest) :
        PersonalizeChallengeAction() {
        override fun toMap() = mapOf("quest" to quest)
    }

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
) : BaseViewState() {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        VALIDATION_SUCCESSFUL,
        VALIDATION_ERROR_NO_QUESTS_SELECTED,
        TOGGLE_SELECTED_QUEST
    }
}