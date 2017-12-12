package io.ipoli.android.theme

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.player.Player
import io.ipoli.android.player.Theme
import io.ipoli.android.player.usecase.ListenForPlayerChangesUseCase
import io.ipoli.android.theme.ThemeStoreViewState.StateType.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/12/17.
 */
class ThemeStorePresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
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
                state.copy(
                    type = PLAYER_CHANGED,
                    viewModels = createThemeViewModels(intent.player)
                )
            }

            is BuyThemeIntent -> {
                state
            }

            is ChangeThemeIntent -> {
                state
            }
        }

    private fun createThemeViewModels(player: Player) =
        Theme.values().map {
            ThemeViewModel(it, player.hasTheme(it), player.currentTheme == it)
        }
}