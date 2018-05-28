package io.ipoli.android.quest.bucketlist

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.bucketlist.usecase.CreateBucketListItemsUseCase

sealed class BucketListAction : Action {
    data class ItemsChanged(val items: List<CreateBucketListItemsUseCase.BucketListItem>) :
        BucketListAction()

    data class CompleteQuest(val questId: String) : BucketListAction()
    data class ScheduleForToday(val questId: String) : BucketListAction()

    object Load : BucketListAction()
}

object BucketListReducer : BaseViewStateReducer<BucketListViewState>() {

    override val stateKey = key<BucketListViewState>()

    override fun reduce(
        state: AppState,
        subState: BucketListViewState,
        action: Action
    ) = when (action) {

        is BucketListAction.Load ->
            subState.copy(type = BucketListViewState.StateType.LOADING)

        is BucketListAction.ItemsChanged ->
            if (action.items.isEmpty()) {
                subState.copy(type = BucketListViewState.StateType.EMPTY)
            } else {
                subState.copy(
                    type = BucketListViewState.StateType.DATA_CHANGED,
                    items = action.items
                )
            }

        else -> subState
    }

    override fun defaultState() =
        BucketListViewState(type = BucketListViewState.StateType.LOADING, items = emptyList())
}

data class BucketListViewState(
    val type: StateType,
    val items: List<CreateBucketListItemsUseCase.BucketListItem>
) : BaseViewState() {

    enum class StateType { LOADING, DATA_CHANGED, EMPTY }
}