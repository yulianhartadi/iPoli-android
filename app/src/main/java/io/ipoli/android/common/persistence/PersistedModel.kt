package io.ipoli.android.common.persistence

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/18/17.
 */
interface PersistedModel {
    var id: String
    var updatedAt: Long
    var createdAt: Long
    var removedAt: Long?
}