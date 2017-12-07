package io.ipoli.android.pet.store

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.player.Player

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/4/17.
 */
sealed class PetStoreIntent : Intent

object LoadDataIntent : PetStoreIntent()
data class ChangePlayerIntent(val player: Player) : PetStoreIntent()
data class BuyPetIntent(val pet: PetAvatar) : PetStoreIntent()
data class ChangePetIntent(val pet: PetAvatar) : PetStoreIntent()

data class PetStoreViewState(
    val type: StateType = StateType.DATA_LOADED,
    val petViewModels: List<PetStoreViewController.PetViewModel> = listOf()
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, PLAYER_CHANGED, PET_TOO_EXPENSIVE, PET_BOUGHT, PET_CHANGED
    }
}