package io.ipoli.android.reminder.ui.picker

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.text.ReminderTimeFormatter
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/5/17.
 */
class ReminderPickerDialogPresenter(private val reminderTimeFormatter: ReminderTimeFormatter) : BaseMviPresenter<ReminderPickerDialogController, ReminderPickerViewState>(ReminderPickerViewState.Loading) {
    override fun bindIntents(): List<Observable<ReminderPickerViewState>> {
        return listOf(
            on { it.newReminderIntent() }.map { _ ->
                ReminderPickerViewState.PredefinedTimeValueLoaded("Proba proba",
                    reminderTimeFormatter.predefinedTimes,
                    0)
            }.cast(ReminderPickerViewState::class.java)
        )
    }

}