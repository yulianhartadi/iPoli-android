package mypoli.android.pet.store

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.pet.PetAvatar
import mypoli.android.pet.store.PetStoreIntent.*
import mypoli.android.pet.store.PetStoreViewState.StateType.*
import mypoli.android.pet.usecase.BuyPetUseCase
import mypoli.android.pet.usecase.ChangePetUseCase
import mypoli.android.player.Player
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 12/4/17.
 */
class PetStorePresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    private val buyPetUseCase: BuyPetUseCase,
    private val changePetUseCase: ChangePetUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PetStoreViewState>, PetStoreViewState, PetStoreIntent>(
    PetStoreViewState(LOADING),
    coroutineContext
) {
    override fun reduceState(intent: PetStoreIntent, state: PetStoreViewState) =
        when (intent) {
            is PetStoreIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(ChangePlayer(it))
                    }
                }
                state.copy(
                    type = DATA_LOADED
                )
            }

            is ChangePlayer -> {
                state.copy(
                    type = PLAYER_CHANGED,
                    playerGems = intent.player.gems,
                    petViewModels = createPetViewModels(intent.player)
                )
            }

            is BuyPet -> {
                val result = buyPetUseCase.execute(intent.pet)
                when (result) {
                    is BuyPetUseCase.Result.TooExpensive -> state.copy(
                        type = PET_TOO_EXPENSIVE
                    )
                    else -> state.copy(
                        type = PET_BOUGHT
                    )
                }
            }

            is ChangePet -> {
                changePetUseCase.execute(intent.pet)
                state.copy(
                    type = PET_CHANGED
                )
            }

            is ShowCurrencyConverter -> {
                state.copy(
                    type = SHOW_CURRENCY_CONVERTER
                )
            }

            is UnlockPet -> {
                state.copy(
                    type = SHOW_GEM_STORE
                )
            }
        }

    private fun createPetViewModels(player: Player) =
        AndroidPetAvatar.values().map {
            val petAvatar = PetAvatar.valueOf(it.name)
            PetStoreViewController.PetViewModel(
                avatar = it,
                isBought = player.hasPet(petAvatar),
                isCurrent = player.pet.avatar == petAvatar,
                isLocked = (it == AndroidPetAvatar.DOG && !player.hasPet(PetAvatar.DOG)))
        }
}
