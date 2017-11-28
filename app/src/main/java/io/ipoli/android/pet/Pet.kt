package io.ipoli.android.pet

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.pet.PetMood.*
import io.ipoli.android.quest.Quest

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 11/27/17.
 */
data class Pet(
    val name: String,
    val avatar: PetAvatar,
    val moodPoints: Int = Constants.DEFAULT_PET_HP,
    val healthPoints: Int = Constants.DEFAULT_PET_HP,
    val mood: PetMood = moodFor(healthPoints),
    val experienceBonus: Float = bonusFor(mood, Constants.MAX_PET_XP_BONUS),
    val coinBonus: Float = bonusFor(mood, Constants.MAX_PET_COIN_BONUS),
    val unlockChanceBonus: Float = bonusFor(mood, Constants.MAX_PET_UNLOCK_CHANCE_BONUS)
) {
    fun rewardFrom(quest: Quest): Pet {
        val reward = PetRewardGenerator.forQuest(quest)
        val newHealthPoints = Math.min(this.healthPoints + reward.healthPoints, Constants.MAX_PET_HP)

        val newMoodPoints = newMoodPoints(newHealthPoints, reward)

        val newMood = moodFor(newMoodPoints)

        return copy(
            healthPoints = newHealthPoints,
            moodPoints = newMoodPoints,
            mood = newMood,
            coinBonus = bonusFor(newMood, Constants.MAX_PET_COIN_BONUS),
            experienceBonus = bonusFor(newMood, Constants.MAX_PET_XP_BONUS),
            unlockChanceBonus = bonusFor(newMood, Constants.MAX_PET_UNLOCK_CHANCE_BONUS)
        )
    }

    private fun newMoodPoints(newHealthPoints: Int, reward: PetReward) =
        if (newHealthPoints <= 0.2 * Constants.MAX_PET_HP) {
            Math.min(moodPoints, GOOD_MIN_MOOD_POINTS - 1)
        } else {
            val moodBonusMultiplier = if (newHealthPoints >= 0.9 * Constants.MAX_PET_HP) 2 else 1
            Math.min(moodPoints + reward.moodPoints * moodBonusMultiplier, Constants.MAX_PET_MP)
        }

    companion object {
        const val AWESOME_MIN_MOOD_POINTS = 90
        const val HAPPY_MIN_MOOD_POINTS = 60
        const val GOOD_MIN_MOOD_POINTS = 35

        private fun bonusFor(mood: PetMood, maxBonus: Float): Float {
            val percentage = when (mood) {
                AWESOME -> 1.0f
                HAPPY -> 0.5f
                GOOD -> 0.25f
                SAD -> 0.1f
            }
            return maxBonus * percentage
        }

        private fun moodFor(moodPoints: Int) =
            when {
                moodPoints >= AWESOME_MIN_MOOD_POINTS -> AWESOME
                moodPoints >= HAPPY_MIN_MOOD_POINTS -> HAPPY
                moodPoints >= GOOD_MIN_MOOD_POINTS -> GOOD
                else -> SAD
            }
    }
}

enum class PetMood {
    SAD, GOOD, HAPPY, AWESOME
}

enum class PetAvatar(val price: Int) {
    SEAL(600),
    DONKEY(500),
    ELEPHANT(500),
    BEAVER(500),
    CHICKEN(700),
    BEAR(500),
    LION(500),
    CAT(500),
    MONKEY(500),
    DUCK(500),
    PIG(500),
    ZEBRA(500)
}

enum class AndroidPetAvatar(
    @StringRes val petName: Int,
    @DrawableRes val image: Int,
    @DrawableRes val headPicture: Int,
    @DrawableRes val deadStateImage: Int,
    val moodImage: Map<PetMood, Int>) {

    //    SEAL(R.string.pet_seal, R.drawable.pet_1, R.drawable.pet_1_head),
//    DONKEY(R.string.pet_donkey, R.drawable.pet_2, R.drawable.pet_2_head),
    ELEPHANT(R.string.pet_elephant, R.drawable.pet_3, R.drawable.pet_3_head, R.drawable.pet_3_dead,
        mapOf(
            SAD to R.drawable.pet_3_sad,
            GOOD to R.drawable.pet_3_good,
            HAPPY to R.drawable.pet_3_happy,
            AWESOME to R.drawable.pet_3_awesome
        )
    ),
//    BEAVER(R.string.pet_beaver, R.drawable.pet_4, R.drawable.pet_4_head),
//    CHICKEN(R.string.pet_chicken, R.drawable.pet_5, R.drawable.pet_5_head),
//    BEAR(R.string.pet_chicken, R.drawable.pet_6, R.drawable.pet_6_head),
//    LION(R.string.pet_chicken, R.drawable.pet_7, R.drawable.pet_7_head),
//    CAT(R.string.pet_chicken, R.drawable.pet_8, R.drawable.pet_8_head),
//    MONKEY(R.string.pet_chicken, R.drawable.pet_9, R.drawable.pet_9_head),
//    DUCK(R.string.pet_chicken, R.drawable.pet_10, R.drawable.pet_10_head),
//    PIG(R.string.pet_chicken, R.drawable.pet_11, R.drawable.pet_11_head),
//    ZEBRA(R.string.pet_chicken, R.drawable.pet_12, R.drawable.pet_12_head)
}