package mypoli.android.pet.store

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 12/4/17.
 */
sealed class PetStoreIntent : Intent

object LoadDataIntent : PetStoreIntent()
data class ChangePlayerIntent(val player: Player) : PetStoreIntent()
data class BuyPetIntent(val pet: PetAvatar) : PetStoreIntent()
data class ChangePetIntent(val pet: PetAvatar) : PetStoreIntent()

data class PetStoreViewState(
    val type: StateType = StateType.DATA_LOADED,
    val playerDiamonds: Int = 0,
    val petViewModels: List<PetStoreViewController.PetViewModel> = listOf()
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, PLAYER_CHANGED, PET_TOO_EXPENSIVE, PET_BOUGHT, PET_CHANGED
    }
}