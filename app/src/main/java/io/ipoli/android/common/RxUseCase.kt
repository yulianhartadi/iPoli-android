package io.ipoli.android.common

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/1/17.
 */
interface RxUseCase<in Parameters, Result> {

    fun execute(parameters: Parameters): Observable<Result>
}

abstract class BaseRxUseCase<in Parameters, Result>() : RxUseCase<Parameters, Result> {

    abstract fun createObservable(parameters: Parameters): Observable<Result>

    fun createObserveOnScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    override fun execute(parameters: Parameters): Observable<Result> {
        return createObservable(parameters)
            .observeOn(createObserveOnScheduler())
    }
}

abstract class SimpleRxUseCase<Result> : BaseRxUseCase<Unit, Result>() {

    override fun execute(parameters: Unit): Observable<Result> {
        return createObservable(parameters)
            .observeOn(createObserveOnScheduler())
    }
}