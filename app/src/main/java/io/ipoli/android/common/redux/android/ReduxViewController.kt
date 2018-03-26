package io.ipoli.android.common.redux.android

import android.content.Context
import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import io.ipoli.android.common.AppState
import io.ipoli.android.common.NamespaceAction
import io.ipoli.android.common.UIAction
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.StateStore
import io.ipoli.android.common.redux.ViewStateReducer
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.AndroidIcon
import io.ipoli.android.common.view.attrData
import io.ipoli.android.myPoliApp
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 1/18/18.
 */
abstract class ReduxViewController<A : Action, VS : ViewState, out R : ViewStateReducer<AppState, VS>> protected constructor(
    args: Bundle? = null
) : RestoreViewOnCreateController(args), Injects<Module>,
    StateStore.StateChangeSubscriber<AppState> {

    private val stateStore by required { stateStore }
    private val eventLogger by required { eventLogger }

    protected open var namespace: String? = null

    protected abstract val reducer: R

    @Volatile
    private var currentState: VS? = null

    init {
        val lifecycleListener = object : LifecycleListener() {

            override fun postCreateView(controller: Controller, view: View) {
                stateStore.dispatch(UIAction.Attach(reducer))
            }

            override fun postAttach(controller: Controller, view: View) {
                eventLogger.logCurrentScreen(activity!!, javaClass.simpleName, javaClass)
                stateStore.subscribe(this@ReduxViewController)
                onCreateLoadAction()?.let {
                    dispatch(it)
                }
            }

            override fun preDetach(controller: Controller, view: View) {
                stateStore.unsubscribe(this@ReduxViewController)
            }

            override fun preDestroyView(controller: Controller, view: View) {
                stateStore.dispatch(UIAction.Detach(reducer))
                currentState = null
            }
        }
        addLifecycleListener(lifecycleListener)
    }

    override fun onContextAvailable(context: Context) {
        inject(myPoliApp.module(context))
    }

    override fun onAttach(view: View) {
        colorLayoutBars()
        super.onAttach(view)
    }

    protected open fun colorLayoutBars() {
        activity?.window?.navigationBarColor = attrData(io.ipoli.android.R.attr.colorPrimary)
        activity?.window?.statusBarColor = attrData(io.ipoli.android.R.attr.colorPrimaryDark)
    }

    fun dispatch(action: A) {
        val a = namespace?.let {

            NamespaceAction(action, it)
        } ?: action
        stateStore.dispatch(a)
    }

    override fun onStateChanged(newState: AppState) {
        val viewState = newState.stateFor<VS>(reducer.stateKey)
        if (viewState != currentState) {
            currentState = viewState
            launch(UI) {
                onRenderViewState(viewState)
            }
        }
    }

    protected open fun onRenderViewState(state: VS) {
        render(state, view!!)
    }

    protected open fun onCreateLoadAction(): A? {
        return null
    }

    abstract fun render(state: VS, view: View)

    fun View.dispatchOnClick(action: A) {
        dispatchOnClickAndExec(action, {})
    }

    fun View.dispatchOnClickAndExec(action: A, block: () -> Unit) {
        setOnClickListener {
            dispatch(action)
            block()
        }
    }

    protected val Color.androidColor: AndroidColor
        get() = AndroidColor.valueOf(this.name)

    protected val AndroidColor.color: Color
        get() = Color.valueOf(this.name)

    protected val Icon.androidIcon: AndroidIcon
        get() = AndroidIcon.valueOf(this.name)

    protected val AndroidIcon.toIcon: Icon
        get() = Icon.valueOf(this.name)
}