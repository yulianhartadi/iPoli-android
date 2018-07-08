package io.ipoli.android.common.persistence

import io.ipoli.android.common.datetime.Duration
import io.ipoli.android.common.datetime.Millisecond
import io.ipoli.android.quest.Entity
import kotlinx.coroutines.experimental.channels.Channel

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/18/17.
 */
interface Repository<T> where T : Entity {

    fun findAllForSync(lastSync: Duration<Millisecond>): List<T> {
        return emptyList()
    }

    fun save(entity: T): T
    fun save(entities: List<T>): List<T>
}

interface EntityRepository<T> : Repository<T> where T : Entity {
    fun listen(): Channel<T?>
    fun find(): T?
}

interface CollectionRepository<T> : Repository<T> where T : Entity {

    fun findById(id: String): T?
    fun findAll(): List<T>
    fun listenById(id: String): Channel<T?>
    fun listenForAll(): Channel<List<T>>
    fun remove(entity: T)
    fun remove(id: String)
    fun undoRemove(id: String)
}