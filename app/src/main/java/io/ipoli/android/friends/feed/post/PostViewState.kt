package io.ipoli.android.friends.feed.post

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.feed.post.PostViewState.StateType.DATA_CHANGED
import io.ipoli.android.friends.feed.post.PostViewState.StateType.LOADING

sealed class PostAction : Action {
    data class Load(val postId: String) : PostAction()

    data class SaveComment(val postId: String, val text: String) : PostAction()
    data class Remove(val postId: String) : PostAction()
    data class React(val postId: String, val reaction: Post.ReactionType) : PostAction()
}

object PostReducer : BaseViewStateReducer<PostViewState>() {
    override val stateKey = key<PostViewState>()

    override fun reduce(state: AppState, subState: PostViewState, action: Action) =
        when (action) {

            is DataLoadedAction.PostChanged -> {
                val currentPlayerId = state.dataState.player!!.id
                subState.copy(
                    type = DATA_CHANGED,
                    post = action.post,
                    comments = action.post.comments
                        .sortedBy { it.createdAt.toEpochMilli() },
                    canDelete = action.post.playerId == currentPlayerId,
                    currentPlayerId = currentPlayerId
                )
            }

            else -> subState
        }

    override fun defaultState() = PostViewState(
        type = LOADING,
        post = null,
        comments = emptyList(),
        canDelete = false,
        currentPlayerId = null
    )
}

data class PostViewState(
    val type: StateType,
    val post: Post?,
    val comments: List<Post.Comment>,
    val canDelete: Boolean,
    val currentPlayerId : String?
) : BaseViewState() {
    enum class StateType {
        LOADING, DATA_CHANGED
    }
}