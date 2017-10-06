package io.ipoli.android.reminder.ui.picker

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.parser.ReminderMinutesParser
import io.ipoli.android.reminder.ui.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.ui.formatter.TimeUnitFormatter
import io.ipoli.android.reminder.ui.picker.ReminderPickerViewState.StateType.*
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/5/17.
 */
class ReminderPickerDialogPresenter(
    private val reminderTimeFormatter: ReminderTimeFormatter,
    private val timeUnitFormatter: TimeUnitFormatter
) : BaseMviPresenter<ReminderPickerDialogController, ReminderPickerViewState, ReminderPickerStateChange>(ReminderPickerViewState(type = LOADING)) {
    override fun bindIntents(): List<Observable<ReminderPickerStateChange>> {
        return listOf(
            on { it.newReminderIntent() }.map { _ ->
                NewReminderDataLoaded(
                    reminderTimeFormatter.predefinedTimes,
                    0
                )
            }.cast(ReminderPickerStateChange::class.java),

            on { it.editReminderIntent() }.map { reminder ->
                val (timeValue, timeUnit) = ReminderMinutesParser
                    .parseCustomMinutes(reminder.getMinutesFromStart())
                EditReminderDataLoaded(
                    reminder.message!!,
                    timeValue.toString(),
                    timeUnitFormatter.customTimeUnits,
                    timeUnit.ordinal
                )
            }.cast(ReminderPickerStateChange::class.java),

            on { it.showCustomTimeIntent() }.map { _ ->
                ShowCustomValues(
                    "",
                    timeUnitFormatter.customTimeUnits,
                    0
                )
            }.cast(ReminderPickerStateChange::class.java)
        )
    }
}

interface StateChange<VS> {
    fun createState(prevState: VS): VS
}

interface ReminderPickerStateChange : StateChange<ReminderPickerViewState>

data class NewReminderDataLoaded(
    private val predefinedTimes: List<String>,
    private val predefinedIndex: Int
) : ReminderPickerStateChange {

    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(
            type = NEW_REMINDER,
            predefinedValues = predefinedTimes,
            predefinedIndex = predefinedIndex
        )
}

data class EditReminderDataLoaded(
    private val message: String,
    private val timeValue: String,
    private val timeUnits: List<String>,
    private val timeValueIndex: Int
) : ReminderPickerStateChange {

    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(
            type = EDIT_REMINDER,
            message = message,
            timeValue = timeValue,
            timeUnits = timeUnits,
            timeValueIndex = timeValueIndex
        )
}

data class ShowCustomValues(private val timeValue: String,
                            private val timeUnits: List<String>,
                            private val timeValueIndex: Int) : ReminderPickerStateChange {

    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(
            type = CUSTOM_TIME,
            timeValue = timeValue,
            timeUnits = timeUnits,
            timeValueIndex = timeValueIndex
        )
}