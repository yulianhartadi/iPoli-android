package io.ipoli.android.quest.data.persistence

import io.ipoli.android.common.datetime.toStartOfDayUTCMillis
import io.ipoli.android.common.persistence.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.quest.data.Quest
import io.reactivex.Observable
import io.realm.Sort
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */
interface QuestRepository : Repository<Quest> {
    fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate): Observable<List<Quest>>
    fun listenForDate(date: LocalDate): Observable<List<Quest>>
}

class RealmQuestRepository : BaseRealmRepository<Quest>(), QuestRepository {
    override fun listenForDate(date: LocalDate): Observable<List<Quest>> =
        listenForAllSorted({ q ->
            q.equalTo("scheduled", date.toStartOfDayUTCMillis())
        }, listOf(
            Pair("startMinute", Sort.ASCENDING),
            Pair("completedAtMinute", Sort.ASCENDING)
        ))

    override fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate): Observable<List<Quest>> =
        listenForAll { query ->
            query.between("scheduled", startDate.toStartOfDayUTCMillis(), endDate.toStartOfDayUTCMillis())
        }

    override fun getModelClass(): Class<Quest> = Quest::class.java
}