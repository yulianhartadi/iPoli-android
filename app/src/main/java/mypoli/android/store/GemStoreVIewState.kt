package mypoli.android.store

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.player.Player

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 27.12.17.
 */
sealed class GemStoreIntent : Intent {
    object LoadData : GemStoreIntent()
    data class ChangePlayer(val player: Player) : GemStoreIntent()
}

data class GemStoreViewState(
    val type: StateType = StateType.DATA_LOADED,
    val playerGems: Int = 0
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        SHOW_CURRENCY_CONVERTER,
        PLAYER_CHANGED
    }
}