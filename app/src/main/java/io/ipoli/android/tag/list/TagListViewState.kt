package io.ipoli.android.tag.list

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.tag.Tag

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/03/2018.
 */
sealed class TagListAction : Action {
    data class Favorite(val tag: Tag) : TagListAction() {
        override fun toMap() = mapOf("tag" to tag.name)
    }

    data class Unfavorite(val tag: Tag) : TagListAction() {
        override fun toMap() = mapOf("tag" to tag.name)
    }

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
                subState.copy(
                    type = TagListViewState.StateType.EMPTY
                )
            } else {
                subState.copy(
                    type = TagListViewState.StateType.DATA_CHANGED,
                    tags = dataState.tags
                )
            }
        }

        is DataLoadedAction.TagsChanged -> {
            if (action.tags.isEmpty()) {
                subState.copy(
                    type = TagListViewState.StateType.EMPTY
                )
            } else {
                subState.copy(
                    type = TagListViewState.StateType.DATA_CHANGED,
                    tags = action.tags
                )
            }
        }

        TagListAction.AddTag ->
            subState.copy(
                type = TagListViewState.StateType.SHOW_ADD
            )

        else ->
            subState
    }

    override fun defaultState() =
        TagListViewState(
            type = TagListViewState.StateType.LOADING,
            tags = emptyList()
        )

    override val stateKey = key<TagListViewState>()

}

data class TagListViewState(val type: StateType, val tags: List<Tag>) : BaseViewState() {

    enum class StateType { LOADING, DATA_CHANGED, EMPTY, SHOW_ADD }
}