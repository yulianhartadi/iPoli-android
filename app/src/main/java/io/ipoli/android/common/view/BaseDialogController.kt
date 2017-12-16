package io.ipoli.android.common.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.MainThread
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler
import io.ipoli.android.common.mvi.*

/**
 * A controller that displays a dialog window, floating on top of its activity's window.
 * This is a wrapper over [Dialog] object like [android.app.DialogFragment].
 *
 *
 * Implementations should override this class and implement [.onCreateDialog] to create a custom dialog, such as an [android.app.AlertDialog]
 */
abstract class BaseDialogController : RestoreViewOnCreateController {

    private lateinit var dialog: Dialog
    private var dismissed: Boolean = false

    /**
     * Convenience constructor for use when no arguments are needed.
     */
    protected constructor() : super()

    /**
     * Constructor that takes arguments that need to be retained across restarts.
     *
     * @param args Any arguments that need to be retained.
     */
    protected constructor(args: Bundle?) : super(args)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        dialog = onCreateDialog(savedViewState)
        dialog.ownerActivity = activity!!
        dialog.setOnDismissListener { dismissDialog() }
        if (savedViewState != null) {
            val dialogState = savedViewState.getBundle(SAVED_DIALOG_STATE_TAG)
            if (dialogState != null) {
                dialog.onRestoreInstanceState(dialogState)
            }
        }
        return View(activity)
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        val dialogState = dialog.onSaveInstanceState()
        outState.putBundle(SAVED_DIALOG_STATE_TAG, dialogState)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        dialog.show()
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        dialog.hide()
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        dialog.setOnDismissListener(null)
        dialog.dismiss()
    }

    /**
     * Display the dialog, create a transaction and pushing the controller.
     * @param router The router on which the transaction will be applied
     * @param tag The tag for this controller
     */
    fun showDialog(router: Router, tag: String?) {
        dismissed = false
        router.pushController(RouterTransaction.with(this)
                .pushChangeHandler(SimpleSwapChangeHandler(false))
                .popChangeHandler(SimpleSwapChangeHandler(false))
                .tag(tag))
    }

    /**
     * Dismiss the dialog and pop this controller
     */
    fun dismissDialog() {
        if (dismissed) {
            return
        }
        router.popController(this)
        dismissed = true
    }

    /**
     * Build your own custom Dialog container such as an [android.app.AlertDialog]
     *
     * @param savedViewState A bundle for the view's state, which would have been created in [.onSaveViewState] or `null` if no saved state exists.
     * @return Return a new Dialog instance to be displayed by the Controller
     */
    protected abstract fun onCreateDialog(savedViewState: Bundle?): Dialog

    companion object {

        private val SAVED_DIALOG_STATE_TAG = "android:savedDialogState"
    }
}

abstract class MviDialogController<VS : ViewState, in V : ViewStateRenderer<VS>, out P : MviPresenter<V, VS, I>, in I : Intent>(
        args: Bundle? = null
) : MviViewController<VS, V, P, I>(args) {
    data class DialogView(val dialog: AlertDialog, val view: View)

    protected lateinit var dialog: AlertDialog
    private lateinit var contentView: View
    private var dismissed: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        val dv = onCreateDialog(savedViewState)
        dialog = dv.dialog
        contentView = dv.view

        dialog.ownerActivity = activity!!
        dialog.setOnDismissListener { dismissDialog() }
        if (savedViewState != null) {
            val dialogState = savedViewState.getBundle(SAVED_DIALOG_STATE_TAG)
            if (dialogState != null) {
                dialog.onRestoreInstanceState(dialogState)
            }
        }
        return View(activity)
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        val dialogState = dialog.onSaveInstanceState()
        outState.putBundle(SAVED_DIALOG_STATE_TAG, dialogState)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        dialog.show()
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        dialog.hide()
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        dialog.setOnDismissListener(null)
        dialog.dismiss()
    }

    /**
     * Display the dialog, create a transaction and pushing the controller.
     * @param router The router on which the transaction will be applied
     * @param tag The tag for this controller
     */
    fun showDialog(router: Router, tag: String?) {
        dismissed = false
        router.pushController(RouterTransaction.with(this)
                .pushChangeHandler(SimpleSwapChangeHandler(false))
                .popChangeHandler(SimpleSwapChangeHandler(false))
                .tag(tag))
    }

    /**
     * Dismiss the dialog and pop this controller
     */
    fun dismissDialog() {
        if (dismissed) {
            return
        }
        router.popController(this)
        dismissed = true
    }

    @MainThread
    override fun render(state: VS) {
        render(state, contentView)
    }

    protected fun changeIcon(@DrawableRes icon: Int) {
        dialog.setIcon(icon)
    }

    protected fun changeTitle(@StringRes title: Int) {
        dialog.setTitle(title)
    }

    protected fun changeNeutralButtonText(@StringRes text: Int) {
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setText(text)
    }

    protected fun changePositiveButtonText(@StringRes text: Int) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(text)
    }

    protected fun changeNegativeButtonText(@StringRes text: Int) {
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(text)
    }

    protected fun setNeutralButtonListener(listener: (() -> Unit)?) {
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
            if (listener != null) listener()
            else dismissDialog()
        }
    }

    protected fun sePositiveButtonListener(listener: (() -> Unit)?) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            if (listener != null) listener()
            else dismissDialog()
        }
    }

    protected fun setNegativeButtonListener(listener: (() -> Unit)?) {
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            if (listener != null) listener()
            else dismissDialog()
        }
    }

    /**
     * Build your own custom Dialog container such as an [android.app.AlertDialog]
     *
     * @param savedViewState A bundle for the view's state, which would have been created in [.onSaveViewState] or `null` if no saved state exists.
     * @return Return a new Dialog instance to be displayed by the Controller
     */
    protected abstract fun onCreateDialog(savedViewState: Bundle?): DialogView

    companion object {

        private val SAVED_DIALOG_STATE_TAG = "android:savedDialogState"
    }
}