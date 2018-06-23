package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.player.persistence.PlayerRepository
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/14/18.
 */
class CompleteChallengeUseCase(
    private val challengeRepository: ChallengeRepository,
    private val playerRepository: PlayerRepository
) :
    UseCase<CompleteChallengeUseCase.Params, Challenge> {

    override fun execute(parameters: Params): Challenge {
        val challengeId = parameters.challengeId
        require(challengeId.isNotEmpty())
        val challenge = challengeRepository.findById(challengeId)!!

        val pet = playerRepository.find()!!.pet

        val experience = experience(pet.experienceBonus)
        val coins = coins(pet.coinBonus)
        val newChallenge = challenge.copy(
            completedAtDate = LocalDate.now(),
            completedAtTime = Time.now(),
            experience = experience,
            coins = coins
        )

        challengeRepository.save(newChallenge)

        return newChallenge
    }

    data class Params(val challengeId: String, val randomSeed: Long? = null)

    private fun coins(coinBonusPercentage: Float, randomSeed: Long? = null): Int {
        val rewards = intArrayOf(10, 15, 25, 40, 50)
        val bonusCoef = (100 + coinBonusPercentage) / 100
        val reward = rewards[createRandom(randomSeed).nextInt(rewards.size)]
        return (reward * bonusCoef).toInt()
    }

    private fun experience(xpBonusPercentage: Float, randomSeed: Long? = null): Int {
        val rewards = intArrayOf(20, 30, 40, 50, 60)
        val bonusCoef = (100 + xpBonusPercentage) / 100
        val reward = rewards[createRandom(randomSeed).nextInt(rewards.size)]
        return (reward * bonusCoef).toInt()
    }

    private fun createRandom(randomSeed: Long?): Random {
        val random = Random()
        randomSeed?.let { random.setSeed(it) }
        return random
    }
}