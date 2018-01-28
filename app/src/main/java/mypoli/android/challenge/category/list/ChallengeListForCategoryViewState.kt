package mypoli.android.challenge.category.list

import mypoli.android.challenge.category.list.ChallengeListForCategoryViewController.ChallengeViewModel
import mypoli.android.challenge.data.Challenge
import mypoli.android.challenge.data.PredefinedChallenge
import mypoli.android.common.AppState
import mypoli.android.common.AppStateReducer
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.player.Player

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
sealed class ChallengeListForCategoryIntent : Intent {
    data class ChangePlayerIntent(val player: Player) : ChallengeListForCategoryIntent()
    data class LoadData(val challengeCategory: Challenge.Category) :
        ChallengeListForCategoryIntent()

    data class BuyChallenge(val challenge: PredefinedChallenge) : ChallengeListForCategoryIntent()
}

data class ChallengeListForCategoryState(
    val type: StateType,
    val category: Challenge.Category?,
    val playerGems: Int?,
    val challenges: List<Challenge>
) : State {
    enum class StateType {
        LOADING,
        PLAYER_CHANGED,
        CHALLENGE_BOUGHT,
        CHALLENGE_TOO_EXPENSIVE

    }
}

sealed class ChallengeListForCategoryAction : Action {
    data class BuyChallenge(val challenge: PredefinedChallenge) : ChallengeListForCategoryAction()

    data class ChallengeBought(val challenge: PredefinedChallenge) :
        ChallengeListForCategoryAction()

    data class ChallengeTooExpensive(val challenge: PredefinedChallenge) :
        ChallengeListForCategoryAction()

}

object ChallengeListForCategoryReducer :
    AppStateReducer<ChallengeListForCategoryState> {

    override fun reduce(
        state: AppState,
        action: Action
    ) =
        state.challengeListForCategoryState.let {
            when (action) {
                is ChallengeListForCategoryAction.BuyChallenge -> {
                    it.copy(
                        type = ChallengeListForCategoryState.StateType.LOADING
                    )
                }

                is ChallengeListForCategoryAction.ChallengeBought -> {
                    it.copy(
                        type = ChallengeListForCategoryState.StateType.CHALLENGE_BOUGHT
                    )
                }

                is ChallengeListForCategoryAction.ChallengeTooExpensive -> {
                    it.copy(
                        type = ChallengeListForCategoryState.StateType.CHALLENGE_TOO_EXPENSIVE
                    )
                }
                else -> it
            }
        }

    override fun defaultState() =
        ChallengeListForCategoryState(
            type = ChallengeListForCategoryState.StateType.LOADING,
            category = null,
            playerGems = null,
            challenges = listOf()
        )
}

data class ChallengeListForCategoryViewState(
    val type: StateType,
    val challengeCategory: Challenge.Category? = null,
    val playerGems: Int = 0,
    val viewModels: List<ChallengeViewModel> = listOf()
) : ViewState {
    enum class StateType {
        LOADING,
        PLAYER_CHANGED,
        CHALLENGE_BOUGHT,
        CHALLENGE_TOO_EXPENSIVE

    }
}