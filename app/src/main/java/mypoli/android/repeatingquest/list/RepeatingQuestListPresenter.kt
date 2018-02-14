package mypoli.android.repeatingquest.list

import android.content.Context
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/14/18.
 */
class RepeatingQuestListPresenter : AndroidStatePresenter<AppState, RepeatingQuestListViewState> {
    override fun present(state: AppState, context: Context): RepeatingQuestListViewState {
        return RepeatingQuestListViewState(
            type = state.repeatingQuestListState.type
        )
    }
}