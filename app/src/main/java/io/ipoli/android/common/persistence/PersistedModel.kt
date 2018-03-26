package io.ipoli.android.common.persistence

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/18/17.
 */
interface PersistedModel {
    var id: String
    var updatedAt: Long
    var createdAt: Long
    var removedAt: Long?
}