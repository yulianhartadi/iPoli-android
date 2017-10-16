package io.ipoli.android.quest.data.persistence

import io.ipoli.android.common.datetime.toStartOfDayUTCMillis
import io.ipoli.android.common.persistence.BaseRealmRepository
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.QuestSchedule
import io.ipoli.android.quest.data.RealmQuest
import io.reactivex.Observable
import io.realm.Sort
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */
interface QuestRepository : Repository<Quest> {
    fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate): Observable<List<Quest>>
    fun listenForDate(date: LocalDate): Observable<List<Quest>>
}

class RealmQuestRepository : BaseRealmRepository<Quest, RealmQuest>(), QuestRepository {
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

    override fun getModelClass() = RealmQuest::class.java

    override fun convertToEntity(realmModel: RealmQuest) =
        Quest(
            id = realmModel.id,
            name = realmModel.name,
            color = Color.valueOf(realmModel.colorName!!),
            category = Category(realmModel.category!!, Color.BLUE),
            plannedSchedule = QuestSchedule(realmModel.scheduledDate, realmModel.startTime, realmModel.getDuration()),
            reminders = listOf(),
            createdAt = LocalDateTime.now()
        )

    override fun convertToRealmModel(entity: Quest): RealmQuest {
        return entity.let {
            val rq = RealmQuest(it.name, io.ipoli.android.quest.data.Category.CHORES)
            rq.id = entity.id
            rq.scheduledDate = entity.plannedSchedule.date
            rq
        }
    }
}