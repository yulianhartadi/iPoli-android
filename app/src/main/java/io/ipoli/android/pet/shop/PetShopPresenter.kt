package io.ipoli.android.pet.shop

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.pet.AndroidPetAvatar
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.pet.shop.PetShopViewState.StateType.*
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
class PetShopPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    private val buyPetUseCase: BuyPetUseCase,
    private val changePetUseCase: ChangePetUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PetShopViewState>, PetShopViewState, PetShopIntent>(
    PetShopViewState(LOADING),
    coroutineContext
) {
    override fun reduceState(intent: PetShopIntent, state: PetShopViewState) =
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
            PetShopViewController.PetViewModel(it, player.hasPet(petAvatar), player.pet.avatar == petAvatar)
        }
}
