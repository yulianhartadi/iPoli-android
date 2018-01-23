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

    fun canHandle(action: Action): Boolean {
        return true
    }

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

    override fun canHandle(action: Action): Boolean {
        for (m in middleware) {
            if (m.canHandle(action)) {
                return true
            }
        }
        return false
    }
}

class AsyncActionHandlerMiddleware<in S : State>(
    private val coroutineContext: CoroutineContext
) : MiddleWare<S> {

    override fun execute(state: S, dispatcher: Dispatcher, action: Action): MiddleWare.Result {
        if (action is AsyncAction) {
            launch(coroutineContext) {
                action.execute(dispatcher)
            }

            return Stop
        }
        return Continue

    }
}

abstract class AsyncMiddleware<in S : State>(
    private val coroutineContext: CoroutineContext
) : MiddleWare<S> {

    override fun execute(state: S, dispatcher: Dispatcher, action: Action): MiddleWare.Result {
        launch(coroutineContext) {
            onExecute(state, dispatcher, action)
        }
        return Continue
    }

    abstract suspend fun onExecute(state: State, dispatcher: Dispatcher, action: Action)
}