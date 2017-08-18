package io.ipoli.android.common.persistence

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.RealmObject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/18/17.
 */
interface Repository<T> where T : PersistedModel, T : RealmObject {
    fun findById(id: String): Observable<T>
    fun findFirst(): Observable<T>
    fun findAll(): Observable<List<T>>
    fun save(model: T): Single<T>
    fun delete(model: T): Completable
}