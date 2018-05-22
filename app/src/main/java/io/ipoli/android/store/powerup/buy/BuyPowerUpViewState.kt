package io.ipoli.android.store.powerup.buy

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.store.powerup.PowerUp
import io.ipoli.android.store.powerup.sideeffect.BuyPowerUpCompletedAction
import io.ipoli.android.store.powerup.usecase.BuyPowerUpUseCase

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/21/2018.
 */

sealed class BuyPowerUpAction : Action {
    object Load : BuyPowerUpAction()
    data class Buy(val powerUp: PowerUp.Type) : BuyPowerUpAction()
}

object BuyPowerUpReducer : BaseViewStateReducer<BuyPowerUpViewState>() {
    override fun reduce(
        state: AppState,
        subState: BuyPowerUpViewState,
        action: Action
    ) =
        when (action) {
            is BuyPowerUpAction.Load ->
                state.dataState.player?.let {
                    BuyPowerUpViewState.CoinsChanged(it.coins)
                } ?: BuyPowerUpViewState.Loading

            is DataLoadedAction.PlayerChanged ->
                BuyPowerUpViewState.CoinsChanged(action.player.coins)

            is BuyPowerUpCompletedAction ->
                when (action.result) {
                    is BuyPowerUpUseCase.Result.Bought ->
                        BuyPowerUpViewState.Bought(action.result.powerUp)

                    is BuyPowerUpUseCase.Result.TooExpensive ->
                        BuyPowerUpViewState.TooExpensive
                }

            else -> subState
        }

    override fun defaultState() = BuyPowerUpViewState.Loading

    override val stateKey get() = key<BuyPowerUpViewState>()
}

sealed class BuyPowerUpViewState : BaseViewState() {

    object Loading : BuyPowerUpViewState()

    data class Bought(val powerUp: PowerUp.Type) : BuyPowerUpViewState()

    object TooExpensive : BuyPowerUpViewState()

    data class CoinsChanged(val lifeCoins: Int) : BuyPowerUpViewState()
}