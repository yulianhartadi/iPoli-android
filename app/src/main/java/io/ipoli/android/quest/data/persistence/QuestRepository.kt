package io.ipoli.android.quest.data.persistence

import com.couchbase.lite.*
import com.couchbase.lite.Expression.property
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.persistence.PersistedModel
import io.ipoli.android.common.persistence.Repository
import io.ipoli.android.quest.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */

interface CouchbasePersistedModel : PersistedModel {
    var type: String
}

interface QuestRepository : Repository<Quest> {
    fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate): Channel<List<Quest>>
    fun listenForDate(date: LocalDate): Channel<List<Quest>>
}

data class CouchbaseQuest(override val map: MutableMap<String, Any?> = mutableMapOf()) : CouchbasePersistedModel {
    override var type: String by map
    override var id: String by map
    var name: String by map
    var color: String by map
    var category: String by map
    var duration: Int by map
    var reminders: List<MutableMap<String, Any?>> by map
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map

    companion object {
        const val TYPE = "Quest"
    }
}

data class CouchbaseReminder(val map: MutableMap<String, Any?> = mutableMapOf()) {
    var notificationId: String by map
    var message: String by map
    var remindMinute: Int by map
    var remindDate: Long by map
}

abstract class BaseCouchbaseRepository<E, out T>(private val database: Database) : Repository<E> where E : Entity, T : CouchbasePersistedModel {
    protected abstract val modelType: String

    override fun listenById(id: String) =
        listenForChange(
            where = property("id").equalTo(id)
        )

    protected fun listenForChange(where: Expression? = null, limit: Int? = null, orderBy: Ordering? = null) =
        sendLiveResult(createQuery(where, limit, orderBy))

    protected fun listenForChanges(where: Expression? = null, limit: Int? = null, orderBy: Ordering? = null) =
        sendLiveResults(createQuery(where, limit, orderBy))

    protected fun createQuery(where: Expression? = null, limit: Int? = null, orderBy: Ordering? = null): Query {
        val typeWhere = property("type").equalTo(modelType)
        val w = if (where == null) typeWhere else typeWhere.and(where)

        val q = selectAll().where(w)
        orderBy?.let { q.orderBy(it) }
        limit?.let { q.limit(it) }
        return q
    }

    override fun listenForAll() = listenForChanges()

    protected fun sendLiveResults(query: Query): Channel<List<E>> {
        val channel = Channel<List<E>>()
        val liveQuery = query.toLive()
        val changeListener = createChangeListener(liveQuery, channel) { changes ->
            val result = toEntities(changes)
            launch {
                channel.send(result.toList())
            }
        }
        runLiveQuery(liveQuery, changeListener)
        return channel
    }

    private fun toEntities(changes: LiveQueryChange): Sequence<E> =
        toEntities(changes.rows.iterator())

    private fun toEntities(iterator: MutableIterator<Result>): Sequence<E> =
        iterator.asSequence().map { toEntityObject(it) }

    private fun sendLiveResult(query: Query): Channel<E?> {
        val channel = Channel<E?>()
        val liveQuery = query.toLive()

        val changeListener = createChangeListener(liveQuery, channel) { changes ->
            launch {
                val result = toEntities(changes)
                channel.send(result.firstOrNull())
            }
        }

        runLiveQuery(liveQuery, changeListener)
        return channel
    }

    private fun <E> createChangeListener(
        query: LiveQuery,
        channel: Channel<E>,
        handler: (changes: LiveQueryChange) -> Unit
    ): LiveQueryChangeListener {
        var changeListener: LiveQueryChangeListener? = null

        changeListener = LiveQueryChangeListener { changes ->
            if (channel.isClosedForReceive) {
                query.removeChangeListener(changeListener)
                query.stop()
            } else {
                handler(changes)
            }
        }
        return changeListener
    }

    private fun runLiveQuery(query: LiveQuery, changeListener: LiveQueryChangeListener) {
        query.addChangeListener(changeListener)
        query.run()
    }

    private fun runQuery(where: Expression? = null, limit: Int? = null, orderBy: Ordering? = null) =
        createQuery(where, limit, orderBy).run().iterator()

    override fun find() =
        toEntities(
            runQuery(
                limit = 1
            )
        ).firstOrNull()

    protected fun selectAll(): From =
        Query.select(SelectResult.all())
            .from(DataSource.database(database))

    override fun save(entity: E): E {
        val cbObject = toCouchbaseObject(entity)
        if (cbObject.id.isNotEmpty()) {
            cbObject.updatedAt = System.currentTimeMillis()
        }
        val doc = Document(cbObject.map)
        database.save(doc)
        return toEntityObject(doc.toMap().toMutableMap())
    }

    override fun delete(entity: E) {
        delete(entity.id)
    }

    override fun delete(id: String) {
        database.delete(database.getDocument(id))
    }

    protected fun toEntityObject(row: Result): E =
        toEntityObject(row.toMap().toMutableMap())

    protected abstract fun toEntityObject(dataMap: MutableMap<String, Any?>): E

    protected abstract fun toCouchbaseObject(entity: E): T
}

class CouchbaseQuestRepository(database: Database) : BaseCouchbaseRepository<Quest, CouchbaseQuest>(database), QuestRepository {
    override val modelType = CouchbaseQuest.TYPE

    override fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate) =
        listenForChanges(
            property("scheduled")
                .between(startDate.startOfDayUTC(), endDate.startOfDayUTC())
        )

    override fun listenForDate(date: LocalDate) =
        listenForChanges(property("scheduled").equalTo(date.startOfDayUTC()))

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): Quest {
        val cq = CouchbaseQuest(dataMap)

        val reminders = cq.reminders
            .map { CouchbaseReminder(it) }
            .map {
                Reminder(
                    notificationId = it.notificationId,
                    message = it.message,
                    remindTime = Time.of(it.remindMinute),
                    remindDate = DateUtils.fromMillis(it.remindDate)
                )
            }

        return Quest(
            id = cq.id,
            name = cq.name,
            color = Color.valueOf(cq.color),
            category = Category(cq.category, Color.GREEN),
            plannedSchedule = QuestSchedule(null, null, cq.duration),
            reminders = reminders
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

        q.reminders = entity.reminders.map { r ->
            createCouchbaseReminder(r).map
        }
        return q
    }

    private fun createCouchbaseReminder(reminder: Reminder): CouchbaseReminder {
        val cr = CouchbaseReminder()
        cr.notificationId = reminder.notificationId
        cr.message = reminder.message
        cr.remindDate = reminder.remindDate.startOfDayUTC()
        cr.remindMinute = reminder.remindTime.toMinuteOfDay()
        return cr
    }
}