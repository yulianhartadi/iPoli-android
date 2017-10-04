package io.ipoli.android.common.ui

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import io.ipoli.android.Constants.Companion.REMINDER_PREDEFINED_MINUTES
import io.ipoli.android.R
import io.ipoli.android.common.parser.ReminderMinutesParser
import io.ipoli.android.quest.data.Reminder
import kotlinx.android.synthetic.main.dialog_reminder_picker.view.*

typealias TimeUnitConverter = java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/4/17.
 */

enum class TimeUnit(val minutes: Long) {

    MINUTES(1), HOURS(60), DAYS(TimeUnitConverter.DAYS.toMinutes(1)), WEEKS(TimeUnitConverter.DAYS.toMinutes(7));
}

class ReminderPickerDialogController : BaseDialogController {

    private lateinit var messageView: TextInputEditText

    private lateinit var predefinedTimesView: Spinner

    private lateinit var customTimeContainer: ViewGroup

    private lateinit var customTimeView: TextInputEditText

    private lateinit var customTimeUnitsView: Spinner

    private var isCustom: Boolean = false

    private var listener: ReminderPickedListener? = null

    private var reminder: Reminder? = null

    constructor(listener: ReminderPickedListener, selectedReminder: Reminder? = null) : super() {
        this.listener = listener
        this.reminder = selectedReminder
    }

    protected constructor() : super()

    protected constructor(args: Bundle?) : super(args)

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_reminder_picker, null)

        messageView = contentView.message
        predefinedTimesView = contentView.predefinedTimes
        customTimeContainer = contentView.customTimeContainer
        customTimeView = contentView.customTime
        customTimeUnitsView = contentView.customTimeUnits

        if (reminder != null) {
            val message = reminder!!.message!!
            if (message.isNotEmpty()) {
                messageView.setText(message)
                messageView.setSelection(message.length)
            }
            if (reminder!!.getMinutesFromStart() != 0L) {
                showCustomTimeForm()
            }
        }

        initPredefinedTimes()
        initCustomTimes()

        return AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle(R.string.reminder_dialog_title)
            .setIcon(R.drawable.pet_5_head)
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.do_not_remind, null)
            .create()
    }

    private fun showCustomTimeForm() {
        predefinedTimesView.visibility = View.GONE
        customTimeContainer.visibility = View.VISIBLE
        isCustom = true
    }

    private fun initCustomTimes() {
        val times = TimeUnit.values().map {
            //            times.add(context!!.getString(TimeOffsetType.getNameBeforeRes(type)).toLowerCase())
            it.name
        }
        val customTimeAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, times)
        customTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        customTimeUnitsView.adapter = customTimeAdapter

        if (reminder != null) {
            val parsedResult = ReminderMinutesParser.parseCustomMinutes(Math.abs(reminder!!.getMinutesFromStart()))
            if (parsedResult != null) {
                customTimeView.setText(parsedResult.first.toString())
                customTimeUnitsView.setSelection(parsedResult.second.ordinal)
            }
        }
    }

    private fun initPredefinedTimes() {
        val predefinedTimes = mutableListOf<String>()
        for (m in REMINDER_PREDEFINED_MINUTES) {
            predefinedTimes.add(m.toString())
//            predefinedTimes.add(ReminderTimeFormatter.formatMinutesBeforeReadable(context, REMINDER_PREDEFINED_MINUTE))
        }
        predefinedTimes.add(activity!!.getString(R.string.custom))

        val predefinedTimesAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, predefinedTimes)
        predefinedTimesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        predefinedTimesView.adapter = predefinedTimesAdapter
        predefinedTimesView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {
                if (position == predefinedTimesAdapter.count - 1) {
                    showCustomTimeForm()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    interface ReminderPickedListener {
        fun onReminderPicked(reminder: Reminder)
    }
}