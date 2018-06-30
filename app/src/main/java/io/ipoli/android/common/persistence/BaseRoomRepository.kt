package io.ipoli.android.common.persistence

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Transaction
import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Millisecond
import io.ipoli.android.quest.Entity
import io.ipoli.android.tag.Tag
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

@Dao
abstract class BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(entity: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveAll(entities: List<T>)

    companion object {
        const val REMOVE_QUERY =
            "SET removedAt = :currentTimeMillis, updatedAt = :currentTimeMillis WHERE id = :id"
        const val UNDO_REMOVE_QUERY =
            "SET removedAt = null, updatedAt = :currentTimeMillis WHERE id = :id"
        const val FIND_SYNC_QUERY = "WHERE updatedAt > :lastSync"
    }
}

interface RoomEntity {
    val id: String
}

interface EntityWithTags : Entity {
    val tags: List<Tag>
}

abstract class BaseRoomRepository<E : io.ipoli.android.quest.Entity, RE, D : BaseDao<RE>>(protected val dao: D) {

    data class Subscription(val liveData: LiveData<*>, val observer: Observer<*>)

    private val channelToSubscription = mutableMapOf<SendChannel<*>, Subscription>()

    protected fun LiveData<RE>.notifySingle(channel: Channel<E?>) =
        listenSingle(this, channel)

    protected fun LiveData<List<RE>>.notify(channel: Channel<List<E>>) =
        listen(this, channel)

    private fun listenSingle(
        data: LiveData<RE>,
        channel: Channel<E?>
    ): Channel<E?> {
        launch(CommonPool) {

            channelToSubscription[channel]?.let {
                removeDataSubscription(it, channel)
            }

            val obs = Observer<RE> {
                obsLaunch@ launch(CommonPool) {
                    val s = channelToSubscription[channel] ?: return@obsLaunch

                    if (channel.isClosedForSend || channel.isClosedForReceive) {
                        removeDataSubscription(s, channel)
                        return@obsLaunch
                    }

                    it?.let {
                        channel.offer(toEntityObject(it))
                    }
                }
            }

            data.observeForever(obs)

            channelToSubscription[channel] = Subscription(data, obs)
        }


        return channel
    }

    private fun listen(
        data: LiveData<List<RE>>,
        channel: Channel<List<E>>
    ): Channel<List<E>> {
        launch(CommonPool) {

            channelToSubscription[channel]?.let {
                removeDataSubscription(it, channel)
            }

            val obs = Observer<List<RE>> {
                obsLaunch@ launch(CommonPool) {
                    val s = channelToSubscription[channel] ?: return@obsLaunch

                    if (channel.isClosedForSend || channel.isClosedForReceive) {
                        removeDataSubscription(s, channel)
                        return@obsLaunch
                    }

                    it?.let {
                        channel.offer(it.map { toEntityObject(it) })
                    }
                }
            }

            data.observeForever(obs)

            channelToSubscription[channel] = Subscription(data, obs)
        }

        return channel
    }

    private suspend fun <T> removeDataSubscription(
        s: Subscription,
        channel: Channel<T>
    ) {
        @Suppress("UNCHECKED_CAST")
        (withContext(UI) {
            val l = s.liveData as LiveData<T>
            val o = s.observer as Observer<T>
            l.removeObserver(o)
        })
        channelToSubscription.remove(channel)
    }

    abstract fun findAllForSync(lastSync: Duration<Millisecond>): List<E>

    protected abstract fun toEntityObject(dbObject: RE): E

    protected abstract fun toDatabaseObject(entity: E): RE
}

abstract class BaseRoomRepositoryWithTags<E : EntityWithTags, RE : RoomEntity, D : BaseDao<RE>, JE>
    (dao: D) : BaseRoomRepository<E, RE, D>(dao), Repository<E> {

    abstract fun createTagJoin(entityId: String, tagId: String): JE

    abstract fun newIdForEntity(id: String, entity: E): E

    abstract fun saveTags(joins: List<JE>)

    abstract fun deleteAllTags(entityId: String)

    abstract fun deleteAllTags(entityIds: List<String>)

    override fun save(entity: E): E {
        val hr = saveWithTags(entity)
        return newIdForEntity(hr.id, entity)
    }

    override fun save(entities: List<E>): List<E> {
        val res = saveWithTags(entities)

        return entities.mapIndexed { i, e ->
            newIdForEntity(res[i].id, e)
        }
    }

    @Transaction
    private fun saveWithTags(entity: E): RE {
        if (entity.id.isNotBlank()) {
            deleteAllTags(entity.id)
        }
        val re = toDatabaseObject(entity)
        dao.save(re)
        val joins = entity.tags.map {
            createTagJoin(re.id, it.id)
        }
        saveTags(joins)
        return re
    }

    @Transaction
    private fun saveWithTags(entities: List<E>): List<RE> {
        val ids = entities.filter { it.id.isNotBlank() }.map { it.id }
        deleteAllTags(ids)

        val res = entities.map { toDatabaseObject(it) }
        dao.saveAll(res)

        val joins = entities.map { e ->
            e.tags.map { t ->
                createTagJoin(e.id, t.id)
            }
        }.flatten()

        saveTags(joins)
        return res
    }
}