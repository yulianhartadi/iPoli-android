package io.ipoli.android.tag.list

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.tag.Tag

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/03/2018.
 */
sealed class TagListAction : Action {
    data class Favorite(val tag: Tag) : TagListAction()
    data class Unfavorite(val tag: Tag) : TagListAction()

    object Load : TagListAction()
    object AddTag : TagListAction()
}

object TagListReducer : BaseViewStateReducer<TagListViewState>() {

    override fun reduce(
        state: AppState,
        subState: TagListViewState,
        action: Action
    ) = when (action) {

        TagListAction.Load -> {
            val dataState = state.dataState

            if (dataState.tags.isEmpty()) {
                TagListViewState.Empty
            } else {
                TagListViewState.Changed(dataState.tags)
            }
        }

        is DataLoadedAction.TagsChanged -> {
            if (action.tags.isEmpty()) {
                TagListViewState.Empty
            } else {
                TagListViewState.Changed(action.tags)
            }
        }

        TagListAction.AddTag ->
            TagListViewState.ShowAdd

        else ->
            subState
    }

    override fun defaultState() = TagListViewState.Loading

    override val stateKey = key<TagListViewState>()

}

sealed class TagListViewState : BaseViewState() {
    object Loading : TagListViewState()
    data class Changed(val tags: List<Tag>) : TagListViewState()
    object Empty : TagListViewState()
    object ShowAdd : TagListViewState()
}