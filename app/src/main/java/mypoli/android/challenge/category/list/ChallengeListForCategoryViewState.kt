package mypoli.android.challenge.category.list

import mypoli.android.challenge.category.list.ChallengeListForCategoryViewController.ChallengeViewModel
import mypoli.android.challenge.data.Challenge
import mypoli.android.challenge.data.PredefinedChallenge
import mypoli.android.challenge.usecase.BuyChallengeUseCase
import mypoli.android.common.AppState
import mypoli.android.common.di.Module
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.*
import mypoli.android.myPoliApp
import mypoli.android.player.Player
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

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
) : PartialState {
    enum class StateType {
        LOADING,
        PLAYER_CHANGED,
        CHALLENGE_BOUGHT,
        CHALLENGE_TOO_EXPENSIVE

    }
}

sealed class ChallengeListForCategoryAction : Action {
    data class BuyChallenge(val challenge: PredefinedChallenge) : ChallengeListForCategoryAction(),
        AsyncAction, Injects<Module> {

        private val buyChallengeUseCase by required { buyChallengeUseCase }

        override suspend fun execute(dispatcher: Dispatcher) {
            inject(myPoliApp.module(myPoliApp.instance))
            val result = buyChallengeUseCase.execute(BuyChallengeUseCase.Params(challenge))
            when (result) {
                is BuyChallengeUseCase.Result.ChallengeBought -> {
                    dispatcher.dispatch(ChallengeBought(challenge))
                }

                is BuyChallengeUseCase.Result.TooExpensive -> {
                    dispatcher.dispatch(ChallengeTooExpensive(challenge))
                }
            }
        }

    }

    data class ChallengeBought(val challenge: PredefinedChallenge) :
        ChallengeListForCategoryAction()

    data class ChallengeTooExpensive(val challenge: PredefinedChallenge) :
        ChallengeListForCategoryAction()

}

object ChallengeListForCategoryReducer :
    PartialReducer<AppState, ChallengeListForCategoryState, ChallengeListForCategoryAction> {
    override fun reduce(
        state: AppState,
        action: ChallengeListForCategoryAction
    ) =
        state.challengeListForCategoryState.also {
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