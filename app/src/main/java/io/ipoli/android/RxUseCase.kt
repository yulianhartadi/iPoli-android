package io.ipoli.android

import android.os.HandlerThread
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/17.
 */
interface RxUseCase<in Parameters, Result> {

    var subscribeOnScheduler: Scheduler?

    fun execute(params: Parameters): Observable<Result>
}

abstract class BaseRxUseCase<in Parameters, Result>() : RxUseCase<Parameters, Result> {

    override var subscribeOnScheduler: Scheduler? = null
        set(value) {
            field = value
        }

    abstract fun createObservable(params: Parameters): Observable<Result>

    private fun createSubscribeOnScheduler(): Scheduler {
        val t = HandlerThread("worker")
        if (!t.isAlive)
            t.start()
        return AndroidSchedulers.from(t.looper)
    }

    fun createObserveOnScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    protected fun getSubscribeScheduler(): Scheduler {
        if (subscribeOnScheduler == null) {
            subscribeOnScheduler = createSubscribeOnScheduler()
        }
        return subscribeOnScheduler!!
    }

    override fun execute(params: Parameters): Observable<Result> {
        return createObservable(params)
                .subscribeOn(getSubscribeScheduler())
                .unsubscribeOn(getSubscribeScheduler())
                .observeOn(createObserveOnScheduler())
    }
}

abstract class SimpleRxUseCase<Result> : BaseRxUseCase<Unit, Result>() {

    override fun execute(params: Unit): Observable<Result> {
        return createObservable(params)
                .subscribeOn(getSubscribeScheduler())
                .unsubscribeOn(getSubscribeScheduler())
                .observeOn(createObserveOnScheduler())
    }
}