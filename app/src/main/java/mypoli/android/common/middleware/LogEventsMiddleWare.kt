package mypoli.android.common.middleware

import android.os.Bundle
import mypoli.android.common.AppState
import mypoli.android.common.di.Module
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Dispatcher
import mypoli.android.common.redux.MiddleWare
import mypoli.android.myPoliApp
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/25/2018.
 */
class LogEventsMiddleWare : MiddleWare<AppState>, Injects<Module> {

    private val eventLogger by required { eventLogger }

    override fun execute(
        state: AppState,
        dispatcher: Dispatcher,
        action: Action
    ): MiddleWare.Result {

        inject(myPoliApp.module(myPoliApp.instance))

        val params = Bundle()
        params.putString("action", action.javaClass.name)
        params.putString("action_data", action.toString())
        params.putString("state", state.toString())

        eventLogger.logEvent("dispatch_action", params)

        return MiddleWare.Result.Continue
    }

}