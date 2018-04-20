package io.ipoli.android.quest.inbox

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Quest

sealed class InboxAction : Action {

}

object InboxReducer : BaseViewStateReducer<InboxViewState>() {

    override fun reduce(state: AppState, subState: InboxViewState, action: Action): InboxViewState {
        return subState
    }

    override fun defaultState() = InboxViewState.Loading

    override val stateKey = key<InboxViewState>()

}

sealed class InboxViewState : ViewState {
    object Loading : InboxViewState()
    data class Changed(val quests: List<Quest>) : InboxViewState()
}