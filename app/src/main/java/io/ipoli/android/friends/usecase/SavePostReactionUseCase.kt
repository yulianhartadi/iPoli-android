package io.ipoli.android.friends.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.friends.feed.data.Post
import io.ipoli.android.friends.feed.persistence.PostRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 7/20/18.
 */
class SavePostReactionUseCase(
    private val postRepository: PostRepository
) : UseCase<SavePostReactionUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        postRepository.react(
            parameters.postPlayerId,
            parameters.postId,
            parameters.reactionType
        )
    }

    data class Params(
        val postPlayerId: String,
        val postId: String,
        val reactionType: Post.ReactionType
    )
}