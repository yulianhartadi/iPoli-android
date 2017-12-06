package io.ipoli.android.pet.shop

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.Player

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/4/17.
 */
sealed class PetShopIntent : Intent

object LoadDataIntent : PetShopIntent()
data class ChangePlayerIntent(val player: Player) : PetShopIntent()
data class BuyPetIntent(val pet: PetAvatar) : PetShopIntent()
data class ChangePetIntent(val pet: PetAvatar) : PetShopIntent()

data class PetShopViewState(
    val type: StateType = StateType.DATA_LOADED,
    val petViewModels: List<PetShopViewController.PetViewModel> = listOf()
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, PLAYER_CHANGED, PET_TOO_EXPENSIVE, PET_BOUGHT, PET_CHANGED
    }
}