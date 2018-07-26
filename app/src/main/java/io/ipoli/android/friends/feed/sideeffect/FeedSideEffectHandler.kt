package io.ipoli.android.friends.feed.sideeffect

import android.arch.paging.ItemKeyedDataSource
import android.arch.paging.PagedList
import com.google.firebase.auth.FirebaseAuth
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.entity.SharingPreference
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.ErrorLogger
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.redux.Action
import io.ipoli.android.friends.ReactionHistoryDialogAction
import io.ipoli.android.friends.feed.FeedAction
import io.ipoli.android.friends.feed.FeedViewState
import io.ipoli.android.friends.feed.PostViewModel
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.feed.persistence.PostRepository
import io.ipoli.android.friends.feed.picker.ItemToSharePickerViewState
import io.ipoli.android.friends.feed.picker.PostItemPickerAction
import io.ipoli.android.friends.usecase.CreateReactionHistoryItemsUseCase
import io.ipoli.android.friends.usecase.SavePostReactionUseCase
import io.ipoli.android.friends.usecase.SavePostsUseCase
import io.ipoli.android.quest.Quest
import kotlinx.coroutines.experimental.channels.Channel
import space.traversal.kapsule.required
import java.util.concurrent.Executor


/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/16/18.
 */
object FeedSideEffectHandler : AppSideEffectHandler() {

    private val savePostsUseCase by required { savePostsUseCase }
    private val postRepository by required { postRepository }
    private val createReactionHistoryItemsUseCase by required { createReactionHistoryItemsUseCase }
    private val executor by required { executorService }
    private val savePostReactionUseCase by required { savePostReactionUseCase }
    private val internetConnectionChecker by required { internetConnectionChecker }

    private var postsChangedChannel: Channel<Unit>? = null

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {

            is FeedAction.Load -> {
                if(FirebaseAuth.getInstance().currentUser == null) {
                    dispatch(FeedAction.RequireLogin)
                    return
                }

                if (!internetConnectionChecker.isConnected()) {
                    dispatch(FeedAction.NoInternetConnection)
                    return
                }

                listenForChanges(
                    oldChannel = postsChangedChannel,
                    channelCreator = {
                        postsChangedChannel = postRepository.listenForChange()
                        postsChangedChannel!!
                    },
                    onResult = { _ ->
                        val dataSource =
                            PostDataSource(postRepository, action.mapToViewModel)
                        val pagedList = PagedList.Builder(
                            dataSource,
                            PagedList.Config.Builder()
                                .setPageSize(10)
                                .setPrefetchDistance(10)
                                .setEnablePlaceholders(false)
                                .build()
                        )
                            .setFetchExecutor(executor)
                            .setNotifyExecutor(CurrentThreadExecutor())
                            .build()
                        dispatch(DataLoadedAction.PostsChanged(pagedList))
                    }
                )
            }

            is FeedAction.Unload -> {
                postsChangedChannel?.close()
            }

            is PostItemPickerAction.Share -> {
                val s = state.stateFor(ItemToSharePickerViewState::class.java)
                val quests = s.quests?.filter { s.selectedQuestIds.contains(it.id) }
                quests?.let {
                    savePostsUseCase.execute(
                        SavePostsUseCase.Params.QuestsComplete(
                            it,
                            state.dataState.player
                        )
                    )
                }

                val challenges = s.challenges?.filter { s.selectedChallengeIds.contains(it.id) }
                challenges?.let {
                    savePostsUseCase.execute(
                        SavePostsUseCase.Params.ChallengesShared(
                            it,
                            state.dataState.player
                        )
                    )
                }

            }

            is FeedAction.React -> {
                val s = state.stateFor(FeedViewState::class.java)
                savePostReactionUseCase.execute(
                    SavePostReactionUseCase.Params(
                        postPlayerId = s.currentPostPlayerId!!,
                        postId = s.currentPostId!!,
                        reactionType = action.reactionType
                    )
                )
            }

            is ReactionHistoryDialogAction.Load -> {
                dispatch(
                    DataLoadedAction.ReactionHistoryItemsChanged(
                        createReactionHistoryItemsUseCase.execute(
                            CreateReactionHistoryItemsUseCase.Params(
                                action.reactions
                            )
                        )
                    )
                )
            }

            is PostItemPickerAction.Load -> {
                var resultQuests: List<Quest>? = null
                var resultChallenges: List<Challenge>? = null

                val quests = state.dataState.todayQuests
                if (quests != null) {
                    val qs = quests.filter { it.isCompleted }
                    val res = postRepository.hasPostedQuests(qs.map { it.id })
                    resultQuests = qs.filter { res[it.id] == false }
                }
                val challenges = state.dataState.challenges
                if (challenges != null) {
                    resultChallenges =
                        challenges.filter {
                            !it.isCompleted &&
                                it.sharingPreference == SharingPreference.PRIVATE
                        }
                }

                dispatch(
                    DataLoadedAction.PostItemPickerItemsChanged(
                        resultQuests,
                        resultChallenges
                    )
                )
            }

            is DataLoadedAction.TodayQuestsChanged -> {
                val quests = state.dataState.todayQuests!!.filter { it.isCompleted }
                val res = postRepository.hasPostedQuests(quests.map { it.id })
                dispatch(
                    DataLoadedAction.PostItemPickerItemsChanged(
                        quests.filter { res[it.id] == false },
                        null
                    )
                )
            }

            is DataLoadedAction.ChallengesChanged -> {
                val challenges =
                    state.dataState.challenges!!.filter {
                        !it.isCompleted &&
                            it.sharingPreference == SharingPreference.PRIVATE
                    }
                dispatch(
                    DataLoadedAction.PostItemPickerItemsChanged(
                        null,
                        challenges
                    )
                )
            }

        }
    }

    private class CurrentThreadExecutor : Executor {

        override fun execute(command: Runnable) {
            command.run()
        }
    }

    override fun canHandle(action: Action) =
        action is PostItemPickerAction ||
            action is FeedAction ||
            action is ReactionHistoryDialogAction

    private class PostDataSource(
        private val postRepository: PostRepository,
        private val mapToViewModel: (Post) -> PostViewModel
    ) :
        ItemKeyedDataSource<Long, PostViewModel>() {
        override fun loadBefore(
            params: LoadParams<Long>,
            callback: LoadCallback<PostViewModel>
        ) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun loadInitial(
            params: LoadInitialParams<Long>,
            callback: LoadInitialCallback<PostViewModel>
        ) {
            try {
                loadInitialPosts(params, callback)
            } catch (e: Throwable) {
                ErrorLogger.log(e)
                dispatch(FeedAction.ErrorLoadingInitialFeed)
            }
        }

        private fun loadInitialPosts(
            params: LoadInitialParams<Long>,
            callback: LoadInitialCallback<PostViewModel>
        ) {
            val posts = postRepository.findForAll(params.requestedLoadSize)
            callback.onResult(posts.map(mapToViewModel), 0, posts.size)

            if (posts.isEmpty()) {
                dispatch(FeedAction.EmptyPosts)
            } else {
                dispatch(FeedAction.PostsLoaded)
            }
        }

        override fun loadAfter(
            params: LoadParams<Long>,
            callback: LoadCallback<PostViewModel>
        ) {
            try {
                val posts = postRepository.findForAll(params.requestedLoadSize, params.key.instant)
                callback.onResult(posts.map(mapToViewModel))
            } catch (e: Throwable) {
                ErrorLogger.log(e)
                dispatch(FeedAction.ErrorLoadingFeedPage)
            }
        }

        override fun getKey(item: PostViewModel) = item.createdAt

    }
}