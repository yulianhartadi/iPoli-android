package mypoli.android.repeatingquest.show

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/21/2018.
 */

sealed class RepeatingQuestAction : Action {

}

sealed class RepeatingQuestViewState : ViewState {

    object Loading : RepeatingQuestViewState()

    data class Changed(val name: String) : RepeatingQuestViewState()
}

object RepeatingQuestReducer : BaseViewStateReducer<RepeatingQuestViewState>() {

    override val stateKey = key<RepeatingQuestViewState>()

    override fun reduce(
        state: AppState,
        subState: RepeatingQuestViewState,
        action: Action
    ): RepeatingQuestViewState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun defaultState() = RepeatingQuestViewState.Loading
}