package mypoli.android.store

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.pet.PetAvatar
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import mypoli.android.store.GemStoreIntent.ChangePlayer
import mypoli.android.store.GemStoreIntent.GemPacksLoaded
import mypoli.android.store.GemStoreViewState.StateType.*
import mypoli.android.store.purchase.InAppPurchaseManager
import mypoli.android.store.usecase.PurchaseGemPackUseCase
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 27.12.17.
 */
class GemStorePresenter(
    private val purchaseGemPackUseCase: PurchaseGemPackUseCase,
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<GemStoreViewState>, GemStoreViewState, GemStoreIntent>(
    GemStoreViewState(LOADING),
    coroutineContext
) {

    lateinit var purchaseManager: InAppPurchaseManager

    override fun reduceState(intent: GemStoreIntent, state: GemStoreViewState) =
        when (intent) {
            is GemStoreIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(GemStoreIntent.ChangePlayer(it))
                    }
                }
                purchaseManager.loadAll {
                    launch {
                        sendChannel.send(GemStoreIntent.GemPacksLoaded(it))
                    }
                }
                state
            }

            is GemPacksLoaded -> {
                state.copy(
                    type = GEM_PACKS_LOADED,
                    gemPacks = intent.gemPacks
                )
            }

            is ChangePlayer -> {
                val player = intent.player
                state.copy(
                    type = PLAYER_CHANGED,
                    playerGems = player.gems,
                    isGiftPurchased = player.hasPet(PetAvatar.DOG)
                )
            }

            is GemStoreIntent.BuyGemPack -> {
                purchaseManager.purchase(intent.gemPack.type, object : InAppPurchaseManager.PurchaseListener {
                    override fun onPurchased() {
                        val result = purchaseGemPackUseCase.execute(PurchaseGemPackUseCase.Params(intent.gemPack))
                        launch {
                            sendChannel.send(
                                GemStoreIntent.GemPackPurchased(result.hasUnlockedPet)
                            )
                        }
                    }

                    override fun onError() {
                        launch {
                            sendChannel.send(GemStoreIntent.PurchaseFailed)
                        }
                    }
                })
                state
            }

            is GemStoreIntent.GemPackPurchased -> {
                val type = if (intent.dogUnlocked) DOG_UNLOCKED else GEM_PACK_PURCHASED
                state.copy(
                    type = type
                )
            }

            is GemStoreIntent.PurchaseFailed -> {
                state.copy(
                    type = PURCHASE_FAILED
                )
            }
        }
}