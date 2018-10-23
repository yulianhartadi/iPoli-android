package io.ipoli.android.player.sideeffect

import com.google.firebase.auth.FirebaseAuth
import io.ipoli.android.achievement.usecase.CreateAchievementItemsUseCase
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.ErrorLogger
import io.ipoli.android.common.redux.Action
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.usecase.SavePostReactionUseCase
import io.ipoli.android.player.attribute.AttributeListAction
import io.ipoli.android.player.attribute.usecase.AddTagToAttributeUseCase
import io.ipoli.android.player.attribute.usecase.RemoveTagFromAttributeUseCase
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.profile.EditProfileAction
import io.ipoli.android.player.profile.ProfileAction
import io.ipoli.android.player.profile.ProfileReducer
import io.ipoli.android.player.profile.ProfileViewState
import io.ipoli.android.player.usecase.FindAverageFocusedDurationForPeriodUseCase
import io.ipoli.android.player.usecase.SaveProfileUseCase
import kotlinx.coroutines.experimental.channels.Channel
import space.traversal.kapsule.required

object ProfileSideEffectHandler : AppSideEffectHandler() {

    private val findAverageFocusedDurationForPeriodUseCase by required { findAverageFocusedDurationForPeriodUseCase }
    private val saveProfileUseCase by required { saveProfileUseCase }
    private val createAchievementItemsUseCase by required { createAchievementItemsUseCase }
    private val friendRepository by required { friendRepository }
    private val playerRepository by required { playerRepository }
    private val postRepository by required { postRepository }
    private val challengeRepository by required { challengeRepository }
    private val savePostReactionUseCase by required { savePostReactionUseCase }
    private val addTagToAttributeUseCase by required { addTagToAttributeUseCase }
    private val removeTagFromAttributeUseCase by required { removeTagFromAttributeUseCase }
    private val internetConnectionChecker by required { internetConnectionChecker }

    private var postsChangedChannel: Channel<List<Post>>? = null

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is ProfileAction.Load ->
                if (action.friendId != null) {
                    updateFriendData(action.friendId)
                } else {
                    state.dataState.player?.let {
                        updateProfileData(it)
                    }
                }

            is DataLoadedAction.PlayerChanged ->
                if (state.hasState(ProfileReducer.PROFILE_KEY))
                    updateProfileData(action.player)

            is EditProfileAction.Save ->
                saveProfileUseCase.execute(
                    SaveProfileUseCase.Params(
                        displayName = action.displayName,
                        bio = action.bio
                    )
                )

            is ProfileAction.LoadFollowers -> {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null && action.playerId == null) {
                    dispatch(ProfileAction.ShowRequireLogin)
                    return
                }

