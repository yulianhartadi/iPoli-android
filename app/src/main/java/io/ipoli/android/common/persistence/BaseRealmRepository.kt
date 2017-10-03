package io.ipoli.android.common.persistence

import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import io.realm.Sort
import java.util.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/18/17.
 */
abstract class BaseRealmRepository<T> : Repository<T> where T : PersistedModel, T : RealmObject {

    protected abstract fun getModelClass(): Class<T>

    override fun listenById(id: String): Observable<T> = listen { it.equalTo("id", id) }

    override fun listen(): Observable<T> = listen { it }

    protected fun listen(query: (RealmQuery<T>) -> RealmQuery<T>): Observable<T> =
        createObservable { emitter ->
            val (realm, q) = createRealmAndQuery(query)
            val result = q.findFirstAsync()
            result.addChangeListener<T> { it ->
                if (it.isLoaded && !emitter.isDisposed) {
                    emitter.onNext(realm.copyFromRealm(it))
                }
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
            })
        }

    override fun listenForAll(): Observable<List<T>> = listenForAll { it }

    protected fun listenForAll(query: (RealmQuery<T>) -> RealmQuery<T>): Observable<List<T>> =
        createObservable { emitter ->
            val (realm, q) = createRealmAndQuery(query)
            val result = q.findAllAsync()
            result.addChangeListener { it ->
                if (it.isLoaded && !emitter.isDisposed) {
                    emitter.onNext(realm.copyFromRealm(it))
                }
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
            })
        }

    protected fun listenForAllSorted(
        query: (RealmQuery<T>) -> RealmQuery<T>,
        sortOrder: List<Pair<String, Sort>>
    ): Observable<List<T>> =
        createObservable { emitter ->
            val (realm, q) = createRealmAndQuery(query)
            val fieldNames = sortOrder.map { it.first }.toTypedArray()
            val sorts = sortOrder.map { it.second }.toTypedArray()
            val result = q.findAllSortedAsync(fieldNames, sorts)
            result.addChangeListener { it ->
                if (it.isLoaded && !emitter.isDisposed) {
                    emitter.onNext(realm.copyFromRealm(it))
                }
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
            })
        }

    override fun find(): Single<T> = findOne { it }

    protected fun findOne(query: (RealmQuery<T>) -> RealmQuery<T>): Single<T> =
        createSingle { emitter ->
            val (realm, q) = createRealmAndQuery(query)
            val result = q.findFirstAsync()
            result.addChangeListener<T> { it ->
                if (it.isLoaded && !emitter.isDisposed) {
                    emitter.onSuccess(realm.copyFromRealm(it))
                }
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
            })
        }

    protected fun getLooper(): Looper? {
        val backgroundThread = BackgroundThread()
        backgroundThread.start()
        return backgroundThread.looper
    }

    override fun save(model: T): Single<T> =
        createSingle { emitter ->
            Realm.getDefaultInstance().use {
                it.executeTransaction {
                    if (model.id.isEmpty()) {
                        model.id = UUID.randomUUID().toString()
                    }
                    it.copyToRealmOrUpdate(model)
                }
                emitter.onSuccess(model)
            }
        }

    override fun delete(model: T): Completable =
        delete(model.id)

    override fun delete(id: String): Completable =
        createCompletable { emitter ->
            Realm.getDefaultInstance().use {
                it.executeTransaction {
                    val result = it.where(getModelClass()).equalTo("id", id).findFirst()
                    result?.deleteFromRealm()
                }
                emitter.onComplete()
            }
        }

    protected fun <R> createObservable(emitter: (ObservableEmitter<R>) -> Unit): Observable<R> {
        val looper = getLooper()
        return Observable.create<R>(emitter).doFinally {
            if (Looper.getMainLooper() != looper) {
                looper?.thread?.interrupt()
            }
        }
            .subscribeOn(AndroidSchedulers.from(looper))
            .unsubscribeOn(AndroidSchedulers.from(looper))
    }

    protected fun <R> createSingle(emitter: (SingleEmitter<R>) -> Unit): Single<R> {
        val looper = getLooper()
        return Single.create<R>(emitter).doFinally {
            if (Looper.getMainLooper() != looper) {
                looper?.thread?.interrupt()
            }
        }
            .subscribeOn(AndroidSchedulers.from(looper))
            .unsubscribeOn(AndroidSchedulers.from(looper))
    }

    protected fun createCompletable(emitter: (CompletableEmitter) -> Unit): Completable {
        val looper = getLooper()
        return Completable.create(emitter).doFinally {
            if (Looper.getMainLooper() != looper) {
                looper?.thread?.interrupt()
            }
        }
            .subscribeOn(AndroidSchedulers.from(looper))
            .unsubscribeOn(AndroidSchedulers.from(looper))
    }

    private fun createRealmAndQuery(query: (RealmQuery<T>) -> RealmQuery<T>): Pair<Realm, RealmQuery<T>> {
        val realm = Realm.getDefaultInstance()
        val q = query(realm.where(getModelClass()))
        return Pair(realm, q)
    }
}

class BackgroundThread : HandlerThread("Scheduler-Realm-BackgroundThread",
    Process.THREAD_PRIORITY_BACKGROUND)