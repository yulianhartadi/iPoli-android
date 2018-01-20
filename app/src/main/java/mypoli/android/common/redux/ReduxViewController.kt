package mypoli.android.common.redux

import android.content.Context
import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import mypoli.android.common.di.Module
import mypoli.android.myPoliApp
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 1/18/18.
 */
abstract class ReduxViewController<in A : Action> protected constructor(args: Bundle? = null) :
    RestoreViewOnCreateController(args), Injects<Module>,
    StateChangeSubscriber<AppState> {

    private val stateStore by required { stateStore }

    override fun onContextAvailable(context: Context) {
        inject(myPoliApp.module(context))
    }

    init {
        val lifecycleListener = object : LifecycleListener() {

            override fun postCreateView(controller: Controller, view: View) {
                stateStore.subscribe(this@ReduxViewController)
            }

            override fun preDestroyView(controller: Controller, view: View) {
                stateStore.unsubscribe(this@ReduxViewController)
            }

        }
        addLifecycleListener(lifecycleListener)
    }

    fun dispatch(action: A) {
        stateStore.dispatch(action)
    }

    override fun onStateChanged(newState: AppState) {
        render(newState, view!!)
    }

    abstract fun render(state: AppState, view: View)
}