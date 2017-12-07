package io.ipoli.android.reminder.view.picker

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.pet.PetAvatar

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/5/17.
 */

sealed class ReminderPickerIntent : Intent

data class LoadReminderDataIntent(val reminder: ReminderViewModel?) : ReminderPickerIntent()
data class ChangeMessageIntent(val message: String) : ReminderPickerIntent()
data class ChangeCustomTimeIntent(val timeValue: String) : ReminderPickerIntent()
data class ChangePredefinedTimeIntent(val index: Int) : ReminderPickerIntent()
data class ChangeTimeUnitIntent(val index: Int) : ReminderPickerIntent()
object PickReminderIntent : ReminderPickerIntent()

data class ReminderPickerViewState(
    val type: StateType,
    val message: String = "",
    val predefinedValues: List<String> = listOf(),
    val predefinedIndex: Int? = null,
    val timeValue: String = "",
    val timeUnits: List<String> = listOf(),
    val timeUnitIndex: Int? = null,
    val pet: PetAvatar? = null,
    val viewModel: ReminderViewModel? = null
) : ViewState {

    enum class StateType {
        LOADING,
        NEW_REMINDER,
        EDIT_REMINDER,
        CUSTOM_TIME,
        NEW_VALUES,
        TIME_VALUE_VALIDATION_ERROR,
        FINISHED
    }

    companion object {
        val Loading = ReminderPickerViewState(StateType.LOADING)
    }
}