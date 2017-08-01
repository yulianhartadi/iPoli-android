package io.ipoli.android

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by vini on 8/1/17.
 */
interface RxUseCase<in Parameters, Result> {

    fun execute(params: Parameters): Observable<Result>
}

abstract class BaseRxUseCase<in Parameters, Result> : RxUseCase<Parameters, Result> {
    abstract fun createObservable(params: Parameters): Observable<Result>

    fun createSubscribeOnScheduler() = Schedulers.io()

    fun createObserveOnScheduler() = AndroidSchedulers.mainThread()

    override fun execute(params: Parameters): Observable<Result> {
        return createObservable(params)
                .subscribeOn(createSubscribeOnScheduler())
                .observeOn(createObserveOnScheduler())
    }
}

abstract class SimpleRxUseCase<Result> : BaseRxUseCase<Unit, Result>() {

    override fun execute(params: Unit): Observable<Result> {
        return createObservable(params)
                .subscribeOn(createSubscribeOnScheduler())
                .observeOn(createObserveOnScheduler())
    }
}