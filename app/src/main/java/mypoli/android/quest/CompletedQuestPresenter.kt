package mypoli.android.quest

import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.quest.CompletedQuestViewState.StateType.LOADING
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/18.
 */
class CompletedQuestPresenter(
    private val coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<CompletedQuestViewState>, CompletedQuestViewState, CompletedQuestIntent>(
    CompletedQuestViewState(LOADING),
    coroutineContext
) {
    override fun reduceState(
        intent: CompletedQuestIntent,
        state: CompletedQuestViewState
    ): CompletedQuestViewState =
        when (intent) {
            is CompletedQuestIntent.LoadData -> {
                state
            }
        }

}