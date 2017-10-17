package io.ipoli.android.common.persistence

import io.ipoli.android.quest.Entity
import kotlinx.coroutines.experimental.channels.Channel

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/18/17.
 */
interface Repository<T> where T : Entity {
    fun listenById(id: String): Channel<T?>
    fun listenForAll(): Channel<List<T>>
    fun find(): T?
    fun save(entity: T): T
    fun delete(entity: T)
    fun delete(id: String)
}