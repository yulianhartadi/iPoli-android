package mypoli.android.quest.calendar

import android.content.Context
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter
import mypoli.android.common.text.CalendarFormatter
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
            type = CalendarViewState.StateType.valueOf(calendarState.type.name),
            currentDate = calendarState.currentDate,
            datePickerState = calendarState.datePickerState,
            adapterPosition = calendarState.adapterPosition,
            dateText = dateText,
            dayText = dayText,
            monthText = monthFormatter.format(calendarState.currentMonth),
            progress = calendarState.progress,
            maxProgress = calendarState.maxProgress,
            level = calendarState.level,
            coins = calendarState.coins
        )
    }

    override fun presentInitial(state: CalendarViewState) =
        state.copy(
            type = CalendarViewState.StateType.INITIAL
        )

}