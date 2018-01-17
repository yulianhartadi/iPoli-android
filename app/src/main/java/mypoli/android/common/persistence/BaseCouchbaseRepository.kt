package mypoli.android.common.persistence

import com.couchbase.lite.*
import com.couchbase.lite.internal.query.LiveQuery
import com.couchbase.lite.internal.query.QueryChangeListenerToken
import com.couchbase.lite.query.QueryChange
import com.couchbase.lite.query.QueryChangeListener
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import mypoli.android.common.datetime.DateUtils
import mypoli.android.quest.Entity
import kotlin.coroutines.experimental.CoroutineContext

abstract class BaseCouchbaseRepository<E, out T>(protected val database: Database, private val coroutineContext: CoroutineContext) : Repository<E> where E : Entity, T : CouchbasePersistedModel {
    protected abstract val modelType: String

    override fun findById(id: String): E? {
        val map = database.getDocument(id).toMap()
        map.put("id", id)
        return toEntityObject(map)
    }

    override fun listenById(id: String) =
        listenForChange(
            where = Meta.id.equalTo(id)
        )

    override fun listen() =
        listenForChange(
            limit = 1
        )

    protected fun listenForChange(select: From? = null, where: Expression? = null, limit: Int? = null, orderBy: Ordering? = null) =
        sendLiveResult(createQuery(select, where, limit, orderBy))

    protected fun listenForChanges(select: From? = null, where: Expression? = null, limit: Int? = null, orderBy: Ordering? = null) =
        sendLiveResults(createQuery(select, where, limit, orderBy))

    data class GroupClause(val groupBy: Expression, val having: Expression)

    protected fun createQuery(
        select: From? = null,
        where: Expression? = null,
        limit: Int? = null,
        orderBy: Ordering? = null,
        groupBy: GroupClause? = null
    ): Query {
        val typeWhere = Expression.property("type").equalTo(modelType)
            .and(Expression.property("removedAt").isNullOrMissing)
        val w = if (where == null) typeWhere else typeWhere.and(where)

        val selectClause = select ?: selectAll()

        val q = selectClause.where(w)

        when {
            groupBy != null -> {
                val group = q.groupBy(groupBy.groupBy).having(groupBy.having)

                if (orderBy != null) {
                    val order = group.orderBy(orderBy)

                    if (limit != null) {
                        return order.limit(limit)
                    }

                    return order
                }

                if (limit != null) {
                    return group.limit(limit)
                }

                return group
            }
            orderBy != null -> {
                val order = q.orderBy(orderBy)

                if (limit != null) {
                    return order.limit(limit)
                }

                return order
            }
            limit != null -> return q.limit(limit)
            else -> return q
        }
    }

    override fun listenForAll() = listenForChanges()

    protected fun sendLiveResults(query: Query) =
        Channel<List<E>>().also {
            addChangeListener(query, it) { changes ->
                val result = toEntities(changes)
                launch(coroutineContext) {
                    it.send(result.toList())
                }
            }
        }

    protected fun toEntities(changes: QueryChange): List<E> =
        toEntities(changes.rows.iterator())

    protected fun toEntities(iterator: MutableIterator<Result>): List<E> {
        val list = mutableListOf<E>()
        iterator.forEach {
            list.add(toEntityObject(it))
        }
        return list
    }

    private fun sendLiveResult(query: Query) =
        Channel<E?>().also {
            addChangeListener(query, it) { changes ->
                val result = toEntities(changes)
                launch(coroutineContext) {
                    it.send(result.firstOrNull())
                }
            }
        }

    private fun <E> addChangeListener(
        query: Query,
        channel: SendChannel<E>,
        handler: (changes: QueryChange) -> Unit
    ) {
        val liveQuery = LiveQuery(query)

        var listenerToken: QueryChangeListenerToken? = null

        val changeListener = QueryChangeListener { changes ->
            if (channel.isClosedForSend) {
                liveQuery.removeChangeListener(listenerToken)
                liveQuery.stop()
            } else {
                handler(changes)
            }
        }

        listenerToken = liveQuery.addChangeListener(changeListener)
    }

    private fun runQuery(select: From? = null, where: Expression? = null, limit: Int? = null, orderBy: Ordering? = null) =
        createQuery(select, where, limit, orderBy).execute().iterator()

    override fun find() =
        toEntities(
            runQuery(
                limit = 1
            )
        ).firstOrNull()

    protected fun select(vararg select: SelectResult): From =
        Query.select(*select).from(DataSource.database(database))

    protected fun selectAll(): From =
        select(SelectResult.all(), SelectResult.expression(Meta.id))

    override fun save(entity: E): E {
        val cbObject = toCouchbaseObject(entity)

        val doc = if (cbObject.id.isNotEmpty()) {
            cbObject.updatedAt = System.currentTimeMillis()
            database.getDocument(cbObject.id).toMutable()
        } else {
            MutableDocument()
        }

        val cbMap = cbObject.map.toMutableMap()
        cbMap.remove("id")
        doc.setData(cbMap)

        cbMap.filterValues { it == null }.keys.forEach {
            doc.remove(it)
        }

        database.save(doc)

        val docMap = doc.toMap().toMutableMap()
        docMap["id"] = doc.id

        return toEntityObject(docMap)
    }

    override fun remove(entity: E) {
        remove(entity.id)
    }

    override fun remove(id: String) {
        val doc = database.getDocument(id).toMutable()
        doc.setLong("removedAt", DateUtils.nowUTC().time)
        database.save(doc)
    }

    override fun undoRemove(id: String) {
        val doc = database.getDocument(id).toMutable()
        doc.remove("removedAt")
        database.save(doc)
    }

    protected fun toEntityObject(row: Result): E {
        val rowMap = row.toMap()
        @Suppress("UNCHECKED_CAST")
        val map = rowMap["myPoli"] as MutableMap<String, Any?>
        map.put("id", rowMap["_id"])
        return toEntityObject(map)
    }

    protected abstract fun toEntityObject(dataMap: MutableMap<String, Any?>): E

    protected abstract fun toCouchbaseObject(entity: E): T
}