package io.ipoli.android.quest.persistence

import io.ipoli.android.common.datetime.toStartOfDayUTCMillis
import io.ipoli.android.common.persistence.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.quest.data.Quest
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/20/17.
 */
interface QuestRepository : Repository<Quest> {
    fun findScheduledBetween(startDate: LocalDate, endDate: LocalDate): Observable<List<Quest>>
}

class RealmQuestRepository : BaseRealmRepository<Quest>(), QuestRepository {
    override fun findScheduledBetween(startDate: LocalDate, endDate: LocalDate): Observable<List<Quest>> =
        findAll { query ->
            query.between("scheduled", startDate.toStartOfDayUTCMillis(), endDate.toStartOfDayUTCMillis())
        }

    override fun getModelClass(): Class<Quest> = Quest::class.java
}