package mypoli.android.common.persistence

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import mypoli.android.quest.Entity

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/18/17.
 */
interface Repository<T> where T : Entity {
    fun save(entity: T): T
    fun save(entities: List<T>): List<T>
}

interface EntityRepository<T> : Repository<T> where T : Entity {
    fun listen(): ReceiveChannel<T?>
    fun find(): T?
}

interface CollectionRepository<T> : Repository<T> where T : Entity {
    fun findById(id: String): T?
    fun listenById(id: String): ReceiveChannel<T?>
    fun listenForAll(): ReceiveChannel<List<T>>
    fun remove(entity: T)
    fun remove(id: String)
    fun undoRemove(id: String)
}