package io.ipoli.android.challenge.persistence

import io.ipoli.android.challenge.data.Challenge
import io.ipoli.android.common.persistence.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/17.
 */
interface ChallengeRepository : Repository<Challenge> {

}

class RealmChallengeRepository : BaseRealmRepository<Challenge>(), ChallengeRepository {

    override fun getModelClass(): Class<Challenge> = Challenge::class.java
}