package mypoli.android.store.theme

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.player.Player
import mypoli.android.player.Theme

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/12/17.
 */
sealed class ThemeStoreIntent : Intent

object LoadDataIntent : ThemeStoreIntent()
data class ChangePlayerIntent(val player: Player) : ThemeStoreIntent()
data class BuyThemeIntent(val theme : Theme) : ThemeStoreIntent()
data class ChangeThemeIntent(val theme : Theme) : ThemeStoreIntent()
object ShowCurrencyConverter : ThemeStoreIntent()

data class ThemeStoreViewState(
    val type: StateType = StateType.DATA_LOADED,
    val theme: Theme? = null,
    val playerGems: Int = 0,
    val viewModels: List<ThemeViewModel> = listOf()
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        PLAYER_CHANGED,
        THEME_CHANGED,
        THEME_BOUGHT,
        THEME_TOO_EXPENSIVE,
        SHOW_CURRENCY_CONVERTER
    }
}