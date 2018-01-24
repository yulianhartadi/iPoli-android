package mypoli.android.pet.store

import mypoli.android.common.AppState
import mypoli.android.common.di.Module
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.*
import mypoli.android.myPoliApp
import mypoli.android.pet.PetAvatar
import mypoli.android.pet.usecase.BuyPetUseCase
import mypoli.android.player.Player
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

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
    data class BuyPet(val pet: PetAvatar) : PetStoreAction(), AsyncAction, Injects<Module> {

        private val buyPetUseCase by required { buyPetUseCase }

        override suspend fun execute(dispatcher: Dispatcher) {
            inject(myPoliApp.module(myPoliApp.instance))
            val result = buyPetUseCase.execute(pet)
            when (result) {
                is BuyPetUseCase.Result.PetBought -> {
                    dispatcher.dispatch(PetStoreAction.PetBought)
                }
                is BuyPetUseCase.Result.TooExpensive -> {
                    dispatcher.dispatch(PetStoreAction.PetTooExpensive)
                }
            }
        }
    }

    data class UnlockPet(val pet: PetAvatar) : PetStoreAction()

    data class ChangePet(val pet: PetAvatar) : PetStoreAction(), AsyncAction, Injects<Module> {

        private val changePetUseCase by required { changePetUseCase }

        override suspend fun execute(dispatcher: Dispatcher) {
            inject(myPoliApp.module(myPoliApp.instance))
            changePetUseCase.execute(pet)
        }

    }

    object Refresh : PetStoreAction()

    object PetBought : PetStoreAction()

    object PetTooExpensive : PetStoreAction()
}

data class PetStoreState(val type: StateType, val pets: List<PetModel>) : PartialState {
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

object PetStoreReducer : PartialReducer<AppState, PetStoreState, PetStoreAction> {

    override fun reduce(state: AppState, action: PetStoreAction): PetStoreState {
        val petStoreState = state.petStoreState
        return when (action) {

            PetStoreAction.Refresh -> {
                val appDataState = state.appDataState
                if (appDataState.player == null) {
                    petStoreState.copy(
                        type = PetStoreState.StateType.LOADING,
                        pets = listOf()
                    )
                } else {
                    petStoreState.copy(
                        type = PetStoreState.StateType.DATA_CHANGED,
                        pets = createPetModels(appDataState.player)
                    )
                }
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
    val type: StateType = StateType.DATA_LOADED,
    val playerGems: Int = 0,
    val petViewModels: List<PetStoreReduxPresenter.PetViewModel> = listOf()
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        PLAYER_CHANGED,
        PET_TOO_EXPENSIVE,
        PET_BOUGHT,
        PET_CHANGED,
        SHOW_GEM_STORE
    }
}