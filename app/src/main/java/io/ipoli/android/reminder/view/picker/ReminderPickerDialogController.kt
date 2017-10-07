package io.ipoli.android.reminder.view.picker

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.view.MviDialogController
import io.ipoli.android.common.view.string
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.data.Reminder
import io.reactivex.Observable
import kotlinx.android.synthetic.main.dialog_reminder_picker.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required
import timber.log.Timber


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

    private val pickReminderSubject = createIntentSubject<Unit>()
    private val messageChangeSubject = createIntentSubject<String>()
    private val predefinedValueChangeSubject = createIntentSubject<Int>()
    private val customTimeChangeSubject = createIntentSubject<String>()
    private val timeUnitChangeSubject = createIntentSubject<Int>()

    override fun editReminderIntent(): Observable<Reminder> =
        Observable.just(reminder != null)
            .filter { !isRestoring && it }.map { reminder!! }

    override fun newReminderIntent(): Observable<Unit> =
        Observable.just(Unit)
            .filter { !isRestoring && reminder == null }


    override fun pickReminderIntent() = pickReminderSubject

    override fun messageChangeIntent() = messageChangeSubject

    override fun predefinedValueChangeIntent() = predefinedValueChangeSubject

    override fun customTimeChangeIntent() = customTimeChangeSubject

    override fun timeUnitChangeIntent() = timeUnitChangeSubject

    private val presenter by required { reminderPickerPresenter }

    override fun createPresenter() = presenter

    override fun render(state: ReminderPickerViewState, dialogView: View) {
        Timber.d("${state}")

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
            }

            ReminderPickerViewState.StateType.EDIT_REMINDER -> {
                showCustomTimeViews(dialogView)
                dialogView.message.setText(state.message)
                dialogView.message.setSelection(state.message.length)

                val customTimeAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, state.timeUnits)
                customTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogView.customTimeUnits.adapter = customTimeAdapter
                dialogView.customTimeUnits.setSelection(state.timeUnitIndex!!)
                dialogView.customTime.setText(state.timeValue)
            }

            ReminderPickerViewState.StateType.CUSTOM_TIME -> {
                showCustomTimeViews(dialogView)

                val customTimeAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, state.timeUnits)
                customTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogView.customTimeUnits.adapter = customTimeAdapter
                dialogView.customTimeUnits.setSelection(state.timeUnitIndex!!)
                dialogView.customTime.setText(state.timeValue)

                dialogView.customTime.requestFocus()
                ViewUtils.showKeyboard(activity!!, dialogView.customTime)
            }

            ReminderPickerViewState.StateType.FINISHED -> {
                ViewUtils.hideKeyboard(dialogView)
                dismissDialog()
            }

            ReminderPickerViewState.StateType.TIME_VALUE_VALIDATION_ERROR -> {
                dialogView.customTime.error = string(R.string.invalid_reminder_time)
            }
        }
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

        val contentView = LayoutInflater.from(activity!!).inflate(R.layout.dialog_reminder_picker, null)

        with(contentView) {
            RxTextView.textChanges(message).map { it.toString() }.subscribe(messageChangeSubject)

            RxAdapterView.itemSelections(predefinedTimes)
                .skipInitialValue()
                .subscribe(predefinedValueChangeSubject)

            RxAdapterView.itemSelections(customTimeUnits)
                .skipInitialValue()
                .subscribe(timeUnitChangeSubject)

            RxTextView.textChanges(customTime).map { it.toString() }.subscribe(customTimeChangeSubject)
        }

        val dialog = AlertDialog.Builder(activity!!)
            .setView(contentView)
            .setTitle(R.string.reminder_dialog_title)
            .setIcon(R.drawable.pet_5_head)
            .setPositiveButton(R.string.dialog_ok, null)
            .setNegativeButton(R.string.cancel, { p0, p1 -> ViewUtils.hideKeyboard(contentView) })
            .setNeutralButton(R.string.do_not_remind, { p0, p1 -> ViewUtils.hideKeyboard(contentView) })
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                pickReminderSubject.onNext(Unit)
            }
        }

        return DialogView(dialog, contentView)
    }

    interface ReminderPickedListener {
        fun onReminderPicked(reminder: Reminder)
    }
}