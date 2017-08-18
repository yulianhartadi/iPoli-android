package io.ipoli.android.reward

import io.ipoli.android.common.BaseRealmRepository

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/17.
 */
class RewardRepository : BaseRealmRepository<Reward>() {

    override fun getModelClass(): Class<Reward> = Reward::class.java
}