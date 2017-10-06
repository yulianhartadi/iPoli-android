package io.ipoli.android.reminder.ui.picker

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.parser.ReminderMinutesParser
import io.ipoli.android.reminder.ui.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.ui.formatter.TimeUnitFormatter
import io.reactivex.Observable

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/5/17.
 */
class ReminderPickerDialogPresenter(
    private val reminderTimeFormatter: ReminderTimeFormatter,
    private val timeUnitFormatter: TimeUnitFormatter
) : BaseMviPresenter<ReminderPickerDialogController, ReminderPickerViewState>(ReminderPickerViewState.Loading) {
    override fun bindIntents(): List<Observable<ReminderPickerViewState>> {
        return listOf(

            on { it.newReminderIntent() }.map { _ ->
                ReminderPickerViewState.PredefinedTimeValueLoaded(
                    "",
                    reminderTimeFormatter.predefinedTimes,
                    0
                )
            }.cast(ReminderPickerViewState::class.java),

            on { it.editReminderIntent() }.map { reminder ->
                val (timeValue, timeUnit) = ReminderMinutesParser
                    .parseCustomMinutes(reminder.getMinutesFromStart())
                ReminderPickerViewState.CustomTimeValueLoaded(
                    reminder.message!!,
                    timeValue.toString(),
                    timeUnitFormatter.customTimeUnits,
                    timeUnit.ordinal
                )
            }.cast(ReminderPickerViewState::class.java),

            on { it.showCustomTimeIntent() }.map { _ ->
                ReminderPickerViewState.ShowCustomTimeValue(
                    "",
                    timeUnitFormatter.customTimeUnits,
                    0
                )
            }.cast(ReminderPickerViewState::class.java)
        )
    }

}