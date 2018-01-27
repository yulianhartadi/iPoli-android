package mypoli.android.quest.data.persistence

import com.couchbase.lite.*
import com.couchbase.lite.Expression.property
import com.couchbase.lite.Function
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import mypoli.android.common.datetime.DateUtils
import mypoli.android.common.datetime.Time
import mypoli.android.common.datetime.instant
import mypoli.android.common.datetime.startOfDayUTC
import mypoli.android.common.persistence.BaseCouchbaseRepository
import mypoli.android.common.persistence.CouchbasePersistedModel
import mypoli.android.common.persistence.Repository
import mypoli.android.pet.Food
import mypoli.android.quest.*
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import kotlin.coroutines.experimental.CoroutineContext

interface QuestRepository : Repository<Quest> {
    fun listenForScheduledBetween(
        startDate: LocalDate,
        endDate: LocalDate
    ): ReceiveChannel<List<Quest>>

    fun listenForDate(date: LocalDate): ReceiveChannel<List<Quest>>
    fun findNextQuestsToRemind(afterTime: Long = DateUtils.nowUTC().time): List<Quest>
    fun findQuestsToRemind(time: Long): List<Quest>
    fun findCompletedForDate(date: LocalDate): List<Quest>
    fun findStartedQuest(): Quest?
}

data class CouchbaseQuest(override val map: MutableMap<String, Any?> = mutableMapOf()) :
    CouchbasePersistedModel {
    override var type: String by map
    override var id: String by map
    var name: String by map
    var color: String by map
    var icon: String? by map
    var category: String by map
    var duration: Int by map
    var reminder: MutableMap<String, Any?>? by map
    var startMinute: Long? by map
    var experience: Long? by map
    var coins: Long? by map
    var bounty: MutableMap<String, Any?>? by map
    var scheduledDate: Long by map
    var completedAtDate: Long? by map
    var completedAtMinute: Long? by map
    var timeRanges: List<MutableMap<String, Any?>> by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map

    companion object {
        const val TYPE = "Quest"
    }
}

data class CouchbaseReminder(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var message: String by map
    var minute: Int by map
    var date: Long by map
}

data class CouchbaseBounty(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var type: String by map
    var name: String? by map

    enum class Type {
        NONE, FOOD
    }
}

data class CouchbaseTimeRange(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var type: String by map
    var duration: Int by map
    var start: Long? by map
    var end: Long? by map
}

