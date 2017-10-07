package io.ipoli.android.reminder.view.picker

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.StateChange
import io.ipoli.android.common.parser.ReminderMinutesParser
import io.ipoli.android.reminder.view.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.view.formatter.TimeUnitFormatter
import io.ipoli.android.reminder.view.picker.ReminderPickerViewState.StateType.*

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
            bindPickReminderIntent(),
            bindMessageChangeIntent(),
            bindPredefinedValueChangeIntent(),
            bindCustomTimeChangeIntent(),
            bindTimeUnitChangeIntent()
        )

    private fun bindTimeUnitChangeIntent() =
        on { it.timeUnitChangeIntent() }.map { (_, index) ->
            TimeUnitChange(index)
        }.cast(ReminderPickerStateChange::class.java)

    private fun bindPredefinedValueChangeIntent() =
        on { it.predefinedValueChangeIntent() }.map { (_, index) ->
            if (index == reminderTimeFormatter.predefinedTimes.size - 1) {
                ShowCustomValues("", timeUnitFormatter.customTimeUnits, 0)
            } else {
                PredefinedValueChange(index)
            }
        }.cast(ReminderPickerStateChange::class.java)

    private fun bindMessageChangeIntent() =
        on { it.messageChangeIntent() }.map { (_, text) ->
            MessageChange(text)
        }.cast(ReminderPickerStateChange::class.java)

    private fun bindCustomTimeChangeIntent() =
        on { it.customTimeChangeIntent() }.map { (_, text) ->
            CustomTimeChange(text)
        }.cast(ReminderPickerStateChange::class.java)

    private fun bindPickReminderIntent() =
        on { it.pickReminderIntent() }.map { (state, _) ->
            if (state.timeUnitIndex != null && state.timeValue.isEmpty()) {
                TimeValueValidationError
            } else {
                ReminderPicked
            }
        }.cast(ReminderPickerStateChange::class.java)

    private fun bindEditReminderIntent() =
        on { it.loadReminderData() }.map { (_, reminder) ->
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
        on { it.loadNewReminderData() }.map { _ ->
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

data class CustomTimeChange(private val text: String) : ReminderPickerStateChange {
    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(type = NEW_VALUES, timeValue = text)
}

data class MessageChange(private val text: String) : ReminderPickerStateChange {
    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(type = NEW_VALUES, message = text)
}

data class PredefinedValueChange(private val index: Int) : ReminderPickerStateChange {
    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(type = NEW_VALUES, predefinedIndex = index)
}

object TimeValueValidationError : ReminderPickerStateChange {
    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(type = TIME_VALUE_VALIDATION_ERROR)
}

object ReminderPicked : ReminderPickerStateChange {
    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(type = FINISHED)
}

data class TimeUnitChange(val index: Int) : ReminderPickerStateChange {
    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(type = NEW_VALUES, timeUnitIndex = index)
}