package io.ipoli.android.repeatingquest.list

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.repeatingquest.list.RepeatingQuestListViewState.StateType.CHANGED
import io.ipoli.android.repeatingquest.list.RepeatingQuestListViewState.StateType.LOADING

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
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
        repeatingQuests: List<RepeatingQuest>?
    ) =
        if (repeatingQuests == null) {
            subState.copy(type = LOADING)
        } else {
            subState.copy(
                type = CHANGED,
                repeatingQuests = repeatingQuests,
                showEmptyView = repeatingQuests.isEmpty()
            )
        }

    override fun defaultState() =
        RepeatingQuestListViewState(
            type = LOADING,
            repeatingQuests = null,
            showEmptyView = false
        )

}

data class RepeatingQuestListViewState(
    val type: RepeatingQuestListViewState.StateType,
    val repeatingQuests: List<RepeatingQuest>?,
    val showEmptyView: Boolean
) : BaseViewState() {
    enum class StateType {
        LOADING,
        CHANGED
    }
}