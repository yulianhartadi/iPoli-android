package mypoli.android.store

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.player.Player
import mypoli.android.store.purchase.GemPack

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 27.12.17.
 */
sealed class GemStoreIntent : Intent {
    object LoadData : GemStoreIntent()
    data class ChangePlayer(val player: Player) : GemStoreIntent()
    data class BuyGemPack(val gemPack: GemPack) : GemStoreIntent()
    data class GemPacksLoaded(val gemPacks: List<GemPack>) : GemStoreIntent()
    data class GemPackPurchased(val dogUnlocked: Boolean) : GemStoreIntent()
    object PurchaseFailed : GemStoreIntent()
}

data class GemStoreViewState(
    val type: StateType = StateType.DATA_LOADED,
    val playerGems: Int = 0,
    val isGiftPurchased: Boolean = false,
    val gemPacks: List<GemPack> = listOf()
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        SHOW_CURRENCY_CONVERTER,
        PLAYER_CHANGED,
        GEM_PACKS_LOADED,
        DOG_UNLOCKED,
        GEM_PACK_PURCHASED,
        PURCHASE_FAILED,
        PURCHASING
    }


}