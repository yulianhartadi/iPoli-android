package mypoli.android.common.persistence

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/20/17.
 */

interface CouchbasePersistedModel : PersistedModel {
    var type: String
}