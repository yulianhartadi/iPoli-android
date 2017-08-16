package io.ipoli.android

import android.os.HandlerThread
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers


/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 8/1/17.
 */
interface RxUseCase<in Parameters, Result> {

    fun execute(params: Parameters): Observable<Result>
}

abstract class BaseRxUseCase<in Parameters, Result>(private val subscribeOnScheduler: Scheduler?, private val observeOnScheduler: Scheduler?) : RxUseCase<Parameters, Result> {

    abstract fun createObservable(params: Parameters): Observable<Result>

    fun createSubscribeOnScheduler(): Scheduler {
        if (subscribeOnScheduler != null)
            return subscribeOnScheduler
//        return Schedulers.io()
        val t = HandlerThread("worker")
        if (!t.isAlive())
            t.start()
        return AndroidSchedulers.from(t.looper)
    }

    fun createObserveOnScheduler(): Scheduler {
        if (observeOnScheduler != null) {
            return observeOnScheduler
        }
        return AndroidSchedulers.mainThread()
    }

    override fun execute(params: Parameters): Observable<Result> {
        val subscribeOnScheduler = createSubscribeOnScheduler()
        return createObservable(params)
                .subscribeOn(subscribeOnScheduler)
                .unsubscribeOn(subscribeOnScheduler)
                .observeOn(createObserveOnScheduler())
    }
}

abstract class SimpleRxUseCase<Result>(subscribeOnScheduler: Scheduler?, observeOnScheduler: Scheduler?) : BaseRxUseCase<Unit, Result>(subscribeOnScheduler, observeOnScheduler) {

    override fun execute(params: Unit): Observable<Result> {
        return createObservable(params)
                .subscribeOn(createSubscribeOnScheduler())
                .observeOn(createObserveOnScheduler())
    }
}