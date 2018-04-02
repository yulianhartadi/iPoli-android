package io.ipoli.android.common.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/30/18.
 */

abstract class BaseFullscreenDialogController : RestoreViewOnCreateController {

    protected lateinit var dialog: Dialog
    protected lateinit var contentView: View
    private var dismissed: Boolean = false

    protected constructor() : super()
    protected constructor(args: Bundle?) : super(args)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        contentView = onCreateContentView(inflater, savedViewState)

        dialog = Dialog(activity!!, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        dialog.setContentView(contentView)

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.window.setLayout(width, height)
        onDialogCreated(dialog, contentView)

        dialog.ownerActivity = activity!!
        dialog.setOnDismissListener { dismiss() }
        if (savedViewState != null) {
            val dialogState = savedViewState.getBundle(SAVED_DIALOG_STATE_TAG)
            if (dialogState != null) {
                dialog.onRestoreInstanceState(dialogState)
            }
        }
        return View(activity)
    }

    protected abstract fun onCreateContentView(
        inflater: LayoutInflater,
        savedViewState: Bundle?
    ): View

    protected open fun onDialogCreated(dialog: Dialog, contentView: View) {

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
    fun show(router: Router, tag: String? = null) {
        dismissed = false
        router.pushController(
            RouterTransaction.with(this)
                .pushChangeHandler(SimpleSwapChangeHandler(false))
                .popChangeHandler(SimpleSwapChangeHandler(false))
                .tag(tag)
        )
    }

    /**
     * Dismiss the dialog and pop this controller
     */
    fun dismiss() {
        if (dismissed) {
            return
        }
        router.popController(this)
        dismissed = true
    }

    companion object {
        private const val SAVED_DIALOG_STATE_TAG = "android:savedDialogState"
    }
}