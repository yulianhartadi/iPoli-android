package mypoli.android.common.redux

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.bluelinelabs.conductor.RouterTransaction
import mypoli.android.AppState
import mypoli.android.R
import mypoli.android.StateChangeSubscriber
import mypoli.android.common.di.Module
import mypoli.android.myPoliApp
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 1/18/18.
 */
abstract class ReduxViewController protected constructor(args: Bundle? = null) :
    RestoreViewOnCreateController(args), Injects<Module>,
    StateChangeSubscriber<AppState> {

    val stateStore by required { stateStore }

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

    override fun onStateChanged(newState: AppState) {
        render(view!!, newState)
    }

    abstract fun render(view: View, newState: AppState)
}

class TestViewController(args: Bundle? = null) : ReduxViewController(args) {

    override fun render(view: View, newState: AppState) {
        Timber.d("AAA Render $newState")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        return inflater.inflate(R.layout.controller_home, container, false)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        Timber.d("AAA store $stateStore")
        router.pushController(RouterTransaction.with(Test2ViewController()))
//        stateStore.dispatch(PlayerAction.Load)
    }
}

class Test2ViewController(args: Bundle? = null) : ReduxViewController(args) {

    override fun render(view: View, newState: AppState) {
        Timber.d("AAA Render $newState")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        return inflater.inflate(R.layout.controller_home, container, false)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        Timber.d("AAA store $stateStore")
//        stateStore.dispatch(PlayerAction.Load)
    }
}