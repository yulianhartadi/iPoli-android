package io.ipoli.android.quest.data.persistence

import io.ipoli.android.common.datetime.toStartOfDayUTCMillis
import io.ipoli.android.common.persistence.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.quest.data.RealmQuest
import io.reactivex.Observable
import io.realm.Sort
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */
interface QuestRepository : Repository<RealmQuest> {
    fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate): Observable<List<RealmQuest>>
    fun listenForDate(date: LocalDate): Observable<List<RealmQuest>>
}

class RealmQuestRepository : BaseRealmRepository<RealmQuest>(), QuestRepository {
    override fun listenForDate(date: LocalDate): Observable<List<RealmQuest>> =
        listenForAllSorted({ q ->
            q.equalTo("scheduled", date.toStartOfDayUTCMillis())
        }, listOf(
            Pair("startMinute", Sort.ASCENDING),
            Pair("completedAtMinute", Sort.ASCENDING)
        ))

    override fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate): Observable<List<RealmQuest>> =
        listenForAll { query ->
            query.between("scheduled", startDate.toStartOfDayUTCMillis(), endDate.toStartOfDayUTCMillis())
        }

    override fun getModelClass(): Class<RealmQuest> = RealmQuest::class.java
}