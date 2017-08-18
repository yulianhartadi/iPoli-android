package io.ipoli.android.common.persistence

import io.reactivex.Single
import io.realm.RealmObject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/18/17.
 */
interface Repository<T> where T : PersistedModel, T : RealmObject {
    fun save(model: T): Single<T>
}