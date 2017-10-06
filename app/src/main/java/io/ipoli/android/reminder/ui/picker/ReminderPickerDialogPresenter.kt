package io.ipoli.android.reminder.ui.picker

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.parser.ReminderMinutesParser
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
            }.cast(ReminderPickerViewState::class.java),

            on { it.editReminderIntent() }.map { reminder ->
                val (timeValue, timeUnit) = ReminderMinutesParser.parseCustomMinutes(reminder.getMinutesFromStart())
                var selectedIndex = -1

                val timeUnits = TimeUnit.values().mapIndexed { i, unit ->
                    if (unit == timeUnit) {
                        selectedIndex = i
                    }
                    unit.toString()
                }

                ReminderPickerViewState.CustomTimeValueLoaded(
                    reminder.message!!,
                    timeValue.toString(),
                    timeUnits,
                    selectedIndex
                )

            }.cast(ReminderPickerViewState::class.java)
        )
    }

}