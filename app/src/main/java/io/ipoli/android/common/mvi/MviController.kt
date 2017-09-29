package io.ipoli.android.common.mvi

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.MainThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import io.reactivex.subjects.PublishSubject

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/8/17.
 */
abstract class MviController<VS, V : ViewStateRenderer<VS>, P : MviPresenter<V, VS>> : Controller, ViewStateRenderer<VS> {

    init {
        addLifecycleListener(createMviLifecycleListener())
    }

    private fun createMviLifecycleListener() = object : LifecycleListener() {

        private var presenter: P? = null

        override fun postCreateView(controller: Controller, view: View) {

            val isRestoringViewState = presenter != null

            if (!isRestoringViewState) {
                presenter = createPresenter()
            }

            if (isRestoringViewState) {
                setRestoringViewState(true)
            }

            try {
                @Suppress("UNCHECKED_CAST")
                presenter?.onAttachView(this@MviController as V)
            } catch (e: ClassCastException) {
                throw RuntimeException("Your view " + this@MviController.javaClass.simpleName + " must implement the View interface ")
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

    protected abstract fun createPresenter(): P

    protected fun <I> createIntentSubject(): PublishSubject<I> {
        return PublishSubject.create<I>()
    }

    @MainThread
    override fun render(state: VS) {
        render(state, view!!)
    }

    abstract fun render(state: VS, view: View)
}