package mypoli.android.challenge

import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class ChallengeCategoryListPresenter(coroutineContext: CoroutineContext) :
    BaseMviPresenter<ViewStateRenderer<ChallengeCategoryListViewState>, ChallengeCategoryListViewState, ChallengeCategoryListIntent>(
        ChallengeCategoryListViewState(type = ChallengeCategoryListViewState.StateType.DATA_LOADED),
        coroutineContext
    ) {

    override fun reduceState(
        intent: ChallengeCategoryListIntent,
        state: ChallengeCategoryListViewState
    ) =
        state
}