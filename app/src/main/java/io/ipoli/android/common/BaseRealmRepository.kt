package io.ipoli.android.common

import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/18/17.
 */
abstract class BaseRealmRepository<T : RealmObject> {

    protected abstract fun getModelClass(): Class<T>

    protected fun queryAll(): Observable<List<T>> {
        val looper = getLooper()
        return Observable.create<List<T>>({ emitter ->
            val realm = Realm.getDefaultInstance()
            val result = RealmQuery.createQuery(realm, getModelClass()).findAllAsync()
            result.addChangeListener { it ->
                emitter.onNext(realm.copyFromRealm(it))
            }
            emitter.setDisposable(Disposables.fromAction {
                result.removeAllChangeListeners()
                realm.close()
                if (Looper.getMainLooper() != looper) {
                    looper?.thread?.interrupt()
                }
            })
        })
                .subscribeOn(AndroidSchedulers.from(looper))
                .unsubscribeOn(AndroidSchedulers.from(looper))
    }

    protected fun query(query: (RealmQuery<T>) -> Unit): Observable<List<T>> {
        val looper = getLooper()
        return Observable.create<List<T>>({ emitter ->
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
                if (Looper.getMainLooper() != looper) {
                    looper?.thread?.interrupt()
                }
            })
        })
                .subscribeOn(AndroidSchedulers.from(looper))
                .unsubscribeOn(AndroidSchedulers.from(looper))
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
}

class BackgroundThread : HandlerThread("Scheduler-Realm-BackgroundThread",
        Process.THREAD_PRIORITY_BACKGROUND)