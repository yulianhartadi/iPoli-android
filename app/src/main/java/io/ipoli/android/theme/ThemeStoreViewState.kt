package io.ipoli.android.theme

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.player.Player
import io.ipoli.android.player.Theme

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/12/17.
 */
sealed class ThemeStoreIntent : Intent

object LoadDataIntent : ThemeStoreIntent()
data class ChangePlayerIntent(val player: Player) : ThemeStoreIntent()
data class BuyThemeIntent(val theme : Theme) : ThemeStoreIntent()
data class ChangeThemeIntent(val theme : Theme) : ThemeStoreIntent()

data class ThemeStoreViewState(
    val type: StateType = StateType.DATA_LOADED,
    val viewModels: List<ThemeViewModel> = listOf()
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, PLAYER_CHANGED
    }
}