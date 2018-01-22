package mypoli.android.quest.calendar

import android.content.Context
import mypoli.android.common.redux.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter
import mypoli.android.common.text.CalendarFormatter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/22/2018.
 */
class CalendarReduxPresenter : AndroidStatePresenter<AppState, CalendarViewState> {

    override fun present(state: AppState, context: Context): CalendarViewState {

        val calendarState = state.calendarState

        val calendarFormatter = CalendarFormatter(context)

        val dayText = calendarFormatter.day(calendarState.currentDate)
        val dateText = calendarFormatter.date(calendarState.currentDate)

        val monthFormatter = DateTimeFormatter.ofPattern("MMMM")

        return CalendarViewState(
            type = CalendarViewState.StateType.DATA_LOADED,
            currentDate = LocalDate.now(),
            datePickerState = CalendarViewState.DatePickerState.INVISIBLE,
            adapterPosition = MID_POSITION,
            dateText = dateText,
            dayText = dayText,
            monthText = monthFormatter.format(calendarState.currentDate)
        )
    }

    companion object {
        const val MID_POSITION = 49
    }

}