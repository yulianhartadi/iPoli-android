package io.ipoli.android.common

import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import io.ipoli.android.MyPoliApp
import io.ipoli.android.R
import io.ipoli.android.common.di.BackgroundModule
import io.ipoli.android.common.view.*
import kotlinx.android.synthetic.main.dialog_help.view.*
import kotlinx.android.synthetic.main.view_dialog_header.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 8/7/18.
 */
class HelpDialogViewController : BaseDialogController, Injects<BackgroundModule> {

    private val eventLogger by required { eventLogger }

    private var title: String = ""
    private var message: String = ""

    constructor(
        title: String,
        message: String
    ) : this() {
        this.title = title
        this.message = message
    }

    constructor(args: Bundle? = null) : super(args)

    override fun onHeaderViewCreated(headerView: View?) {
        headerView!!.dialogHeaderTitle.text = title
        headerView.dialogHeaderIcon.setImageResource(R.drawable.logo)
        val background = headerView.dialogHeaderIcon.background as GradientDrawable
        background.setColor(colorRes(R.color.md_light_text_70))
    }

    override fun onCreateContentView(inflater: LayoutInflater, savedViewState: Bundle?): View {
        inject(MyPoliApp.backgroundModule(MyPoliApp.instance))
        val view = inflater.inflate(R.layout.dialog_help, null)
        view.helpMessage.setMarkdown(message)
        return view
    }

    override fun onCreateDialog(
        dialogBuilder: AlertDialog.Builder,
        contentView: View,
        savedViewState: Bundle?
    ): AlertDialog {
        val dialog = dialogBuilder
            .setPositiveButton(R.string.got_it, null)
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.not_helpful, null)
            .create()
        dialog.setOnShowListener { _ ->
            val neutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
            val positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            negative.gone()
            neutral.setOnClickListener { _ ->
                contentView.helpSwitcher.showNext()
                setupFeedback(positive, negative, neutral, contentView)
            }
        }

        return dialog
    }

    private fun setupFeedback(
        positive: Button,
        negative: Button,
        neutral: Button,
        contentView: View
    ) {
        positive.setText(R.string.send)
        neutral.setText(R.string.back)
        neutral.setOnClickListener {
            contentView.helpSwitcher.showNext()
            setupFeedback(positive, negative, neutral, contentView)
        }
        negative.visible()

        positive.setOnClickListener { _ ->
            eventLogger.logEvent(
                "help_section_feedback", mapOf(
                    "screen" to title,
                    "feedback" to contentView.helpFeedback.text.toString()
                )
            )
            dismiss()
        }
    }
}