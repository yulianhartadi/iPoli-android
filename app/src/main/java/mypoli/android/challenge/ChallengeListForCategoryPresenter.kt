package mypoli.android.challenge

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.challenge.ChallengeListForCategoryViewState.StateType.*
import mypoli.android.challenge.data.AndroidPredefinedChallenge
import mypoli.android.challenge.data.PredefinedChallenge
import mypoli.android.challenge.usecase.BuyChallengeUseCase
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.player.Player
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/30/17.
 */
class ChallengeListForCategoryPresenter(private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase, private val buyChallengeUseCase: BuyChallengeUseCase, coroutineContext: CoroutineContext) :
    BaseMviPresenter<ViewStateRenderer<ChallengeListForCategoryViewState>, ChallengeListForCategoryViewState, ChallengeListForCategoryIntent>(
        ChallengeListForCategoryViewState(type = LOADING),
        coroutineContext
    ) {
    override fun reduceState(intent: ChallengeListForCategoryIntent, state: ChallengeListForCategoryViewState) =
        when (intent) {

            is ChallengeListForCategoryIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(ChallengeListForCategoryIntent.ChangePlayerIntent(it))
                    }
                }
                state.copy(
                    challengeCategory = intent.challengeCategory
                )
            }

            is ChallengeListForCategoryIntent.ChangePlayerIntent -> {
                val player = intent.player
                state.copy(
                    type = PLAYER_CHANGED,
                    playerGems = player.gems,
                    viewModels = createViewModels(state.challengeCategory!!, player)
                )
            }

            is ChallengeListForCategoryIntent.BuyChallenge -> {
                val result = buyChallengeUseCase.execute(BuyChallengeUseCase.Params(intent.challenge))
                when (result) {
                    is BuyChallengeUseCase.Result.TooExpensive -> state.copy(
                        type = CHALLENGE_TOO_EXPENSIVE
                    )
                    is BuyChallengeUseCase.Result.ChallengeBought -> state.copy(
                        type = CHALLENGE_BOUGHT
                    )
                }
            }
        }

    private fun createViewModels(challengeCategory: PredefinedChallenge.Category, player: Player): List<ChallengeListForCategoryViewController.ChallengeViewModel> {
        return PredefinedChallenge.values().filter { it.category == challengeCategory }.map {
            val andChallenge = AndroidPredefinedChallenge.valueOf(it.name)
            ChallengeListForCategoryViewController.ChallengeViewModel(
                andChallenge.title,
                andChallenge.description,
                andChallenge.category.color,
                andChallenge.smallImage,
                it.gemPrice,
                player.hasChallenge(it),
                it
            )
        }

    }
}