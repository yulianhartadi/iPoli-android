package io.ipoli.android.common

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/1/17.
 */
interface UseCase<in Parameters, out Result> {
    fun execute(parameters: Parameters): Result
}