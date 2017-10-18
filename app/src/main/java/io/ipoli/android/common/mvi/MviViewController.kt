package io.ipoli.android.common.mvi

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.MainThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import io.ipoli.android.quest.calendar.dayview.view.DayViewIntent
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/8/17.
 */
abstract class MviViewController<VS, V : ViewStateRenderer<VS>, P : MviPresenter<V, VS>> : Controller, ViewStateRenderer<VS> {

    private val presenterChannel: Channel<DayViewIntent> = Channel()

    init {
        val lifecycleListener = object : LifecycleListener() {

            private var presenter: P? = null

            override fun postCreateView(controller: Controller, view: View) {

                val isRestoringViewState = presenter != null

                if (!isRestoringViewState) {
                    presenter = createPresenter(presenterChannel)
                } else {
                    setRestoringViewState(true)
                }

                try {
                    @Suppress("UNCHECKED_CAST")
                    presenter?.onAttachView(this@MviViewController as V)
                } catch (e: ClassCastException) {
                    throw RuntimeException("Your view " + this@MviViewController.javaClass.simpleName + " must implement the View interface ")
                }

                if (isRestoringViewState) {
                    setRestoringViewState(false)
                }
            }

            override fun preDestroyView(controller: Controller, view: View) {
                val shouldRetainInstance = (controller.activity!!.isChangingConfigurations
                    || !controller.activity!!.isFinishing) && !controller.isBeingDestroyed

                if (shouldRetainInstance) {
                    presenter?.onDetachView()
                } else {
                    presenter?.onDestroy()
                    presenter = null
                }
            }

            override fun postDestroy(controller: Controller) {
                presenter = null
            }
        }
        addLifecycleListener(lifecycleListener)
    }

    @LayoutRes
    private val viewLayout: Int

    protected var isRestoring = false

    constructor(@LayoutRes viewLayout: Int) : super() {
        this.viewLayout = viewLayout
    }

    @Suppress("UNUSED")
    constructor(@LayoutRes viewLayout: Int, args: Bundle) : super(args) {
        this.viewLayout = viewLayout
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val v = inflater.inflate(viewLayout, container, false)
        bindView(v)
        return v
    }

    abstract fun bindView(view: View)

    private fun setRestoringViewState(isRestoring: Boolean) {
        this.isRestoring = isRestoring
    }

    protected abstract fun createPresenter(channel: ReceiveChannel<DayViewIntent>): P

    fun sendIntent(intent: DayViewIntent) {
        launch {
            presenterChannel.send(intent)
        }
    }

//    protected fun <I> createIntentSubject(): PublishSubject<I> {
//        return PublishSubject.create<I>()
//    }

    @MainThread
    override fun render(state: VS) {
        render(state, view!!)
    }

    abstract fun render(state: VS, view: View)
}