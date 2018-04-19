package io.ipoli.android.store.theme

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.player.Player
import io.ipoli.android.player.Theme
import io.ipoli.android.store.theme.sideeffect.BuyThemeCompletedAction
import io.ipoli.android.store.theme.usecase.BuyThemeUseCase

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/12/17.
 */
sealed class ThemeStoreAction : Action {
    object Load : ThemeStoreAction()
    data class Buy(val theme: Theme) : ThemeStoreAction()
    data class Change(val theme: Theme) : ThemeStoreAction()
}

object ThemeStoreReducer : BaseViewStateReducer<ThemeStoreViewState>() {
    override val stateKey = key<ThemeStoreViewState>()

    override fun reduce(
        state: AppState,
        subState: ThemeStoreViewState,
        action: Action
    ) = when (action) {
        ThemeStoreAction.Load ->
            state.dataState.player?.let {
                ThemeStoreViewState.Changed(createThemes(it))
            } ?: ThemeStoreViewState.Loading

        is DataLoadedAction.PlayerChanged ->
            ThemeStoreViewState.Changed(createThemes(action.player))

        is BuyThemeCompletedAction ->
            when (action.result) {
                is BuyThemeUseCase.Result.ThemeBought ->
                    ThemeStoreViewState.ThemeBought

                is BuyThemeUseCase.Result.TooExpensive ->
                    ThemeStoreViewState.ThemeTooExpensive
            }

        is ThemeStoreAction.Change -> {
            ThemeStoreViewState.ThemeChanged(action.theme)
        }

        else -> subState
    }

    override fun defaultState() = ThemeStoreViewState.Loading

    private fun createThemes(player: Player) =
        Theme.values().map {
            when {
                player.preferences.theme == it ->
                    ThemeItem.Current(it)

                player.inventory.hasTheme(it) ->
                    ThemeItem.Bought(it)

                else -> ThemeItem.ForSale(it)
            }
        }

}

sealed class ThemeItem(open val theme: Theme) {
    data class Current(override val theme: Theme) : ThemeItem(theme)
    data class Bought(override val theme: Theme) : ThemeItem(theme)
    data class ForSale(override val theme: Theme) : ThemeItem(theme)
}

sealed class ThemeStoreViewState : ViewState {
    object Loading : ThemeStoreViewState()
    object ThemeBought : ThemeStoreViewState()
    object ThemeTooExpensive : ThemeStoreViewState()
    data class ThemeChanged(val theme: Theme) : ThemeStoreViewState()
    data class Changed(val themes: List<ThemeItem>) : ThemeStoreViewState()
}
