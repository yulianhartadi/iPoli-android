package io.ipoli.android.challenge.picker

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.picker.ChallengePickerViewState.StateType.*
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.pet.PetAvatar

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/11/18.
 */
sealed class ChallengePickerAction : Action {
    object Select : ChallengePickerAction()
    data class Load(val challenge: Challenge?) : ChallengePickerAction()
    data class ChangeSelected(val challenge: Challenge) : ChallengePickerAction()
}

object ChallengePickerReducer : BaseViewStateReducer<ChallengePickerViewState>() {
    override val stateKey = key<ChallengePickerViewState>()

    override fun reduce(
        state: AppState,
        subState: ChallengePickerViewState,
        action: Action
    ) = when (action) {
        is ChallengePickerAction.Load -> {
            val challenges = state.dataState.challenges
            subState.copy(
                type = DATA_LOADED,
                petAvatar = state.dataState.player!!.pet.avatar,
                challenges = challenges,
                selectedChallenge = action.challenge,
                showEmpty = challenges.isEmpty()
            )
        }

        is ChallengePickerAction.ChangeSelected -> {
            subState.copy(
                type = CHALLENGE_CHANGED,
                selectedChallenge = action.challenge
            )
        }

        is ChallengePickerAction.Select -> {
            subState.copy(
                type = CHALLENGE_SELECTED
            )
        }

        else -> subState
    }

    override fun defaultState() = ChallengePickerViewState(
        type = LOADING,
        petAvatar = PetAvatar.ELEPHANT,
        challenges = listOf(),
        selectedChallenge = null,
        showEmpty = false
    )

}

data class ChallengePickerViewState(
    val type: StateType,
    val petAvatar: PetAvatar,
    val challenges: List<Challenge>,
    val selectedChallenge: Challenge?,
    val showEmpty: Boolean
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        CHALLENGE_CHANGED,
        CHALLENGE_SELECTED
    }
}
