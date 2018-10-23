package io.ipoli.android.friends.feed.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.feed.post.AddPostAction
import io.ipoli.android.friends.feed.post.AddPostViewState
import io.ipoli.android.friends.feed.post.PostAction
import io.ipoli.android.friends.usecase.SavePostReactionUseCase
import io.ipoli.android.friends.usecase.SavePostsUseCase
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.job.RewardScheduler
import kotlinx.coroutines.experimental.channels.Channel
import space.traversal.kapsule.required

object PostSideEffectHandler : AppSideEffectHandler() {

    private val postRepository by required { postRepository }
    private val savePostsUseCase by required { savePostsUseCase }
    private val savePostReactionUseCase by required { savePostReactionUseCase }

    private val playerRepository by required { playerRepository }
    private val challengeRepository by required { challengeRepository }
    private val habitRepository by required { habitRepository }
    private val questRepository by required { questRepository }
    private val rewardPlayerUseCase by required { rewardPlayerUseCase }
    private val rewardScheduler by required { rewardScheduler }

    private var postChangedChannel: Channel<Post>? = null

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is PostAction.Load -> {

                listenForChanges(
                    oldChannel = postChangedChannel,
                    channelCreator = {
                        postChangedChannel = postRepository.listen(action.postId)
                        postChangedChannel!!
                    },
                    onResult = { post ->
                        dispatch(DataLoadedAction.PostChanged(post))
                    }
                )
            }

            is PostAction.SaveComment -> {
                if (action.text.isNotBlank()) {
                    postRepository.saveComment(action.postId, action.text.trim())
                }
            }

            is AddPostAction.Load -> {
                val p = playerRepository.find()!!
                val c = action.challengeId?.let {
                    challengeRepository.findById(it)!!
                }
                val h = action.habitId?.let {
                    habitRepository.findById(it)!!
                }
                val q = action.questId?.let {
                    questRepository.findById(it)!!
                }
                dispatch(DataLoadedAction.AddPostDataChanged(p, q, h, c))
            }

            is AddPostAction.Save -> {
                val s = state.stateFor(AddPostViewState::class.java)
                val player = state.dataState.player
                savePostsUseCase.execute(
                    if (s.quest != null && s.quest.isFromChallenge) {
                        SavePostsUseCase.Params.QuestFromChallengeComplete(
                            quest = s.quest,
                            challenge = s.challenge!!,
                            description = action.playerMessage,
                            imageData = action.imageData,
                            player = player
                        )
                    } else if (s.quest != null) {
                        SavePostsUseCase.Params.QuestComplete(
                            quest = s.quest,
                            description = action.playerMessage,
                            imageData = action.imageData,
                            player = player
                        )
                    } else if (s.habit != null) {
                        SavePostsUseCase.Params.HabitCompleted(
                            habit = s.habit,
                            challenge = s.challenge,
                            description = action.playerMessage,
                            imageData = action.imageData,
                            player = player
                        )
                    } else if (!s.challenge!!.isCompleted) {
                        SavePostsUseCase.Params.ChallengeShared(
                            challenge = s.challenge,
                            description = action.playerMessage,
                            imageData = action.imageData,
                            player = player
                        )
                    } else {
                        SavePostsUseCase.Params.ChallengeComplete(
                            challenge = s.challenge,
                            description = action.playerMessage,
                            imageData = action.imageData,
                            player = player
                        )
                    }
                )
                val r = rewardPlayerUseCase.execute(RewardPlayerUseCase.Params.ForAddPost()).reward
                rewardScheduler.schedule(r, true, RewardScheduler.Type.ADD_POST, null)
            }

            is PostAction.Remove ->
                postRepository.delete(action.postId)

            is PostAction.React ->
                savePostReactionUseCase.execute(
                    SavePostReactionUseCase.Params(
                        postId = action.postId,
                        reactionType = action.reaction
                    )
                )
        }
    }

    override fun canHandle(action: Action) = action is PostAction || action is AddPostAction

}