package io.ipoli.android.home

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.home.HomeViewState.StateType.DATA_LOADED
import io.ipoli.android.home.HomeViewState.StateType.LOADING
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */
class HomePresenter(
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<HomeViewState>, HomeViewState, HomeIntent>(
    HomeViewState(LOADING),
    coroutineContext
) {

    override fun reduceState(intent: HomeIntent, state: HomeViewState) =
        when (intent) {
            is LoadDataIntent -> {
                state.copy(type = DATA_LOADED)
            }
        }

}