package io.ipoli.android.friends.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.persistence.FriendRepository
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/20/18.
 */
class CreateReactionHistoryItemsUseCase(
    private val playerRepository: PlayerRepository,
    private val friendsRepository: FriendRepository
) : UseCase<CreateReactionHistoryItemsUseCase.Params, List<CreateReactionHistoryItemsUseCase.ReactionHistoryItem>> {

    override fun execute(parameters: Params): List<ReactionHistoryItem> {
        val playerIdToReaction = parameters.reactions.map { it.playerId to it }.toMap()
        val players = playerRepository.findAll(parameters.reactions.map { it.playerId })
        val playerIdToPlayer = players.map { it.id to it }.toMap()

        return parameters.reactions.sortedByDescending { it.createdAt }.map {
            ReactionHistoryItem(
                reaction = playerIdToReaction[it.playerId]!!,
                player = playerIdToPlayer[it.playerId]!!
            )
        }
    }

    data class Params(val reactions: List<Post.Reaction>)

    data class ReactionHistoryItem(
        val reaction: Post.Reaction,
        val player: Player
    )
}