package io.ipoli.android.quest.bucketlist

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.bucketlist.usecase.CreateBucketListItemsUseCase

sealed class BucketListAction : Action {
    data class ItemsChanged(val items: List<CreateBucketListItemsUseCase.BucketListItem>)
    object Load : BucketListAction()
}

object BucketListReducer : BaseViewStateReducer<BucketListViewState>() {

    override fun reduce(
        state: AppState,
        subState: BucketListViewState,
        action: Action
    ): BucketListViewState {
        return subState
    }

    override fun defaultState() = BucketListViewState.Loading

    override val stateKey = key<BucketListViewState>()

}

sealed class BucketListViewState : ViewState {
    object Loading : BucketListViewState()
    data class Changed(val quests: List<Quest>) : BucketListViewState()
}