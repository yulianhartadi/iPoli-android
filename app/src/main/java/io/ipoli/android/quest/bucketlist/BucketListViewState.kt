package io.ipoli.android.quest.bucketlist

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.bucketlist.usecase.CreateBucketListItemsUseCase

sealed class BucketListAction : Action {
    data class ItemsChanged(val items: List<CreateBucketListItemsUseCase.BucketListItem>) :
        BucketListAction()

    object Load : BucketListAction()
}

object BucketListReducer : BaseViewStateReducer<BucketListViewState>() {

    override val stateKey = key<BucketListViewState>()

    override fun reduce(
        state: AppState,
        subState: BucketListViewState,
        action: Action
    ) = when (action) {

        is BucketListAction.ItemsChanged ->
            BucketListViewState.Changed(action.items)

        else -> subState
    }

    override fun defaultState() = BucketListViewState.Loading
}

sealed class BucketListViewState : ViewState {
    object Loading : BucketListViewState()
    data class Changed(val quests: List<CreateBucketListItemsUseCase.BucketListItem>) :
        BucketListViewState()
}