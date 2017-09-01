package io.ipoli.android.repeatingquest.persistence

import io.ipoli.android.common.persistence.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.repeatingquest.data.RepeatingQuest

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/22/17.
 */
interface RepeatingQuestRepository : Repository<RepeatingQuest> {

}

class RealmRepeatingQuestRepository : BaseRealmRepository<RepeatingQuest>(), RepeatingQuestRepository {
    override fun getModelClass(): Class<RepeatingQuest> = RepeatingQuest::class.java
}