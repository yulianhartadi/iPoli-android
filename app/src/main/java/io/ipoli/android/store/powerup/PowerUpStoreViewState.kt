package io.ipoli.android.store.powerup

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.daysUntil
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.player.Membership
import io.ipoli.android.player.Player
import io.ipoli.android.store.powerup.sideeffect.BuyPowerUpCompletedAction
import io.ipoli.android.store.powerup.usecase.BuyPowerUpUseCase
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/15/2018.
 */

sealed class PowerUpStoreAction : Action {
    object Load : PowerUpStoreAction()
    data class Enable(val type: PowerUp.Type) : PowerUpStoreAction()
    data class SyncCalendarsSelected(val calendars: Set<Player.Preferences.SyncCalendar>) : PowerUpStoreAction()
}

object PowerUpStoreReducer : BaseViewStateReducer<PowerUpStoreViewState>() {

    override val stateKey = key<PowerUpStoreViewState>()

    override fun reduce(
        state: AppState,
        subState: PowerUpStoreViewState,
        action: Action
    ) =
        when (action) {

            PowerUpStoreAction.Load ->
                state.dataState.player?.let {
                    PowerUpStoreViewState.Changed(createPowerUps(it))
                } ?: PowerUpStoreViewState.Loading

            is DataLoadedAction.PlayerChanged ->
                PowerUpStoreViewState.Changed(createPowerUps(action.player))

            is PowerUpStoreAction.Enable ->
                PowerUpStoreViewState.Loading

            is BuyPowerUpCompletedAction ->
                when (action.result) {
                    is BuyPowerUpUseCase.Result.Bought ->
                        PowerUpStoreViewState.PowerUpBought(action.result.powerUp)
                    is BuyPowerUpUseCase.Result.TooExpensive ->
                        PowerUpStoreViewState.PowerUpTooExpensive
                }

            else -> subState
        }

    private fun createPowerUps(player: Player): List<PowerUpItem> {
        val inventory = player.inventory
        return PowerUp.Type.values().map {
            when {
                inventory.isPowerUpEnabled(it) -> {
                    val p = inventory.getPowerUp(it)!!
                    PowerUpItem.Enabled(
                        type = p.type,
                        daysUntilExpiration = LocalDate.now().daysUntil(p.expirationDate).toInt(),
                        expirationDate = p.expirationDate,
                        showExpirationDate = player.membership == Membership.NONE
                    )
                }
                else -> PowerUpItem.Disabled(
                    type = it,
                    coinPrice = it.coinPrice
                )
            }
        }
    }

    override fun defaultState() = PowerUpStoreViewState.Loading
}

sealed class PowerUpItem {
    data class Enabled(
        val type: PowerUp.Type,
        val daysUntilExpiration: Int,
        val expirationDate: LocalDate,
        val showExpirationDate: Boolean
    ) : PowerUpItem()

    data class Disabled(val type: PowerUp.Type, val coinPrice: Int) : PowerUpItem()
}

sealed class PowerUpStoreViewState : ViewState {
    object Loading : PowerUpStoreViewState()
    data class PowerUpBought(val type: PowerUp.Type) : PowerUpStoreViewState()
    object PowerUpTooExpensive : PowerUpStoreViewState()
    data class Changed(val powerUps: List<PowerUpItem>) : PowerUpStoreViewState()
}