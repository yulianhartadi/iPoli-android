package mypoli.android.repeatingquest.list

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.repeatingquest.entity.RepeatingQuest
import mypoli.android.repeatingquest.list.RepeatingQuestListViewState.StateType.CHANGED
import mypoli.android.repeatingquest.list.RepeatingQuestListViewState.StateType.LOADING

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
                createState(subState, state.dataState.repeatingQuests)
            }

            is DataLoadedAction.RepeatingQuestsChanged -> {
                createState(subState, action.repeatingQuests)
            }

            else -> subState
        }

    private fun createState(
        subState: RepeatingQuestListViewState,
        repeatingQuests: List<RepeatingQuest>
    ) =
        subState.copy(
            type = CHANGED,
            repeatingQuests = repeatingQuests,
            showEmptyView = repeatingQuests.isEmpty()
        )

    override fun defaultState() =
        RepeatingQuestListViewState(
            type = LOADING,
            repeatingQuests = listOf(),
            showEmptyView = false
        )

}

data class RepeatingQuestListViewState(
    val type: RepeatingQuestListViewState.StateType,
    val repeatingQuests: List<RepeatingQuest>,
    val showEmptyView: Boolean
) : ViewState {
    enum class StateType {
        LOADING,
        CHANGED
    }
}