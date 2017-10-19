package io.ipoli.android.common

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/1/17.
 */
interface UseCase<in Parameters, out Result> {
    fun execute(parameters: Parameters): Result
}

abstract class StreamingUseCase<in Parameters, out Result>(protected val coroutineContext: CoroutineContext) : UseCase<Parameters, ReceiveChannel<Result>>