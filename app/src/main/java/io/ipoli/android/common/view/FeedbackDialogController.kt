package io.ipoli.android.common.view

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import io.ipoli.android.R
import kotlinx.android.synthetic.main.dialog_feedback.view.*

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

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_feedback, null)

        return AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle(R.string.rate_dialog_feedback_title)
            .setIcon(R.drawable.logo)
            .setPositiveButton("Send", { _, _ ->
                listener?.onSendFeedback(contentView.feedback.text.toString())
            })
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton("Chat with us", { _, _ ->
                listener?.onChatWithUs()
            })
            .create()
    }
}