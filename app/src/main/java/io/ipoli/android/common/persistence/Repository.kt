package io.ipoli.android.common.persistence

import io.ipoli.android.quest.Entity
import kotlinx.coroutines.experimental.channels.Channel

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/18/17.
 */
interface Repository<T> where T : Entity {

    fun save(entity: T): T
    fun save(entities: List<T>): List<T>
}

interface EntityRepository<T> : Repository<T> where T : Entity {
    fun listen(channel: Channel<T?>): Channel<T?>
    fun find(): T?
}

interface CollectionRepository<T> : Repository<T> where T : Entity {

    fun findById(id: String): T?
    fun findAll(): List<T>
    fun listenById(id: String, channel: Channel<T?>): Channel<T?>
    fun listenForAll(channel: Channel<List<T>>): Channel<List<T>>
    fun remove(entity: T)
    fun remove(id: String)
    fun undoRemove(id: String)
}