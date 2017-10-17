package io.ipoli.android.quest.data.persistence

import com.couchbase.lite.*
import io.ipoli.android.common.datetime.toStartOfDayUTCMillis
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
    override var createdAt: Long by map
    override var updatedAt: Long by map
    override var removedAt: Long? by map

    companion object {
        const val TYPE = "Quest"
    }
}

abstract class BaseCouchbaseRepository<E, T>(private val database: Database) : Repository<E> where E : Entity, T : CouchbasePersistedModel {
    protected abstract val modelType: String

    override fun listenById(id: String): Channel<E?> {
        val query = selectAll()
            .where(Expression.property("id").equalTo(id))
            .toLive()

        return sendResult(query)
    }

    override fun listenForAll(): Channel<List<E>> {
        val query = selectAll()
            .where(Expression.property("type").equalTo(modelType))
            .toLive()
        return sendResults(query)
    }

    protected fun sendResults(query: LiveQuery): Channel<List<E>> {
        val channel = Channel<List<E>>()
        val changeListener = createChangeListener(query, channel) { changes ->
            val result = toEntities(changes)
            launch {
                channel.send(result.toList())
            }
        }
        runLiveQuery(query, changeListener)
        return channel
    }

    private fun toEntities(changes: LiveQueryChange) =
        changes.rows.iterator().asSequence().map { toEntityObject(it) }

    private fun sendResult(query: LiveQuery): Channel<E?> {
        val channel = Channel<E?>()

        val changeListener = createChangeListener(query, channel) { changes ->
            launch {
                val result = toEntities(changes)
                channel.send(result.firstOrNull())
            }
        }

        runLiveQuery(query, changeListener)
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

    override fun find(): E? {
        val query = selectAll()
            .where(Expression.property("type").equalTo(modelType))
            .limit(1)
        val iterator = query.run().iterator()
        return when {
            iterator.hasNext() -> toEntityObject(iterator.next())
            else -> null
        }
    }

    protected fun selectAll() =
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

    }

    override fun delete(id: String) {

    }

    protected fun toEntityObject(row: Result): E =
        toEntityObject(row.toMap().toMutableMap())

    protected abstract fun toEntityObject(dataMap: MutableMap<String, Any?>): E

    protected abstract fun toCouchbaseObject(entity: E): T
}

class CouchbaseQuestRepository(database: Database) : BaseCouchbaseRepository<Quest, CouchbaseQuest>(database), QuestRepository {
    override val modelType = CouchbaseQuest.TYPE

    override fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate): Channel<List<Quest>> {
        val query = selectAll()
            .where(
                Expression.property("type").equalTo(modelType)
                    .and(Expression.property("scheduled"))
                    .between(startDate.toStartOfDayUTCMillis(), endDate.toStartOfDayUTCMillis())
            )
            .toLive()
        return sendResults(query)
    }

    override fun listenForDate(date: LocalDate): Channel<List<Quest>> {
        val query = selectAll()
            .where(
                Expression.property("type").equalTo(modelType)
                    .and(Expression.property("scheduled"))
                    .equalTo(date.toStartOfDayUTCMillis()))
            .toLive()
        return sendResults(query)
    }

    override fun toEntityObject(dataMap: MutableMap<String, Any?>): Quest {
        val cq = CouchbaseQuest(dataMap)
        return Quest(
            id = cq.id,
            name = cq.name,
            color = Color.valueOf(cq.color),
            category = Category(cq.category, Color.GREEN),
            plannedSchedule = QuestSchedule(null, null, cq.duration),
            reminders = listOf()
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
        return q
    }
}

//class RealmQuestRepository : BaseRealmRepository<Quest, RealmQuest>(), QuestRepository {
//    override fun listenForDate(date: LocalDate): Observable<List<Quest>> =
//        listenForAllSorted({ q ->
//            q.equalTo("scheduled", date.toStartOfDayUTCMillis())
//        }, listOf(
//            Pair("startMinute", Sort.ASCENDING),
//            Pair("completedAtMinute", Sort.ASCENDING)
//        ))
//
//    override fun listenForScheduledBetween(startDate: LocalDate, endDate: LocalDate): Observable<List<Quest>> =
//        listenForAll { query ->
//            query.between("scheduled", startDate.toStartOfDayUTCMillis(), endDate.toStartOfDayUTCMillis())
//        }
//
//    override fun getModelClass() = RealmQuest::class.java
//
//    override fun convertToEntity(realmModel: RealmQuest) =
//        Quest(
//            id = realmModel.id,
//            name = realmModel.name,
//            color = Color.valueOf(realmModel.colorName!!),
//            category = Category(realmModel.category!!, Color.BLUE),
//            plannedSchedule = QuestSchedule(realmModel.scheduledDate, realmModel.startTime, realmModel.getDuration()),
//            reminders = listOf(),
//            createdAt = LocalDateTime.now()
//        )
//
//    override fun convertToRealmModel(entity: Quest): RealmQuest {
//        return entity.let {
//            val rq = RealmQuest(it.name, io.ipoli.android.quest.data.Category.CHORES)
//            rq.id = entity.id
//            rq.scheduledDate = entity.plannedSchedule.date
//            rq
//        }
//    }
//}