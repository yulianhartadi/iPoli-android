package io.ipoli.android.reminder.ui.picker

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
    val timeValueIndex: Int? = null
) {

    enum class StateType {
        LOADING, NEW_REMINDER, EDIT_REMINDER, CUSTOM_TIME
    }

//    object Loading : ReminderPickerViewState()
//
//    data class NewReminderDataLoaded(
//        val message: String,
//        val predefinedValues: List<String>,
//        val predefinedIndex: Int
//    ) : ReminderPickerViewState()
//
//    data class CustomTimeValueLoaded(
//        val message: String,
//        val timeValue: String,
//        val timeUnits: List<String>,
//        val timeValueIndex: Int
//    ) : ReminderPickerViewState()
//
//    data class ShowCustomTimeValue(
//        val timeValue: String,
//        val timeUnits: List<String>,
//        val timeValueIndex: Int
//    ) : ReminderPickerViewState()
}

interface ReminderPickerView : ViewStateRenderer<ReminderPickerViewState> {
    fun editReminderIntent(): Observable<Reminder>
    fun newReminderIntent(): Observable<Unit>
    fun showCustomTimeIntent(): Observable<Unit>
}