                if (!internetConnectionChecker.isConnected()) {
                    dispatch(ProfileAction.NoInternetConnection)
                    return
                }
                try {
                    val followers =
                        friendRepository.findFollowers(action.playerId ?: currentUser!!.uid)
                    dispatch(DataLoadedAction.FollowersChanged(followers))
                } catch (e: Throwable) {
                    ErrorLogger.log(e)
                    dispatch(ProfileAction.ErrorLoadingFriends)
                }
            }

            is ProfileAction.LoadFollowing -> {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null && action.playerId == null) {
                    dispatch(ProfileAction.ShowRequireLogin)
                    return
                }

                if (!internetConnectionChecker.isConnected()) {
                    dispatch(ProfileAction.NoInternetConnection)
                    return
                }
                try {
                    val followers =
                        friendRepository.findFollowing(action.playerId ?: currentUser!!.uid)
                    dispatch(DataLoadedAction.FollowingChanged(followers))
                } catch (e: Throwable) {
                    ErrorLogger.log(e)
                    dispatch(ProfileAction.ErrorLoadingFriends)
                }
            }

            is ProfileAction.LoadPosts -> {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null && action.playerId == null) {
                    dispatch(ProfileAction.ShowRequireLogin)
                    return
                }

                if (!internetConnectionChecker.isConnected()) {
                    dispatch(ProfileAction.NoInternetConnection)
                    return
                }

                val playerId = action.playerId ?: FirebaseAuth.getInstance().currentUser!!.uid

                listenForChanges(
                    oldChannel = postsChangedChannel,
                    channelCreator = {
                        postsChangedChannel = postRepository.listenForPlayer(playerId, limit = 50)
                        postsChangedChannel!!
                    },
                    onResult = { posts ->
                        dispatch(DataLoadedAction.PostsChanged(posts))
                    }
                )

            }

            is ProfileAction.UnloadPosts ->
                postsChangedChannel?.close()

            is ProfileAction.SaveDescription -> {
                postRepository.saveDescription(action.postId, action.description)
            }

            is ProfileAction.ReactToPost -> {
                val s: ProfileViewState = state.stateFor(
                    if (action.friendId == null) ProfileReducer.PROFILE_KEY
                    else ProfileReducer.FRIEND_KEY
                )

                savePostReactionUseCase.execute(
                    SavePostReactionUseCase.Params(
                        postId = s.currentPostId!!,
                        reactionType = action.reaction
                    )
                )
            }

            is ProfileAction.LoadFriendChallenges -> {
                dispatch(
                    DataLoadedAction.FriendChallengesChanged(
                        challengeRepository.findForFriend(
                            action.friendId
                        )
                    )
                )
            }

            is AttributeListAction.AddTag ->
                addTagToAttributeUseCase.execute(
                    AddTagToAttributeUseCase.Params(
                        action.attributeType,
                        action.tag
                    )
                )

            is AttributeListAction.RemoveTag ->
                removeTagFromAttributeUseCase.execute(
                    RemoveTagFromAttributeUseCase.Params(
                        action.attributeType,
                        action.tag
                    )
                )

            is ProfileAction.Follow ->
                friendRepository.follow(action.friendId)

            is ProfileAction.Unfollow ->
                friendRepository.unfollow(action.friendId)
        }
    }

    private fun updateProfileData(player: Player) {
        val ai = createAchievementItemsUseCase.execute(
            CreateAchievementItemsUseCase.Params(player)
        )
            .filter {
                it is CreateAchievementItemsUseCase.AchievementListItem.UnlockedItem || hasUnlockedAtLeast1Level(
                    it
                )
            }
            .map {
                when (it) {
                    is CreateAchievementItemsUseCase.AchievementListItem.UnlockedItem ->
                        it.achievementItem

                    is CreateAchievementItemsUseCase.AchievementListItem.LockedItem ->
                        it.achievementItem

                    else -> throw IllegalArgumentException("Unknown achievement type $it")
                }
            }
        dispatch(
            DataLoadedAction.ProfileDataChanged(
                player,
                ai,
                player.statistics.dailyChallengeCompleteStreak.count.toInt(),
                findAverageFocusedDurationForPeriodUseCase.execute(
                    FindAverageFocusedDurationForPeriodUseCase.Params(dayPeriod = 7)
                )
            )
        )
    }

    private fun updateFriendData(friendId: String) {
        val friend = playerRepository.findFriend(friendId)
        val isCurrentPlayerGuest = FirebaseAuth.getInstance().currentUser == null

        val ai = createAchievementItemsUseCase.execute(
            CreateAchievementItemsUseCase.Params(friend)
        )
            .filter {
                it is CreateAchievementItemsUseCase.AchievementListItem.UnlockedItem || hasUnlockedAtLeast1Level(
                    it
                )
            }
            .map {
                when (it) {
                    is CreateAchievementItemsUseCase.AchievementListItem.UnlockedItem ->
                        it.achievementItem

                    is CreateAchievementItemsUseCase.AchievementListItem.LockedItem ->
                        it.achievementItem

                    else -> throw IllegalArgumentException("Unknown achievement type $it")
                }
            }

        dispatch(
            DataLoadedAction.FriendDataChanged(
                player = friend,
                unlockedAchievements = ai,
                streak = friend.statistics.dailyChallengeCompleteStreak.count.toInt(),
                averageProductiveDuration = findAverageFocusedDurationForPeriodUseCase.execute(
                    FindAverageFocusedDurationForPeriodUseCase.Params(
                        dayPeriod = 7,
                        friendId = friendId
                    )
                ),
                isCurrentPlayerGuest = isCurrentPlayerGuest,
                isFollowing = if (isCurrentPlayerGuest) false else friendRepository.isFollowing(
                    friendId
                ),
                isFollower = if (isCurrentPlayerGuest) false else friendRepository.isFollower(
                    friendId
                )
            )
        )
    }

    private fun hasUnlockedAtLeast1Level(it: CreateAchievementItemsUseCase.AchievementListItem) =
        it is CreateAchievementItemsUseCase.AchievementListItem.LockedItem
            && it.achievementItem.isMultiLevel
            && it.achievementItem.currentLevel >= 1

    override fun canHandle(action: Action) =
        action is ProfileAction
            || action is DataLoadedAction.PlayerChanged
            || action is EditProfileAction
            || action is AttributeListAction
}