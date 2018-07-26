package io.ipoli.android.friends.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.persistence.Friend
import io.ipoli.android.friends.persistence.FriendRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/20/18.
 */
class CreateReactionHistoryItemsUseCase(
    private val friendsRepository: FriendRepository
) : UseCase<CreateReactionHistoryItemsUseCase.Params, List<CreateReactionHistoryItemsUseCase.ReactionHistoryItem>> {

    override fun execute(parameters: Params): List<ReactionHistoryItem> {
        val friendIdToReaction = parameters.reactions.map { it.playerId to it }.toMap()
        val friends = friendsRepository.findAll(parameters.reactions.map { it.playerId })
        val friendIdToFriend = friends.map { it.id to it }.toMap()

        return parameters.reactions.sortedByDescending { it.createdAt }.map {
            ReactionHistoryItem(friendIdToReaction[it.playerId]!!, friendIdToFriend[it.playerId]!!)
        }
    }

    data class Params(val reactions: List<Post.Reaction>)

    data class ReactionHistoryItem(
        val reaction: Post.Reaction,
        val friend: Friend
    )
}