package io.ipoli.android.tag.list

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/03/2018.
 */
sealed class TagListAction : Action {
    object Load : TagListAction()
}

object TagListReducer : BaseViewStateReducer<TagListViewState>() {

    override fun reduce(
        state: AppState,
        subState: TagListViewState,
        action: Action
    ): TagListViewState {
        return subState
    }

    override fun defaultState() = TagListViewState.Loading

    override val stateKey = key<TagListViewState>()

}

sealed class TagListViewState : ViewState {
    object Loading : TagListViewState()
}