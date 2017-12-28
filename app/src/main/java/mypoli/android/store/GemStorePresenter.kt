package mypoli.android.store

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import mypoli.android.store.GemStoreViewState.StateType.LOADING
import mypoli.android.store.GemStoreViewState.StateType.PLAYER_CHANGED
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 27.12.17.
 */
class GemStorePresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<GemStoreViewState>, GemStoreViewState, GemStoreIntent>(
    GemStoreViewState(LOADING),
    coroutineContext
) {

    override fun reduceState(intent: GemStoreIntent, state: GemStoreViewState) =
        when (intent) {
            is GemStoreIntent.LoadData -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(GemStoreIntent.ChangePlayer(it))
                    }
                }
                state
            }
            is GemStoreIntent.ChangePlayer -> {
                state.copy(
                    type = PLAYER_CHANGED,
                    playerGems = intent.player.gems
                )
            }
        }
}