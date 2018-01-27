package mypoli.android.common.redux

import kotlinx.coroutines.experimental.launch
import mypoli.android.common.redux.MiddleWare.Result.Continue
import mypoli.android.common.redux.MiddleWare.Result.Stop
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/20/2018.
 */
interface MiddleWare<in S : State> {
    fun execute(state: S, dispatcher: Dispatcher, action: Action): Result

    sealed class Result {
        object Continue : Result()
        object Stop : Result()
    }
}

interface SimpleMiddleware<in S : State> : MiddleWare<S> {

    override fun execute(state: S, dispatcher: Dispatcher, action: Action): MiddleWare.Result {
        onExecute(state, dispatcher, action)
        return Continue
    }

    fun onExecute(state: S, dispatcher: Dispatcher, action: Action)
}

class CompositeMiddleware<in S : State>(private val middleware: List<MiddleWare<S>>) :
    MiddleWare<S> {

    override fun execute(state: S, dispatcher: Dispatcher, action: Action): MiddleWare.Result {

        for (m in middleware) {
            if (m.execute(state, dispatcher, action) == Stop) {
                return Stop
            }
        }

        return Continue
    }
}

class SagaMiddleware<in S : State>(
    private val coroutineContext: CoroutineContext,
    private val sagas: List<Saga> = listOf()
) : MiddleWare<S> {

    override fun execute(state: S, dispatcher: Dispatcher, action: Action): MiddleWare.Result {

        sagas
            .filter { it.canHandle(action) }
            .forEach {
                launch(coroutineContext) {
                    it.execute(action, dispatcher)
                }
            }
        return Continue
    }
}