package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.entity.SharingPreference
import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.friends.usecase.SavePostsUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/14/18.
 */
class CompleteChallengeUseCase(
    private val challengeRepository: ChallengeRepository,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val playerRepository: PlayerRepository,
    private val savePostsUseCase: SavePostsUseCase
) :
    UseCase<CompleteChallengeUseCase.Params, Challenge> {

    override fun execute(parameters: Params): Challenge {
        val challengeId = parameters.challengeId
        require(challengeId.isNotEmpty())
        val challenge = challengeRepository.findById(challengeId)!!

        val player = playerRepository.find()!!

        val completeChallenge = challenge.copy(
            completedAtDate = LocalDate.now(),
            completedAtTime = Time.now()
        )
        val reward = rewardPlayerUseCase.execute(
            RewardPlayerUseCase.Params.ForChallenge(
                completeChallenge,
                player
            )
        ).reward

        val c = challengeRepository.save(completeChallenge.copy(reward = reward))

        if (c.sharingPreference == SharingPreference.FRIENDS) {
            savePostsUseCase.execute(SavePostsUseCase.Params.ChallengeComplete(c, player))
        }

        return c
    }

    data class Params(val challengeId: String, val randomSeed: Long? = null)
}