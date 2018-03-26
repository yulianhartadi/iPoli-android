package io.ipoli.android.common

import io.ipoli.android.quest.Quest

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/13/17.
 */
interface Reward {
    val experience: Int
    val coins: Int
    val bounty: Quest.Bounty
}

data class SimpleReward(
    override val experience: Int,
    override val coins: Int,
    override val bounty: Quest.Bounty
) : Reward {
    companion object {
        fun of(quest: Quest) =
            SimpleReward(quest.experience!!, quest.coins!!, quest.bounty!!)
    }
}