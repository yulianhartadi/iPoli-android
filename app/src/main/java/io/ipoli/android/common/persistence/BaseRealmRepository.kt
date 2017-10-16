package io.ipoli.android.common.persistence

import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import io.ipoli.android.quest.Entity
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.realm.*
import java.util.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/18/17.
 */
abstract class BaseRealmRepository<E, T> : Repository<E>  where E : Entity, T : RealmObject, T : PersistedModel {

    protected abstract fun getModelClass(): Class<T>

    override fun listenById(id: String): Observable<E> = listen { it.equalTo("id", id) }

    override fun listen(): Observable<E> = listen { it }

    protected fun listen(query: (RealmQuery<T>) -> RealmQuery<T>): Observable<E> =
        createObservable { emitter ->
            val (realm, q) = createRealmAndQuery(query)
            val result = q.findFirstAsync()
            result.addChangeListener<T> { it ->
                if (it.isLoaded && !emitter.isDisposed) {
                    emitter.onNext(convertToEntity(it))
                }
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
            })
        }

    override fun listenForAll(): Observable<List<E>> = listenForAll { it }

    protected fun listenForAll(query: (RealmQuery<T>) -> RealmQuery<T>): Observable<List<E>> =
        createObservable { emitter ->
            val (realm, q) = createRealmAndQuery(query)
            val result = q.findAllAsync()
            result.addChangeListener { it ->
                if (it.isLoaded && !emitter.isDisposed) {
                    emitter.onNext(convertAll(it))
                }
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
            })
        }

    private fun convertAll(realmResults: RealmResults<T>) = realmResults.map { convertToEntity(it) }

    protected fun listenForAllSorted(
        query: (RealmQuery<T>) -> RealmQuery<T>,
        sortOrder: List<Pair<String, Sort>>
    ): Observable<List<E>> =
        createObservable { emitter ->
            val (realm, q) = createRealmAndQuery(query)
            val fieldNames = sortOrder.map { it.first }.toTypedArray()
            val sorts = sortOrder.map { it.second }.toTypedArray()
            val result = q.findAllSortedAsync(fieldNames, sorts)
            result.addChangeListener { it ->
                if (it.isLoaded && !emitter.isDisposed) {
                    emitter.onNext(convertAll(it))
                }
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
            })
        }

    override fun find(): Single<E> = findOne { it }

    protected fun findOne(query: (RealmQuery<T>) -> RealmQuery<T>): Single<E> =
        createSingle { emitter ->
            val (realm, q) = createRealmAndQuery(query)
            val result = q.findFirstAsync()
            result.addChangeListener<T> { it ->
                if (it.isLoaded && !emitter.isDisposed) {
                    emitter.onSuccess(convertToEntity(it))
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

    override fun save(entity: E): Single<E> =
        createSingle { emitter ->
            Realm.getDefaultInstance().use {
                val realmModel = convertToRealmModel(entity)
                it.executeTransaction {
                    if (realmModel.id.isEmpty()) {
                        realmModel.id = UUID.randomUUID().toString()
                    }
                    it.copyToRealmOrUpdate(realmModel)
                }
                emitter.onSuccess(convertToEntity(realmModel))
            }
        }

    abstract fun convertToRealmModel(entity: E): T

    abstract fun convertToEntity(realmModel: T): E

    override fun delete(entity: E): Completable =
        delete(entity.id)

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