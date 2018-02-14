package mypoli.android.repeatingquest.list

import mypoli.android.common.AppState
import mypoli.android.common.AppStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/14/18.
 */
sealed class RepeatingQuestListAction : Action

data class RepeatingQuestListState(
    val type: StateType
) : State {
    enum class StateType {
        LOADING
    }
}

object RepeatingQuestListReducer : AppStateReducer<RepeatingQuestListState> {

    override fun reduce(state: AppState, action: Action): RepeatingQuestListState {
        return state.repeatingQuestListState
    }

    override fun defaultState() =
        RepeatingQuestListState(
            type = RepeatingQuestListState.StateType.LOADING
        )

}

data class RepeatingQuestListViewState(
    val type: RepeatingQuestListState.StateType
) : ViewState