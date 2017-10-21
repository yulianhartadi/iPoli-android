package io.ipoli.android.reminder.view.picker

import io.ipoli.android.Constants
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.reminder.view.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.view.formatter.TimeUnitFormatter
import io.ipoli.android.reminder.view.picker.ReminderPickerViewState.StateType.*
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/5/17.
 */
class ReminderPickerDialogPresenter(
    private val reminderTimeFormatter: ReminderTimeFormatter,
    private val timeUnitFormatter: TimeUnitFormatter,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<ReminderPickerViewState>, ReminderPickerViewState, ReminderPickerIntent>(
    ReminderPickerViewState(type = ReminderPickerViewState.StateType.LOADING), coroutineContext) {


    override fun reduceState(intent: ReminderPickerIntent, state: ReminderPickerViewState): ReminderPickerViewState {
        return when (intent) {
            is PickReminderIntent -> {
                if (state.timeUnitIndex != null && state.timeValue.isEmpty()) {
                    state.copy(type = TIME_VALUE_VALIDATION_ERROR)
                } else {
                    state.copy(
                        type = FINISHED,
                        viewModel = ReminderViewModel(
                            state.message,
                            calculateMinutesFromStart(state))
                    )
                }
            }
            is MessageChangeIntent -> {
                state.copy(type = NEW_VALUES, message = intent.message)
            }

            is PredefinedTimeChangeIntent -> {
                if (intent.index == reminderTimeFormatter.predefinedTimes.size - 1) {
                    state.copy(
                        type = CUSTOM_TIME,
                        timeValue = "",
                        timeUnits = timeUnitFormatter.customTimeUnits,
                        timeUnitIndex = 0,
                        predefinedIndex = null,
                        predefinedValues = listOf()
                    )
                } else {
                    state.copy(type = NEW_VALUES, predefinedIndex = intent.index)
                }
            }

            is CustomTimeChangeIntent -> {
                state.copy(type = NEW_VALUES, timeValue = intent.timeValue)
            }

            is TimeUnitChangeIntent -> {
                state.copy(type = NEW_VALUES, timeUnitIndex = intent.index)
            }
        }
    }

    private fun calculateMinutesFromStart(state: ReminderPickerViewState): Long {
        return if (state.timeUnitIndex != null) {
            val timeUnitMinutes = TimeUnit.values()[state.timeUnitIndex].minutes
            state.timeValue.toLong() * timeUnitMinutes
        } else {
            Constants.REMINDER_PREDEFINED_MINUTES[state.predefinedIndex!!].toLong()
        }
    }

//    private fun bindEditReminderIntent() =
//        on { it.loadReminderData() }.map { (state, reminder) ->
//            val (timeValue, timeUnit) = ReminderMinutesParser
//                .parseCustomMinutes(reminder.minutesFromStart)
//
//            state.copy(
//                type = EDIT_REMINDER,
//                message = reminder.message!!,
//                timeValue = timeValue.toString(),
//                timeUnits = timeUnitFormatter.customTimeUnits,
//                timeUnitIndex = timeUnit.ordinal
//            )
//        }.cast(ReminderPickerViewState::class.java)
//
//    private fun bindLoadNewReminderIntent() =
//        on { it.loadNewReminderData() }.map { (state, _) ->
//            state.copy(
//                type = NEW_REMINDER,
//                predefinedValues = reminderTimeFormatter.predefinedTimes,
//                predefinedIndex = 0
//            )
//        }.cast(ReminderPickerViewState::class.java)
}