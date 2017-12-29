package mypoli.android.store.theme

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.player.Player
import mypoli.android.player.Theme
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import mypoli.android.store.theme.ThemeStoreViewState.StateType.*
import mypoli.android.store.theme.usecase.BuyThemeUseCase
import mypoli.android.store.theme.usecase.BuyThemeUseCase.Result.ThemeBought
import mypoli.android.store.theme.usecase.BuyThemeUseCase.Result.TooExpensive
import mypoli.android.store.theme.usecase.ChangeThemeUseCase
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/12/17.
 */
class ThemeStorePresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    private val changeThemeUseCase: ChangeThemeUseCase,
    private val buyThemeUseCase: BuyThemeUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<ThemeStoreViewState>, ThemeStoreViewState, ThemeStoreIntent>(
    ThemeStoreViewState(LOADING),
    coroutineContext
) {

    override fun reduceState(intent: ThemeStoreIntent, state: ThemeStoreViewState) =
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
                val player = intent.player
                state.copy(
                    type = PLAYER_CHANGED,
                    theme = player.currentTheme,
                    playerGems = player.gems,
                    viewModels = createThemeViewModels(player)
                )
            }

            is BuyThemeIntent -> {
                val result = buyThemeUseCase.execute(intent.theme)
                when (result) {
                    is TooExpensive -> state.copy(
                        type = THEME_TOO_EXPENSIVE
                    )
                    is ThemeBought -> state.copy(
                        type = THEME_BOUGHT
                    )
                }
            }

            is ChangeThemeIntent -> {
                changeThemeUseCase.execute(intent.theme)
                state.copy(
                    type = THEME_CHANGED,
                    theme = intent.theme
                )
            }

            is ShowCurrencyConverter -> {
                state.copy(
                    type = SHOW_CURRENCY_CONVERTER
                )
            }
        }

    private fun createThemeViewModels(player: Player) =
        Theme.values().map {
            ThemeViewModel(it, player.hasTheme(it), player.currentTheme == it)
        }
}