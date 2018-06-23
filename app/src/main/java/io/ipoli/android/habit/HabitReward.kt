package io.ipoli.android.habit

import io.ipoli.android.common.SimpleReward
import io.ipoli.android.quest.Quest
import java.util.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/20/18.
 */
class HabitReward(private val randomSeed: Long? = null) {

    fun generate(
        coinBonusPercentage: Float,
        xpBonusPercentage: Float
    ) =
        SimpleReward(
            experience = experience(xpBonusPercentage),
            coins = coins(coinBonusPercentage),
            bounty = Quest.Bounty.None
        )

    private fun coins(coinBonusPercentage: Float): Int {
        val rewards = intArrayOf(1, 2)
        val bonusCoef = (100 + coinBonusPercentage) / 100
        val reward = rewards[createRandom().nextInt(rewards.size)]
        return (reward * bonusCoef).toInt()
    }

    private fun experience(xpBonusPercentage: Float): Int {
        val rewards = intArrayOf(1, 2, 4, 5)
        val bonusCoef = (100 + xpBonusPercentage) / 100
        val reward = rewards[createRandom().nextInt(rewards.size)]
        return (reward * bonusCoef).toInt()
    }

    private fun createRandom(): Random {
        val random = Random()
        randomSeed?.let { random.setSeed(it) }
        return random
    }
}