package io.ipoli.android.pet.store

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.pet.store.PetStoreViewState.StateType.*
import io.ipoli.android.pet.usecase.BuyPetUseCase
import io.ipoli.android.pet.usecase.ChangePetUseCase
import io.ipoli.android.player.Player
import io.ipoli.android.player.usecase.ListenForPlayerChangesUseCase
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
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
            is LoadDataIntent -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(ChangePlayerIntent(it))
                    }
                }
                state.copy(
                    type = DATA_LOADED
                )
            }

            is ChangePlayerIntent -> {
                state.copy(
                    type = PLAYER_CHANGED,
                    petViewModels = createPetViewModels(intent.player)
                )
            }

            is BuyPetIntent -> {
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

            is ChangePetIntent -> {
                changePetUseCase.execute(intent.pet)
                state.copy(
                    type = PET_CHANGED
                )
            }
        }

    private fun createPetViewModels(player: Player) =
        AndroidPetAvatar.values().map {
            val petAvatar = PetAvatar.valueOf(it.name)
            PetStoreViewController.PetViewModel(it, player.hasPet(petAvatar), player.pet.avatar == petAvatar)
        }
}
