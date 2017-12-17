package io.ipoli.android.common.view

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import io.ipoli.android.R
import kotlinx.android.synthetic.main.dialog_feedback.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */

class FeedbackDialogController : BaseDialogController {

    interface FeedbackListener {
        fun onSendFeedback(feedback: String)
        fun onChatWithUs()
    }

    private var listener: FeedbackListener? = null

    constructor(listener: FeedbackListener) : this() {
        this.listener = listener
    }

    constructor(args: Bundle? = null) : super(args)

    override fun onCreateDialog(dialogBuilder: AlertDialog.Builder, contentView: View, savedViewState: Bundle?): AlertDialog =
        dialogBuilder
            .setPositiveButton("Send", { _, _ ->
                val feedback = contentView.feedback.text.toString()
                listener?.onSendFeedback(feedback)
            })
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton("Chat with us", { _, _ ->
                listener?.onChatWithUs()
            })
            .create()

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View =
        inflater.inflate(R.layout.dialog_feedback, null)

    override fun onHeaderViewCreated(headerView: View) {
        headerView.dialogHeaderTitle.setText(R.string.rate_dialog_feedback_title)
        headerView.dialogHeaderIcon.setImageResource(R.drawable.logo)
    }
}