package io.ipoli.android.theme

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/12/17.
 */
class ThemeStorePresenter(
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<ThemeStoreViewState>, ThemeStoreViewState, ThemeStoreIntent>(
    ThemeStoreViewState(ThemeStoreViewState.StateType.LOADING),
    coroutineContext
) {
    override fun reduceState(intent: ThemeStoreIntent, state: ThemeStoreViewState) =
        when (intent) {
            is LoadDataIntent -> {
                state
            }
        }

}