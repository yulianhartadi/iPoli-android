package mypoli.android.common.persistence

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import mypoli.android.quest.Entity

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/18/17.
 */
interface Repository<T> where T : Entity {
    fun listenById(id: String): ReceiveChannel<T?>
    fun listenForAll(): ReceiveChannel<List<T>>
    fun listen(): ReceiveChannel<T?>
    fun find(): T?
    fun findById(id: String): T?
    fun save(entity: T): T
    fun remove(entity: T)
    fun remove(id: String)
    fun undoRemove(id: String)
}