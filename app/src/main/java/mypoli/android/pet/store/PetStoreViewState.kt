package mypoli.android.pet.store

import mypoli.android.common.AppState
import mypoli.android.common.AppStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.pet.PetAvatar
import mypoli.android.player.Player

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 12/4/17.
 */
sealed class PetStoreIntent : Intent {
    object LoadData : PetStoreIntent()
    data class ChangePlayer(val player: Player) : PetStoreIntent()
    data class BuyPet(val pet: PetAvatar) : PetStoreIntent()
    data class UnlockPet(val pet: PetAvatar) : PetStoreIntent()
    data class ChangePet(val pet: PetAvatar) : PetStoreIntent()
}

sealed class PetStoreAction : Action {
    data class BuyPet(val pet: PetAvatar) : PetStoreAction()

    data class UnlockPet(val pet: PetAvatar) : PetStoreAction()

    data class ChangePet(val pet: PetAvatar) : PetStoreAction()

    object PetBought : PetStoreAction()

    object PetTooExpensive : PetStoreAction()
}

data class PetStoreState(val type: StateType, val pets: List<PetModel>) : State {
    data class PetModel(
        val avatar: PetAvatar,
        val isBought: Boolean,
        val isCurrent: Boolean,
        val isLocked: Boolean
    )

    enum class StateType {
        LOADING,
        DATA_CHANGED,
        PET_TOO_EXPENSIVE,
        PET_BOUGHT,
        PET_CHANGED,
        SHOW_GEM_STORE
    }
}

object PetStoreReducer : AppStateReducer<PetStoreState> {

    override fun reduce(state: AppState, action: Action): PetStoreState {
        val petStoreState = state.petStoreState
        return when (action) {
            is DataLoadedAction.PlayerChanged -> {
                val player = action.player
                petStoreState.copy(
                    type = PetStoreState.StateType.DATA_CHANGED,
                    pets = createPetModels(player)
                )
            }

            is PetStoreAction.BuyPet -> {
                petStoreState.copy(
                    type = PetStoreState.StateType.LOADING
                )
            }

            PetStoreAction.PetBought -> {
                petStoreState.copy(
                    type = PetStoreState.StateType.PET_BOUGHT
                )
            }

            PetStoreAction.PetTooExpensive -> {
                petStoreState.copy(
                    type = PetStoreState.StateType.PET_TOO_EXPENSIVE
                )
            }

            is PetStoreAction.UnlockPet -> {
                petStoreState.copy(
                    type = PetStoreState.StateType.SHOW_GEM_STORE
                )
            }
            is PetStoreAction.ChangePet -> {
                petStoreState.copy(
                    type = PetStoreState.StateType.LOADING
                )
            }
            else -> petStoreState
        }
    }

    private fun createPetModels(player: Player) =
        PetAvatar.values().map {
            PetStoreState.PetModel(
                avatar = it,
                isBought = player.hasPet(it),
                isCurrent = player.pet.avatar == it,
                isLocked = (it == PetAvatar.DOG && !player.hasPet(PetAvatar.DOG))
            )
        }

    override fun defaultState() = PetStoreState(PetStoreState.StateType.LOADING, listOf())
}

data class PetStoreViewState(
    val type: StateType = StateType.DATA_CHANGED,
    val playerGems: Int = 0,
    val petViewModels: List<PetStorePresenter.PetViewModel> = listOf()
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_CHANGED,
        PET_TOO_EXPENSIVE,
        PET_BOUGHT,
        PET_CHANGED,
        SHOW_GEM_STORE
    }
}