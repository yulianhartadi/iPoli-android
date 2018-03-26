package io.ipoli.android.quest.reminder.picker

import io.ipoli.android.Constants
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.parser.ReminderMinutesParser
import io.ipoli.android.pet.usecase.FindPetUseCase
import io.ipoli.android.quest.reminder.formatter.ReminderTimeFormatter
import io.ipoli.android.quest.reminder.formatter.TimeUnitFormatter
import io.ipoli.android.quest.reminder.picker.ReminderPickerViewState.StateType.*
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/5/17.
 */
class ReminderPickerDialogPresenter(
    private val reminderTimeFormatter: ReminderTimeFormatter,
    private val timeUnitFormatter: TimeUnitFormatter,
    private val findPetUseCase: FindPetUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<ReminderPickerViewState>, ReminderPickerViewState, ReminderPickerIntent>(
    ReminderPickerViewState(type = LOADING), coroutineContext
) {

    override fun reduceState(
        intent: ReminderPickerIntent,
        state: ReminderPickerViewState
    ): ReminderPickerViewState {
        return when (intent) {

            is LoadReminderDataIntent -> {
                val stateWithPet = state.copy(
                    petAvatar = findPetUseCase.execute(Unit).avatar
                )
                if (intent.reminder == null) {
                    loadNewReminderData(stateWithPet)
                } else {
                    loadExistingReminderData(intent.reminder, stateWithPet)
                }
            }

            is PickReminderIntent -> {
                if (state.timeUnitIndex != null && state.timeValue.isEmpty()) {
                    state.copy(type = TIME_VALUE_VALIDATION_ERROR)
                } else {
                    state.copy(
                        type = FINISHED,
                        viewModel = ReminderViewModel(
                            state.message,
                            calculateMinutesFromStart(state)
                        )
                    )
                }
            }
            is ChangeMessageIntent -> {
                state.copy(type = NEW_VALUES, message = intent.message)
            }

            is ChangePredefinedTimeIntent -> {
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

            is ChangeCustomTimeIntent -> {
                state.copy(type = NEW_VALUES, timeValue = intent.timeValue)
            }

            is ChangeTimeUnitIntent -> {
                state.copy(type = NEW_VALUES, timeUnitIndex = intent.index)
            }
        }
    }

    private fun loadNewReminderData(state: ReminderPickerViewState): ReminderPickerViewState {
        return state.copy(
            type = NEW_REMINDER,
            predefinedValues = reminderTimeFormatter.predefinedTimes,
            predefinedIndex = 0
        )
    }

    private fun loadExistingReminderData(
        reminder: ReminderViewModel,
        state: ReminderPickerViewState
    ): ReminderPickerViewState {

        if (reminder.minutesFromStart == 0L) {
            return state.copy(
                type = EDIT_REMINDER,
                message = reminder.message,
                predefinedValues = reminderTimeFormatter.predefinedTimes,
                predefinedIndex = 0
            )
        }

        val (timeValue, timeUnit) = ReminderMinutesParser
            .parseCustomMinutes(reminder.minutesFromStart)

        return state.copy(
            type = EDIT_REMINDER,
            message = reminder.message,
            timeValue = timeValue.toString(),
            timeUnits = timeUnitFormatter.customTimeUnits,
            timeUnitIndex = timeUnit.ordinal
        )
    }

    private fun calculateMinutesFromStart(state: ReminderPickerViewState): Long {
        return if (state.timeUnitIndex != null) {
            val timeUnitMinutes = TimeUnit.values()[state.timeUnitIndex].minutes
            state.timeValue.toLong() * timeUnitMinutes
        } else {
            Constants.REMINDER_PREDEFINED_MINUTES[state.predefinedIndex!!].toLong()
        }
    }
}