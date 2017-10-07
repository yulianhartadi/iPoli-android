package io.ipoli.android.reminder.ui.picker

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.StateChange
import io.ipoli.android.common.parser.ReminderMinutesParser
import io.ipoli.android.reminder.ui.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.ui.formatter.TimeUnitFormatter
import io.ipoli.android.reminder.ui.picker.ReminderPickerViewState.StateType.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/5/17.
 */
class ReminderPickerDialogPresenter(
    private val reminderTimeFormatter: ReminderTimeFormatter,
    private val timeUnitFormatter: TimeUnitFormatter
) : BaseMviPresenter<ReminderPickerDialogController, ReminderPickerViewState, ReminderPickerStateChange>(ReminderPickerViewState.Loading) {
    override fun bindIntents() =
        listOf(
            bindLoadNewReminderIntent(),
            bindEditReminderIntent(),
            bindShowCustomValuesIntent(),
            bindPickReminderIntent(),
            bindCustomTimeChangeIntent()
        )

    private fun bindCustomTimeChangeIntent() =
        on { it.customTimeChangeIntent() }.map { text ->
            CustomTimeChange(text)
        }.cast(ReminderPickerStateChange::class.java)

    private fun bindPickReminderIntent() =
        on { it.pickReminderIntent() }.map { _ ->
            if (state.timeUnitIndex != null && state.timeValue.isEmpty()) {
                TimeValueValidationError
            } else {
                ReminderPicked
            }
        }.cast(ReminderPickerStateChange::class.java)


    private fun bindShowCustomValuesIntent() =
        on { it.showCustomValuesIntent() }.map { _ ->
            ShowCustomValues(
                "",
                timeUnitFormatter.customTimeUnits,
                0
            )
        }.cast(ReminderPickerStateChange::class.java)

    private fun bindEditReminderIntent() =
        on { it.editReminderIntent() }.map { reminder ->
            val (timeValue, timeUnit) = ReminderMinutesParser
                .parseCustomMinutes(reminder.getMinutesFromStart())
            EditReminderDataLoaded(
                reminder.message!!,
                timeValue.toString(),
                timeUnitFormatter.customTimeUnits,
                timeUnit.ordinal
            )
        }.cast(ReminderPickerStateChange::class.java)

    private fun bindLoadNewReminderIntent() =
        on { it.newReminderIntent() }.map { _ ->
            NewReminderDataLoaded(
                reminderTimeFormatter.predefinedTimes,
                0
            )
        }.cast(ReminderPickerStateChange::class.java)
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
            timeUnitIndex = timeValueIndex
        )
}

data class ShowCustomValues(
    private val timeValue: String,
    private val timeUnits: List<String>,
    private val timeValueIndex: Int
) : ReminderPickerStateChange {

    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(
            type = CUSTOM_TIME,
            timeValue = timeValue,
            timeUnits = timeUnits,
            timeUnitIndex = timeValueIndex,
            predefinedIndex = null,
            predefinedValues = listOf()
        )
}

data class CustomTimeChange(val text: String) : ReminderPickerStateChange {
    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(type = NEW_VALUES, timeValue = text)
}

object TimeValueValidationError : ReminderPickerStateChange {
    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(type = TIME_VALUE_VALIDATION_ERROR)
}

object ReminderPicked : ReminderPickerStateChange {
    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(type = FINISHED)
}