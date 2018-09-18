package io.ipoli.android.friends.feed

import android.arch.paging.PagedList
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
    object EmptyPosts : FeedAction()
    object PostsLoaded : FeedAction()
    object ErrorLoadingFeedPage : FeedAction()
    object ErrorLoadingInitialFeed : FeedAction()
    object RequireLogin : FeedAction()
    object InviteFriend : FeedAction()

    data class Load(val mapToViewModel: (Post) -> PostViewModel) : FeedAction()
    data class React(
        val reactionType: Post.ReactionType
    ) : FeedAction() {
        override fun toMap() = mapOf("reaction" to reactionType.name)
    }

    data class ShowReactionPopupForPost(val postId: String, val postPlayerId: String) : FeedAction()
}

object FeedReducer : BaseViewStateReducer<FeedViewState>() {
    override val stateKey = key<FeedViewState>()

    override fun reduce(state: AppState, subState: FeedViewState, action: Action) =
        when (action) {

            is FeedAction.Load ->
                subState.copy(
                    type = LOADING
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

            is FeedAction.PostsLoaded ->
                subState.copy(
                    type = NON_EMPTY_FEED
                )

            is FeedAction.EmptyPosts ->
                subState.copy(
                    type = EMPTY_FEED
                )

            is FeedAction.ShowReactionPopupForPost ->
                subState.copy(
                    type = REACTION_POPUP_SHOWN,
                    currentPostId = action.postId,
                    currentPostPlayerId = action.postPlayerId
                )

            is FeedAction.RequireLogin ->
                subState.copy(
                    type = SHOW_REQUIRE_LOGIN
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
        currentPostPlayerId = null
    )
}

data class FeedViewState(
    val type: StateType,
    val posts: PagedList<PostViewModel>?,
    val currentPostId: String?,
    val currentPostPlayerId: String?
) : BaseViewState() {

    enum class StateType {
        LOADING,
        NO_INTERNET_CONNECTION,
        POSTS_CHANGED,
        NON_EMPTY_FEED,
        EMPTY_FEED,
        REACTION_POPUP_SHOWN,
        SHOW_REQUIRE_LOGIN,
        NO_INVITES_LEFT,
        SHOW_INVITE_FRIEND
    }
}