package mypoli.android.repeatingquest.edit

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/26/18.
 */

sealed class EditRepeatingQuestAction : Action {
    data class Load(val repeatingQuestId : String) : EditRepeatingQuestAction()
}


object EditRepeatingQuestReducer : BaseViewStateReducer<EditRepeatingQuestViewState>() {

    override val stateKey = key<EditRepeatingQuestViewState>()

    override fun reduce(
        state: AppState,
        subState: EditRepeatingQuestViewState,
        action: Action
    ) =
        when (action) {
            is EditRepeatingQuestAction.Load -> {
                subState.copy(
                    type = EditRepeatingQuestViewState.StateType.CHANGED
                )
            }

            else -> subState
        }

    override fun defaultState() =
        EditRepeatingQuestViewState(
            type = EditRepeatingQuestViewState.StateType.LOADING
        )

}


data class EditRepeatingQuestViewState(
    val type: EditRepeatingQuestViewState.StateType
) : ViewState {
    enum class StateType {
        LOADING,
        CHANGED
    }
}