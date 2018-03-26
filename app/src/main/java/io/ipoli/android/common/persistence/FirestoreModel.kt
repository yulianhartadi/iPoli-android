package io.ipoli.android.common.persistence

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/20/17.
 */

interface FirestoreModel : PersistedModel {
    val map: Map<String, Any?>
}