package mypoli.android.common.persistence

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/20/17.
 */

interface CouchbasePersistedModel : PersistedModel {
    val map: Map<String, Any?>
    var type: String
}