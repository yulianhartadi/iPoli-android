package mypoli.android.common.middleware

import android.content.Context
import kotlinx.coroutines.experimental.channels.consumeEach
import mypoli.android.common.AppState
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.LoadDataAction
import mypoli.android.common.di.Module
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.AsyncMiddleware
import mypoli.android.common.redux.Dispatcher
import mypoli.android.common.redux.State
import mypoli.android.myPoliApp
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/23/2018.
 */
class LoadDataMiddleware(private val context: Context, coroutineContext: CoroutineContext) :
    AsyncMiddleware<AppState>(coroutineContext),
    Injects<Module> {

    private val playerRepository by required { playerRepository }

    override suspend fun onExecute(state: State, dispatcher: Dispatcher, action: Action) {
        inject(myPoliApp.module(context))
        val a = action as LoadDataAction
        when (a) {
            LoadDataAction.All -> {
                playerRepository.listen().consumeEach {
                    dispatcher.dispatch(DataLoadedAction.PlayerLoaded(it!!))
                }
            }
        }
    }

    override fun canHandle(action: Action): Boolean {
        return action is LoadDataAction
    }
}