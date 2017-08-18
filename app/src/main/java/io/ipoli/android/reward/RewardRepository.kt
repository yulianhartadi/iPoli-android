package io.ipoli.android.reward

import io.ipoli.android.common.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/17.
 */
interface RewardRepository : Repository<Reward> {}

class RealmRewardRepository : BaseRealmRepository<Reward>(), RewardRepository {
    override fun getModelClass(): Class<Reward> = Reward::class.java
}