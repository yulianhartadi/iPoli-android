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
                val repeatingQuests = state.dataState.repeatingQuests
                if (repeatingQuests.isNotEmpty()) {
                    subState.copy(
                        type = CHANGED,
                        repeatingQuests = repeatingQuests
                    )
                } else {
                    subState.copy(
                        type = LOADING
                    )
                }
            }

            is DataLoadedAction.RepeatingQuestsChanged -> {
                subState.copy(
                    type = CHANGED,
                    repeatingQuests = action.repeatingQuests
                )
            }

            else -> subState
    }

    override fun defaultState() =
        RepeatingQuestListViewState(
            type = LOADING,
            repeatingQuests = listOf()
        )

}

data class RepeatingQuestListViewState(
    val type: RepeatingQuestListViewState.StateType,
    val repeatingQuests: List<RepeatingQuest>
) : ViewState {
    enum class StateType {
        LOADING,
        CHANGED
    }
}
