package io.ipoli.android.common.persistence

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */

interface CouchbasePersistedModel : PersistedModel {
    var type: String
}