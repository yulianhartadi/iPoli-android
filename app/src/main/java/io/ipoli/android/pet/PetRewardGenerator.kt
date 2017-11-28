package io.ipoli.android.pet

import io.ipoli.android.Constants
import io.ipoli.android.quest.Quest

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 28.11.17.
 */

data class PetReward(val healthPoints: Int, val moodPoints: Int)

object PetRewardGenerator {
    fun forQuest(quest: Quest) =
        PetReward(
            healthPoints = Math.floor(quest.experience!! / Constants.XP_TO_PET_HP_RATIO).toInt(),
            moodPoints = Math.floor(quest.experience / Constants.XP_TO_PET_MOOD_RATIO).toInt()
        )
}