class CouchbaseQuestRepository(database: Database, coroutineContext: CoroutineContext) :
    BaseCouchbaseRepository<Quest, CouchbaseQuest>(database, coroutineContext), QuestRepository {

    override val modelType = CouchbaseQuest.TYPE

    override fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate) =
        listenForChanges(
            where = property("scheduledDate")
                .between(startDate.startOfDayUTC(), endDate.startOfDayUTC())
        )

    override fun listenForDate(date: LocalDate) =
        listenForChanges(
            where = property(
                "scheduledDate"
            ).equalTo(date.startOfDayUTC())
        )

    override fun findNextQuestsToRemind(afterTime: Long): List<Quest> {

        val remindDate = DateUtils.fromMillis(afterTime).startOfDayUTC()

        val e = Instant.ofEpochMilli(afterTime).atZone(ZoneId.systemDefault())
        val time = Time.at(e.hour, e.minute)
        val minDate = Function.min(property("reminder.date"))
        val minMinute = Function.min(property("reminder.minute"))

        val query = createQuery(
            select = select(
                SelectResult.all(),
                SelectResult.expression(Meta.id),
                SelectResult.expression(minDate),
                SelectResult.expression(minMinute)
            ),
            where = property("reminder.date").greaterThanOrEqualTo(remindDate)
                .and(property("reminder.minute").greaterThan(time.toMinuteOfDay()))
                .and(property("type").equalTo(modelType))
                .and(property("completedAtDate").isNullOrMissing),
            groupBy = GroupClause(
                groupBy = property("_id"),
                having = property("reminder.minute").equalTo(minMinute).and(
                    property("reminder.date").equalTo(minDate)
                )
            ),
            orderBy = Ordering.expression(property("reminder.minute"))
        )
        return toEntities(query.execute().iterator())
    }

    override fun findQuestsToRemind(time: Long): List<Quest> {
        val remindDate = DateUtils.fromMillis(time).startOfDayUTC()
        val e = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())
        val remindTime = Time.at(e.hour, e.minute)
        val query = createQuery(
            where = property("reminder.date").equalTo(remindDate)
                .and(property("reminder.minute").equalTo(remindTime.toMinuteOfDay()))
                .and(property("type").equalTo(modelType))
                .and(property("completedAtDate").isNullOrMissing)
        )
        return toEntities(query.execute().iterator())

    }

    override fun findCompletedForDate(date: LocalDate): List<Quest> {
        val query = createQuery(
            where = property("completedAtDate")
                .between(date.startOfDayUTC(), date.startOfDayUTC())
        )
        return toEntities(query.execute().iterator())
    }

    override fun findStartedQuest(): Quest? {
        val query = createQuery(
            where = property("completedAtDate").isNullOrMissing
                .and(ArrayFunction.length(property("timeRanges")).greaterThan(0)),
            limit = 1
        )
        val result = query.execute().next()
        return result?.let {
            toEntityObject(it)
        }
    }

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): Quest {
        val cq = CouchbaseQuest(dataMap.withDefault {
            null
        })

        val plannedDate = DateUtils.fromMillis(cq.scheduledDate)
        val plannedTime = cq.startMinute?.let { Time.of(it.toInt()) }

        return Quest(
            id = cq.id,
            name = cq.name,
            color = Color.valueOf(cq.color),
            icon = cq.icon?.let {
                Icon.valueOf(it)
            },
            category = Category(cq.category, Color.GREEN),
            scheduledDate = plannedDate,
            startTime = plannedTime,
            duration = cq.duration,
            experience = cq.experience?.toInt(),
            coins = cq.coins?.toInt(),
            bounty = cq.bounty?.let {
                val cr = CouchbaseBounty(it)
                when {
                    cr.type == CouchbaseBounty.Type.NONE.name -> Quest.Bounty.None
                    cr.type == CouchbaseBounty.Type.FOOD.name -> Quest.Bounty.Food(Food.valueOf(cr.name!!))
                    else -> null
                }

            },
            completedAtDate = cq.completedAtDate?.let {
                DateUtils.fromMillis(it)
            },
            completedAtTime = cq.completedAtMinute?.let {
                Time.of(it.toInt())
            },
            reminder = cq.reminder?.let {
                val cr = CouchbaseReminder(it)
                Reminder(cr.message, Time.of(cr.minute), DateUtils.fromMillis(cr.date))
            },
            timeRanges = cq.timeRanges.map {
                val ctr = CouchbaseTimeRange(it)
                TimeRange(
                    TimeRange.Type.valueOf(ctr.type),
                    ctr.duration,
                    ctr.start?.instant,
                    ctr.end?.instant
                )
            }
        )
    }

    override fun toCouchbaseObject(entity: Quest): CouchbaseQuest {
        val q = CouchbaseQuest()
        q.id = entity.id
        q.name = entity.name
        q.category = entity.category.name
        q.color = entity.color.name
        q.icon = entity.icon?.name
        q.duration = entity.duration
        q.type = CouchbaseQuest.TYPE
        q.scheduledDate = entity.scheduledDate.startOfDayUTC()
        q.reminder = entity.reminder?.let {
            createCouchbaseReminder(it).map
        }
        q.experience = entity.experience?.toLong()
        q.coins = entity.coins?.toLong()
        q.bounty = entity.bounty?.let {
            val cr = CouchbaseBounty()

            cr.type = when (it) {
                is Quest.Bounty.None -> CouchbaseBounty.Type.NONE.name
                is Quest.Bounty.Food -> CouchbaseBounty.Type.FOOD.name
                else -> throw IllegalArgumentException("Unexpected bounty type: ${it}")
            }

            if (it is Quest.Bounty.Food) {
                cr.name = it.food.name
            }

            cr.map
        }
        q.startMinute = entity.startTime?.toMinuteOfDay()?.toLong()
        q.completedAtDate = entity.completedAtDate?.startOfDayUTC()
        q.completedAtMinute = entity.completedAtTime?.toMinuteOfDay()?.toLong()
        q.timeRanges = entity.timeRanges.map {
            createCouchbaseTimeRange(it).map
        }
        return q
    }

    private fun createCouchbaseTimeRange(timeRange: TimeRange): CouchbaseTimeRange {
        val cTimeRange = CouchbaseTimeRange()
        cTimeRange.type = timeRange.type.name
        cTimeRange.duration = timeRange.duration
        cTimeRange.start = timeRange.start?.toEpochMilli()
        cTimeRange.end = timeRange.end?.toEpochMilli()
        return cTimeRange
    }

    private fun createCouchbaseReminder(reminder: Reminder): CouchbaseReminder {
        val cr = CouchbaseReminder()
        cr.message = reminder.message
        cr.date = reminder.remindDate.startOfDayUTC()
        cr.minute = reminder.remindTime.toMinuteOfDay()
        return cr
    }
}