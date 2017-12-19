package mypoli.android.home

import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.home.HomeViewState.StateType.DATA_LOADED
import mypoli.android.home.HomeViewState.StateType.LOADING
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
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