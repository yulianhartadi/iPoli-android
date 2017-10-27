package io.ipoli.android.quest.data.persistence

import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Expression.property
import com.couchbase.lite.Function
import com.couchbase.lite.SelectResult
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.persistence.BaseCouchbaseRepository
import io.ipoli.android.common.persistence.CouchbasePersistedModel
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.quest.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import kotlin.coroutines.experimental.CoroutineContext

interface QuestRepository : Repository<Quest> {
    fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate): ReceiveChannel<List<Quest>>
    fun listenForDate(date: LocalDate): ReceiveChannel<List<Quest>>
    fun findNextQuestsToRemind(afterTime: Long): List<Quest>
    fun findQuestsToRemind(time: Long): List<Quest>
}

data class CouchbaseQuest(override val map: MutableMap<String, Any?> = mutableMapOf()) : CouchbasePersistedModel {
    override var type: String by map
    override var id: String by map
    var name: String by map
    var color: String by map
    var category: String by map
    var duration: Int by map
    var reminder: MutableMap<String, Any?>? by map
    var startMinute: Long? by map
    var scheduledDate: Long? by map
    var completedDate: Long? by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map

    companion object {
        const val TYPE = "Quest"
    }
}

data class CouchbaseReminder(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var id: String by map
    var message: String by map
    var minute: Int by map
    var date: Long by map
}

class CouchbaseQuestRepository(database: Database, coroutineContext: CoroutineContext) : BaseCouchbaseRepository<Quest, CouchbaseQuest>(database, coroutineContext), QuestRepository {

    override val modelType = CouchbaseQuest.TYPE

    override fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate) =
        listenForChanges(
            property("scheduledDate")
                .between(startDate.startOfDayUTC(), endDate.startOfDayUTC())
        )

    override fun listenForDate(date: LocalDate) =
        listenForChanges(property("scheduledDate").equalTo(date.startOfDayUTC()))

    override fun findNextQuestsToRemind(afterTime: Long): List<Quest> {

        val remindDate = DateUtils.fromMillis(afterTime).startOfDayUTC()

        val e = Instant.ofEpochMilli(afterTime).atZone(ZoneId.systemDefault())
        val time = Time.at(e.hour, e.minute)
        val minDate = Function.min(property("reminder.date"))
        val minMinute = Function.min(property("reminder.minute"))
        val query = select(
            SelectResult.all(),
            SelectResult.expression(Expression.meta().id),
            SelectResult.expression(minDate),
            SelectResult.expression(minMinute)
        )
            .where(
                property("reminder.date").greaterThanOrEqualTo(remindDate)
                    .and(property("reminder.minute").greaterThan(time.toMinuteOfDay()))
                    .and(property("type").equalTo(modelType)
                    )
            )
            .groupBy(property("_id"))
            .having(
                property("reminder.minute").equalTo(minMinute).and(
                    property("reminder.date").equalTo(minDate)
                )
            )
        return toEntities(query.run().iterator())
    }

    override fun findQuestsToRemind(time: Long): List<Quest> {
        val remindDate = DateUtils.fromMillis(time).startOfDayUTC()
        val e = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())
        val remindTime = Time.at(e.hour, e.minute)
        val query = selectAll()
            .where(
                property("reminder.date").equalTo(remindDate)
                    .and(property("reminder.minute").equalTo(remindTime.toMinuteOfDay()))
                    .and(property("type").equalTo(modelType)
                    )
            )
        return toEntities(query.run().iterator())

    }

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): Quest {
        val cq = CouchbaseQuest(dataMap.withDefault {
            null
        })

        val plannedDate = cq.scheduledDate?.let { DateUtils.fromMillis(it) }
        val plannedTime = cq.startMinute?.let { Time.of(it.toInt()) }

        return Quest(
            id = cq.id,
            name = cq.name,
            color = Color.valueOf(cq.color),
            category = Category(cq.category, Color.GREEN),
            plannedSchedule = QuestSchedule(plannedDate, plannedTime, cq.duration),
            completedAtDate = cq.completedDate?.let {
                DateUtils.fromMillis(it)
            },
            reminder = cq.reminder?.let {
                val cr = CouchbaseReminder(it)
                Reminder(cr.id, cr.message, Time.of(cr.minute), DateUtils.fromMillis(cr.date))
            }
        )
    }

    override fun toCouchbaseObject(entity: Quest): CouchbaseQuest {
        val q = CouchbaseQuest()
        q.id = entity.id
        q.name = entity.name
        q.category = entity.category.name
        q.color = entity.color.name
        q.duration = entity.plannedSchedule.duration
        q.type = CouchbaseQuest.TYPE
        q.scheduledDate = DateUtils.toMillis(entity.plannedSchedule.date!!)
        q.reminder = entity.reminder?.let {
            createCouchbaseReminder(it).map
        }
        entity.plannedSchedule.time?.let { q.startMinute = it.toMinuteOfDay().toLong() }
        entity.completedAtDate?.let { q.completedDate = it.startOfDayUTC() }
        return q
    }

    private fun createCouchbaseReminder(reminder: Reminder): CouchbaseReminder {
        val cr = CouchbaseReminder()
        cr.id = reminder.id
        cr.message = reminder.message
        cr.date = reminder.remindDate.startOfDayUTC()
        cr.minute = reminder.remindTime.toMinuteOfDay()
        return cr
    }
}