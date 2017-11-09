package io.ipoli.android.quest.calendar

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.quest.calendar.CalendarViewState.StateType.DATA_LOADED
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/21/17.
 */

sealed class CalendarIntent : Intent

data class LoadDataIntent(val currentDate: LocalDate) : CalendarIntent()
data class SwipeChangeDateIntent(val position: Int) : CalendarIntent()
data class CalendarChangeDateIntent(val year: Int, val month: Int, val day: Int) : CalendarIntent()
data class ChangeMonthIntent(val year: Int, val month: Int) : CalendarIntent()
object ExpandToolbarIntent : CalendarIntent()
object ExpandToolbarWeekIntent : CalendarIntent()

data class CalendarViewState(
    val type: StateType = DATA_LOADED,
    val currentDate: LocalDate,
    val monthText: String = "",
    val dayText: String = "",
    val dateText: String = "",
    val datePickerState: DatePickerState,
    val adapterPosition: Int
) : ViewState {

    enum class StateType {
        LOADING, DATA_LOADED, CALENDAR_DATE_CHANGED, SWIPE_DATE_CHANGED, DEFAULT, DATE_PICKER_CHANGED
    }

    enum class DatePickerState {
        INVISIBLE, SHOW_WEEK, SHOW_MONTH
    }
}