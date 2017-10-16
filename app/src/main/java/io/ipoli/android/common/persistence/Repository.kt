package io.ipoli.android.common.persistence

import io.ipoli.android.quest.Entity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/18/17.
 */
interface Repository<T> where T : Entity {
    fun listenById(id: String): Observable<T>
    fun listen(): Observable<T>
    fun listenForAll(): Observable<List<T>>
    fun find(): Single<T>
    fun save(entity: T): Single<T>
    fun delete(entity: T): Completable
    fun delete(id: String): Completable
}