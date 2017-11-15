package io.ipoli.android.common

import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/1/17.
 */
interface UseCase<in Parameters, out Result> {
    fun execute(parameters: Parameters): Result
}

interface StreamingUseCase<in Parameters, out Result> : UseCase<Parameters, ReceiveChannel<Result>>