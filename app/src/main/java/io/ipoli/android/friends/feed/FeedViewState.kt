package io.ipoli.android.friends.feed

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.friends.feed.FeedViewState.StateType.*
import io.ipoli.android.friends.feed.data.Post

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/10/18.
 */
sealed class FeedAction : Action {
    object Unload : FeedAction()
    object NoInternetConnection : FeedAction()
    object ErrorLoadingFeedPage : FeedAction()
    object ErrorLoadingInitialFeed : FeedAction()
    object InviteFriend : FeedAction()

    object Load : FeedAction()
    data class React(
        val reactionType: Post.ReactionType
    ) : FeedAction() {
        override fun toMap() = mapOf("reaction" to reactionType.name)
    }

    data class ShowReactionPopupForPost(val postId: String) : FeedAction()
}

object FeedReducer : BaseViewStateReducer<FeedViewState>() {
    override val stateKey = key<FeedViewState>()

    override fun reduce(state: AppState, subState: FeedViewState, action: Action) =
        when (action) {

            is FeedAction.Load ->
                subState.copy(
                    type = LOADING,
                    isPlayerSignedIn = state.dataState.player?.isLoggedIn()
                )

            is FeedAction.ErrorLoadingInitialFeed,
            is FeedAction.ErrorLoadingFeedPage,
            is FeedAction.NoInternetConnection ->
                subState.copy(
                    type = NO_INTERNET_CONNECTION
                )

            is DataLoadedAction.PostsChanged ->
                subState.copy(
                    type = POSTS_CHANGED,
                    posts = action.posts
                )

            is FeedAction.ShowReactionPopupForPost ->
                subState.copy(
                    type = REACTION_POPUP_SHOWN,
                    currentPostId = action.postId
                )

            is FeedAction.InviteFriend ->
                state.dataState.player?.let {
                    if (it.statistics.inviteForFriendCount <= 0) {
                        subState.copy(type = NO_INVITES_LEFT)
                    } else subState.copy(type = SHOW_INVITE_FRIEND)
                } ?: subState.copy(type = NO_INVITES_LEFT)

            else -> subState
        }

    override fun defaultState() = FeedViewState(
        type = LOADING,
        posts = null,
        currentPostId = null,
        isPlayerSignedIn = null
    )
}

data class FeedViewState(
    val type: StateType,
    val posts: List<Post>?,
    val currentPostId: String?,
    val isPlayerSignedIn: Boolean?
) : BaseViewState() {

    enum class StateType {
        LOADING,
        NO_INTERNET_CONNECTION,
        POSTS_CHANGED,
        REACTION_POPUP_SHOWN,
        NO_INVITES_LEFT,
        SHOW_INVITE_FRIEND
    }
}