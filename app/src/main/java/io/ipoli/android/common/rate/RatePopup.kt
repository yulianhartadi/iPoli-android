package io.ipoli.android.common.rate

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.IntentUtil
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.view.Popup
import kotlinx.android.synthetic.main.popup_rate.view.*
import space.traversal.kapsule.required

class RatePopup : Popup(isAutoHide = true) {

    private val eventLogger by required { eventLogger }

    override fun createView(inflater: LayoutInflater): View {
        @SuppressLint("InflateParams")
        val view = inflater.inflate(R.layout.popup_rate, null)
        changeTitle(view, R.string.rate_dialog_initial_title)
        view.positive.setText(R.string.dialog_yes)
        view.negative.setText(R.string.dialog_no)
        view.neutral.setText(R.string.rate_dialog_never_ask_again)

        view.rateDialogHeaderIcon.setImageResource(R.drawable.logo)
        view.neutral.visibility = View.VISIBLE

        return view
    }

    override fun onViewShown(contentView: View) {

        contentView.positive.setOnClickListener {
            logEvent("rate_initial", "answer", "yes")
            showRate(contentView)
        }
        contentView.negative.setOnClickListener {
            logEvent("rate_initial", "answer", "no")
            showFeedback(contentView)
        }
        contentView.neutral.setOnClickListener {
            logEvent("rate_initial", "answer", "never")
            saveDoNotShowAgainPref(contentView.context)
            hide()
        }
    }

    private fun showRate(
        view: View
    ) {
        view.neutral.visibility = View.INVISIBLE
        changeTitle(view, R.string.rate_dialog_rate_title)
        ViewUtils.goneViews(view.feedbackLayout)
        ViewUtils.showViews(view.rate)
        view.positive.setText(R.string.dialog_lets_go)
        view.negative.setText(R.string.dialog_later)
        view.positive.setOnClickListener {
            logEvent("rate_positive", "answer", "yes")
            saveDoNotShowAgainPref(view.context)
            view.context.startActivity(IntentUtil.startRatePage(view.context))
            hide()
        }
        view.negative.setOnClickListener {
            logEvent("rate_positive", "answer", "no")
            hide()
        }
        view.viewSwitcher.showNext()
    }

    private fun showFeedback(
        view: View
    ) {
        view.neutral.visibility = View.INVISIBLE
        changeTitle(view, R.string.rate_dialog_feedback_title)
        ViewUtils.goneViews(view.rate)
        ViewUtils.showViews(view.feedbackLayout)
        view.positive.setText(R.string.rate_dialog_feedback_send)
        view.negative.setText(R.string.rate_dialog_feedback_no)

        view.positive.setOnClickListener {
            val feedback = view.feedback.text.toString()
            if (feedback.isNotEmpty()) {
                logEvent("rate_negative", "feedback", feedback)
                Toast.makeText(view.context, R.string.thank_you, Toast.LENGTH_SHORT).show()
            }
            hide()
        }
        view.negative.setOnClickListener {
            logEvent("rate_negative", "answer", "no")
            hide()
        }

        view.viewSwitcher.showNext()
    }

    private fun logEvent(name: String, paramName: String, paramValue: String) {
        eventLogger.logEvent(
            name,
            mapOf(paramName to paramValue)
        )
    }

    private fun changeTitle(view: View, @StringRes title: Int) {
        view.rateDialogHeaderTitle.setText(title)
    }

    private fun saveDoNotShowAgainPref(context: Context) {
        val pm = PreferenceManager.getDefaultSharedPreferences(context)
        pm.edit().putBoolean(Constants.KEY_SHOULD_SHOW_RATE_DIALOG, false).apply()
    }
}