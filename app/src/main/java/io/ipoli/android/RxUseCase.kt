package io.ipoli.android

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/17.
 */
interface RxUseCase<in Parameters, Result> {

    fun execute(params: Parameters): Observable<Result>
}

abstract class BaseRxUseCase<in Parameters, Result>() : RxUseCase<Parameters, Result> {

    abstract fun createObservable(params: Parameters): Observable<Result>

    fun createObserveOnScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    override fun execute(params: Parameters): Observable<Result> {
        return createObservable(params)
            .observeOn(createObserveOnScheduler())
    }
}

abstract class SimpleRxUseCase<Result> : BaseRxUseCase<Unit, Result>() {

    override fun execute(params: Unit): Observable<Result> {
        return createObservable(params)
            .observeOn(createObserveOnScheduler())
    }
}