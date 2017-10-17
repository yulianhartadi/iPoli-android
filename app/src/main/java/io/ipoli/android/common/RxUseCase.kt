package io.ipoli.android.common

import io.reactivex.Observable
import kotlinx.coroutines.experimental.channels.Channel

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/1/17.
 */
interface RxUseCase<in Parameters, Result> {
    fun execute(parameters: Parameters): Observable<Result>
}

interface UseCase<in Parameters, out Result> {
    fun execute(parameters: Parameters): Result
}

abstract class StreamingUseCase<in Parameters, Result>() : UseCase<Parameters, Channel<Result>>

abstract class BaseRxUseCase<in Parameters, Result>() : RxUseCase<Parameters, Result> {

    abstract fun createObservable(parameters: Parameters): Observable<Result>

    override fun execute(parameters: Parameters): Observable<Result> {
        return createObservable(parameters)
    }
}

abstract class SimpleRxUseCase<Result> : BaseRxUseCase<Unit, Result>() {

    override fun execute(parameters: Unit): Observable<Result> {
        return createObservable(parameters)
    }
}