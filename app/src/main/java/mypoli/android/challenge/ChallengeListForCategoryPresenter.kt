package mypoli.android.challenge

import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/30/17.
 */
class ChallengeListForCategoryPresenter(coroutineContext: CoroutineContext) :
    BaseMviPresenter<ViewStateRenderer<ChallengeListForCategoryViewState>, ChallengeListForCategoryViewState, ChallengeListForCategoryIntent>(
        ChallengeListForCategoryViewState(type = ChallengeListForCategoryViewState.StateType.DATA_LOADED),
        coroutineContext
    ) {
    override fun reduceState(intent: ChallengeListForCategoryIntent, state: ChallengeListForCategoryViewState) =
        state

}