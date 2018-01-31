package mypoli.android.quest.schedule.calendar

import mypoli.android.common.AppState
import mypoli.android.common.AppStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.quest.schedule.ScheduleAction
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/31/2018.
 */


data class CalendarState(
    val type: StateType,
    val adapterPosition: Int,
    val adapterMidPosition: Int
) : State {
    enum class StateType {
        INITIAL, CALENDAR_DATE_CHANGED, SWIPE_DATE_CHANGED
    }
}

object CalendarReducer : AppStateReducer<CalendarState> {
    override fun reduce(state: AppState, action: Action): CalendarState {

        return when (action) {
            is CalendarAction.SwipeChangeDate -> {
                state.calendarState.copy(
                    type = CalendarState.StateType.SWIPE_DATE_CHANGED,
                    adapterPosition = action.adapterPosition
                )
            }
            is ScheduleAction.ScheduleChangeDate -> {
                state.calendarState.copy(
                    type = CalendarState.StateType.CALENDAR_DATE_CHANGED,
                    adapterPosition = MID_POSITION
                )
            }
            else -> state.calendarState
        }
    }

    override fun defaultState(): CalendarState {
        return CalendarState(
            type = CalendarState.StateType.INITIAL,
            adapterPosition = -1,
            adapterMidPosition = MID_POSITION
        )
    }

    private const val MID_POSITION = 49
}

sealed class CalendarAction : Action {
    data class SwipeChangeDate(val adapterPosition: Int) : CalendarAction()
}

data class CalendarViewState(
    val type: CalendarState.StateType,
    val currentDate: LocalDate,
    val adapterPosition: Int
) : ViewState