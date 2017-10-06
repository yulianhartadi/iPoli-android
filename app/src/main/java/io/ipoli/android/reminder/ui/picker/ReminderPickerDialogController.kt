package io.ipoli.android.reminder.ui.picker

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import com.jakewharton.rxbinding2.widget.RxAdapterView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.ui.MviDialogController
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.data.Reminder
import io.reactivex.Observable
import kotlinx.android.synthetic.main.dialog_reminder_picker.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

typealias TimeUnitConverter = java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/4/17.
 */

enum class TimeUnit(val minutes: Long) {

    MINUTES(1),
    HOURS(60),
    DAYS(TimeUnitConverter.DAYS.toMinutes(1)),
    WEEKS(TimeUnitConverter.DAYS.toMinutes(7))
}

class ReminderPickerDialogController :
    MviDialogController<ReminderPickerViewState, ReminderPickerDialogController, ReminderPickerDialogPresenter>
    , ReminderPickerView, Injects<Module> {

    private val showCustomTimeSubject = createIntentSubject<Unit>()

    override fun editReminderIntent(): Observable<Reminder> =
        Observable.just(reminder != null)
            .filter { !isRestoring && it }.map { reminder!! }

    override fun newReminderIntent(): Observable<Unit> =
        Observable.just(Unit)
            .filter { !isRestoring && reminder == null }

    override fun showCustomTimeIntent() = showCustomTimeSubject

    private val presenter by required { reminderPickerPresenter }

    override fun createPresenter() = presenter

    override fun render(state: ReminderPickerViewState, dialogView: View) {

        when (state.type) {
            ReminderPickerViewState.StateType.NEW_REMINDER -> {
                ViewUtils.showViews(dialogView.predefinedTimes)
                ViewUtils.hideViews(dialogView.customTimeContainer)
                dialogView.message.setText(state.message)
                dialogView.message.setSelection(state.message.length)

                val predefinedTimesAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, state.predefinedValues)
                predefinedTimesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogView.predefinedTimes.adapter = predefinedTimesAdapter
                dialogView.predefinedTimes.setSelection(state.predefinedIndex!!)

                RxAdapterView.itemSelections(dialogView.predefinedTimes)
                    .filter { it == state.predefinedValues.size - 1 }
                    .map { Unit }
                    .subscribe(showCustomTimeSubject)
            }

            ReminderPickerViewState.StateType.EDIT_REMINDER -> {
                showCustomTimeViews(dialogView)
                dialogView.message.setText(state.message)
                dialogView.message.setSelection(state.message.length)

                val customTimeAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, state.timeUnits)
                customTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogView.customTimeUnits.adapter = customTimeAdapter
                dialogView.customTimeUnits.setSelection(state.timeValueIndex!!)
                dialogView.customTime.setText(state.timeValue)
            }

            ReminderPickerViewState.StateType.CUSTOM_TIME -> {
                showCustomTimeViews(dialogView)

                val customTimeAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, state.timeUnits)
                customTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogView.customTimeUnits.adapter = customTimeAdapter
                dialogView.customTimeUnits.setSelection(state.timeValueIndex!!)
                dialogView.customTime.setText(state.timeValue)

                dialogView.customTime.requestFocus()
                ViewUtils.showKeyboard(activity!!, dialogView.customTime)
            }
        }
//            is ReminderPickerViewState.NewReminderDataLoaded -> {

//            }

//            is ReminderPickerViewState.CustomTimeValueLoaded -> {
//
//            }
//
//            is ReminderPickerViewState.ShowCustomTimeValue -> {
//                showCustomTimeViews(dialogView)
//
//                val customTimeAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, state.timeUnits)
//                customTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                dialogView.customTimeUnits.adapter = customTimeAdapter
//                dialogView.customTimeUnits.setSelection(state.timeValueIndex)
//                dialogView.customTime.setText(state.timeValue)
//
//                dialogView.customTime.requestFocus()
//                ViewUtils.showKeyboard(activity!!, dialogView.customTime)
//            }
//        }
    }

    private fun showCustomTimeViews(dialogView: View) {
        ViewUtils.showViews(dialogView.customTimeContainer)
        ViewUtils.hideViews(dialogView.predefinedTimes)
    }

    override fun onContextAvailable(context: Context) {
        inject(iPoliApp.module(context, router))
    }

    private var listener: ReminderPickedListener? = null

    private var reminder: Reminder? = null

    constructor(listener: ReminderPickedListener, selectedReminder: Reminder? = null) : super() {
        this.listener = listener
        this.reminder = selectedReminder
    }

    protected constructor() : super()

    protected constructor(args: Bundle?) : super(args)

    override fun onCreateDialog(savedViewState: Bundle?): DialogView {
        val inflater = LayoutInflater.from(activity!!)

        val contentView = inflater.inflate(R.layout.dialog_reminder_picker, null)

//        messageView = contentView.message
//        predefinedTimesView = contentView.predefinedTimes
//        customTimeContainer = contentView.customTimeContainer
//        customTimeView = contentView.customTime
//        customTimeUnitsView = contentView.customTimeUnits

//        if (reminder != null) {
//            val message = reminder!!.message!!
//            if (message.isNotEmpty()) {
//                messageView.setText(message)
//                messageView.setSelection(message.length)
//            }
//            if (reminder!!.getMinutesFromStart() != 0L) {
//                showCustomTimeForm()
//            }
//        }
//
//        initPredefinedTimes()
//        initCustomTimes()

        val dialog = AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle(R.string.reminder_dialog_title)
            .setIcon(R.drawable.pet_5_head)
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.do_not_remind, null)
            .create()

        return DialogView(dialog, contentView)
    }
//
//    private fun showCustomTimeForm() {
//        predefinedTimesView.visibility = View.GONE
//        customTimeContainer.visibility = View.VISIBLE
//        isCustom = true
//    }
//
//    private fun initCustomTimes() {
//        val times = TimeUnit.values().map {
//            //            times.add(context!!.getString(TimeOffsetType.getNameBeforeRes(type)).toLowerCase())
//            it.name
//        }
//        val customTimeAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, times)
//        customTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        customTimeUnitsView.adapter = customTimeAdapter
//
//        if (reminder != null) {
//            val parsedResult = ReminderMinutesParser.parseCustomMinutes(Math.abs(reminder!!.getMinutesFromStart()))
//            if (parsedResult != null) {
//                customTimeView.setText(parsedResult.first.toString())
//                customTimeUnitsView.setSelection(parsedResult.second.ordinal)
//            }
//        }
//    }
//
//    private fun initPredefinedTimes() {
//        val predefinedTimes = mutableListOf<String>()
//        for (m in REMINDER_PREDEFINED_MINUTES) {
//            predefinedTimes.add(m.toString())
////            predefinedTimes.add(ReminderTimeFormatter.formatMinutesBeforeReadable(context, REMINDER_PREDEFINED_MINUTE))
//        }
//        predefinedTimes.add(activity!!.getString(R.string.custom))
//
//        val predefinedTimesAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, predefinedTimes)
//        predefinedTimesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        predefinedTimesView.adapter = predefinedTimesAdapter
//        predefinedTimesView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {
//                if (position == predefinedTimesAdapter.count - 1) {
//                    showCustomTimeForm()
//                }
//            }
//
//            override fun onNothingSelected(adapterView: AdapterView<*>) {}
//        }
//    }

    interface ReminderPickedListener {
        fun onReminderPicked(reminder: Reminder)
    }
}