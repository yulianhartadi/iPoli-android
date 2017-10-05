package io.ipoli.android.reminder.ui.picker

import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.quest.data.Reminder
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/5/17.
 */
sealed class ReminderPickerViewState {
    object Loading : ReminderPickerViewState()

    data class PredefinedTimeValueLoaded(
        val message: String,
        val predefinedValues: List<String>,
        val predefinedIndex: Int
    ) : ReminderPickerViewState()

    data class CustomTimeValueLoaded(
        val message: String,
        val timeValue: String,
        val timeUnits: List<String>,
        val timeValueIndex: Int
    ) : ReminderPickerViewState()
}

interface ReminderPickerView : ViewStateRenderer<ReminderPickerViewState> {
    fun editReminderIntent(): Observable<Reminder>
    fun newReminderIntent(): Observable<Boolean>
}



