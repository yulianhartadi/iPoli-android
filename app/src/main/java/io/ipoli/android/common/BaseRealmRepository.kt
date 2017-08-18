package io.ipoli.android.common

import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import io.ipoli.android.common.persistence.PersistedModel
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import java.util.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/18/17.
 */
abstract class BaseRealmRepository<T> where T : PersistedModel, T : RealmObject {

    protected abstract fun getModelClass(): Class<T>

    fun queryById(id: String): Observable<T> = querySingle { it.equalTo("id", id) }

    fun queryFirst(): Observable<T> = querySingle {}

    protected fun querySingle(query: (RealmQuery<T>) -> Unit): Observable<T> =
        createObservable { emitter ->
            val realm = Realm.getDefaultInstance()
            val realmQuery = RealmQuery.createQuery(realm, getModelClass())
            query(realmQuery)
            val result = realmQuery.findFirstAsync()
            result.addChangeListener<T> { it ->
                emitter.onNext(realm.copyFromRealm(it))
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
            })
        }

    protected fun queryAll(): Observable<List<T>> = queryAll {}

    protected fun queryAll(query: (RealmQuery<T>) -> Unit): Observable<List<T>> =
        createObservable { emitter ->
            val realm = Realm.getDefaultInstance()
            val realmQuery = RealmQuery.createQuery(realm, getModelClass())
            query(realmQuery)
            val result = realmQuery.findAllAsync()
            result.addChangeListener { it ->
                emitter.onNext(realm.copyFromRealm(it))
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
            })
        }

    protected fun getLooper(): Looper? {
        return if (Looper.myLooper() != Looper.getMainLooper()) {
            val backgroundThread = BackgroundThread()
            backgroundThread.start()
            backgroundThread.looper
        } else {
            Looper.getMainLooper()
        }
    }

    fun save(model: T): Single<T> =
        createSingle { emitter ->
            Realm.getDefaultInstance().use {
                it.executeTransaction {
                    if (model.id.isEmpty()) {
                        model.id = UUID.randomUUID().toString()
                    }
                    it.copyToRealmOrUpdate(model)
                    emitter.onSuccess(model)
                }
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
}

class BackgroundThread : HandlerThread("Scheduler-Realm-BackgroundThread",
    Process.THREAD_PRIORITY_BACKGROUND)