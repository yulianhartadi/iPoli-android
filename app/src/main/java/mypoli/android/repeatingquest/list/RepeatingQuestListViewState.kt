package mypoli.android.repeatingquest.list

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.repeatingquest.entity.RepeatingQuest

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/14/18.
 */
sealed class RepeatingQuestListAction : Action {
    object LoadData : RepeatingQuestListAction()
}


object RepeatingQuestListReducer : BaseViewStateReducer<RepeatingQuestListViewState>() {

    override val stateKey = key<RepeatingQuestListViewState>()

    override fun reduce(
        state: AppState,
        subState: RepeatingQuestListViewState,
        action: Action
    ) =
        when (action) {
            RepeatingQuestListAction.LoadData -> {
                subState.copy(
                    type = RepeatingQuestListViewState.StateType.DATA_LOADED,
                    repeatingQuests = state.dataState.repeatingQuests
                )
            }

            else -> subState
    }

    override fun defaultState() =
        RepeatingQuestListViewState(
            type = RepeatingQuestListViewState.StateType.LOADING,
            repeatingQuests = listOf()
        )

}

data class RepeatingQuestListViewState(
    val type: RepeatingQuestListViewState.StateType,
    val repeatingQuests: List<RepeatingQuest>
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED
    }
}
