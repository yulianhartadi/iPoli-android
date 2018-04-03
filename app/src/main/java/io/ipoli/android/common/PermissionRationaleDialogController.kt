package io.ipoli.android.common

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import io.ipoli.android.R
import io.ipoli.android.common.view.BaseDialogController
import io.ipoli.android.common.view.stringRes
import kotlinx.android.synthetic.main.view_dialog_header.view.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/2/18.
 */
class PermissionRationaleDialogController(args: Bundle? = null) : BaseDialogController(args) {

    private lateinit var message: String

    private lateinit var positiveListener: () -> Unit

    private lateinit var negativeListener: () -> Unit

    constructor(
        message: String,
        positiveListener: () -> Unit,
        negativeListener: () -> Unit
    ) : this() {
        this.message = message
        this.positiveListener = positiveListener
        this.negativeListener = negativeListener
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_permission_rationale, null)
        (view as TextView).text = if (message.isNotEmpty())
            message
        else
            stringRes(R.string.permission_rationale_default_message)
        return view
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog =
        dialogBuilder
            .setPositiveButton(R.string.dialog_ok, { _, _ ->
                positiveListener()
            })
            .setNegativeButton(R.string.cancel, { _, _ ->
                negativeListener()
            })
            .create()

    override fun onHeaderViewCreated(headerView: View?) {
        headerView!!.dialogHeaderTitle.setText(R.string.permission_rationale_title)
        headerView.dialogHeaderIcon.setImageResource(R.drawable.logo)
    }

}