package io.ipoli.android.reminder.ui.picker

import io.ipoli.android.common.mvi.BaseMviPresenter
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
) : BaseMviPresenter<ReminderPickerDialogController, ReminderPickerViewState, ReminderPickerStateChange>(ReminderPickerViewState.Loading) {
    override fun bindIntents(): List<Observable<ReminderPickerStateChange>> {
        return listOf(
            on { it.newReminderIntent() }.map { _ ->
                PredefinedTimeValueLoaded(
                    reminderTimeFormatter.predefinedTimes,
                    0
                )
            }.cast(ReminderPickerStateChange::class.java)
        )
//
//            on { it.editReminderIntent() }.map { reminder ->
//                val (timeValue, timeUnit) = ReminderMinutesParser
//                    .parseCustomMinutes(reminder.getMinutesFromStart())
//                ReminderPickerViewState.CustomTimeValueLoaded(
//                    reminder.message!!,
//                    timeValue.toString(),
//                    timeUnitFormatter.customTimeUnits,
//                    timeUnit.ordinal
//                )
//            }.cast(ReminderPickerViewState::class.java),
//
//            on { it.showCustomTimeIntent() }.map { _ ->
//                ReminderPickerViewState.ShowCustomTimeValue(
//                    "",
//                    timeUnitFormatter.customTimeUnits,
//                    0
//                )
//            }.cast(ReminderPickerViewState::class.java)
//        )
    }
}

interface StateChange<VS> {
    fun createState(prevState: VS): VS
}

interface ReminderPickerStateChange : StateChange<ReminderPickerViewState>

data class PredefinedTimeValueLoaded(
    private val predefinedTimes: List<String>,
    private val predefinedIndex: Int
) : ReminderPickerStateChange {

    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(
            predefinedValues = predefinedTimes,
            predefinedIndex = predefinedIndex
        )
}

data class CustomTimeValue(private val timeValue: String) : ReminderPickerStateChange {

    override fun createState(prevState: ReminderPickerViewState) =
        prevState.copy(
            timeValue = timeValue
        )
}