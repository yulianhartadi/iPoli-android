package io.ipoli.android.reminder.view.picker

import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.quest.data.Reminder
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/5/17.
 */
data class ReminderPickerViewState(
    val type: StateType,
    val message: String = "",
    val predefinedValues: List<String> = listOf(),
    val predefinedIndex: Int? = null,
    val timeValue: String = "",
    val timeUnits: List<String> = listOf(),
    val timeUnitIndex: Int? = null,
    val viewModel: ReminderViewModel? = null
) {

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

interface ReminderPickerView : ViewStateRenderer<ReminderPickerViewState> {
    fun loadNewReminderData(): Observable<Unit>
    fun loadReminderData(): Observable<ReminderViewModel>
    fun pickReminderIntent(): Observable<Unit>
    fun messageChangeIntent(): Observable<String>
    fun predefinedValueChangeIntent(): Observable<Int>
    fun customTimeChangeIntent(): Observable<String>
    fun timeUnitChangeIntent(): Observable<Int>
}



