package mypoli.android.store

import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 27.12.17.
 */
class GemStorePresenter(
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<GemStoreViewState>, GemStoreViewState, GemStoreIntent>(
    GemStoreViewState(GemStoreViewState.StateType.LOADING),
    coroutineContext
) {

    override fun reduceState(intent: GemStoreIntent, state: GemStoreViewState): GemStoreViewState {
        return state
    }
}