package mypoli.android.quest.schedule.calendar

import android.content.Context
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/31/2018.
 */
class CalendarPresenter : AndroidStatePresenter<AppState, CalendarViewState> {

    override fun present(state: AppState, context: Context): CalendarViewState {
        val calendarState = state.calendarState
        return CalendarViewState(
            type = calendarState.type,
            currentDate = state.scheduleState.currentDate,
            adapterPosition = calendarState.adapterPosition
        )
    }

    override fun presentInitial(state: CalendarViewState): CalendarViewState {
        return state.copy(
            type = CalendarState.StateType.INITIAL
        )
    }

}