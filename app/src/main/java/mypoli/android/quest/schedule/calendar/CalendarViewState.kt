package mypoli.android.quest.schedule.calendar

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.quest.schedule.ScheduleAction
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/31/2018.
 */

object CalendarReducer : BaseViewStateReducer<CalendarViewState>() {

    override val stateKey = key<CalendarViewState>()

    override fun reduce(
        state: AppState,
        subState: CalendarViewState,
        action: Action
    ): CalendarViewState {

        return when (action) {

            is CalendarAction.Load -> {
                subState.copy(
                    type = CalendarViewState.StateType.IDLE,
                    currentDate = action.startDate
                )
            }

            is CalendarAction.SwipeChangeDate -> {

                val currentPos = subState.adapterPosition
                val newPos = action.adapterPosition
                val currentDate = subState.currentDate
                val newDate = if (newPos < currentPos)
                    currentDate.minusDays(1)
                else
                    currentDate.plusDays(1)

                subState.copy(
                    type = CalendarViewState.StateType.SWIPE_DATE_CHANGED,
                    adapterPosition = action.adapterPosition,
                    currentDate = newDate
                )
            }
            is ScheduleAction.ScheduleChangeDate -> {
                subState.copy(
                    type = CalendarViewState.StateType.CALENDAR_DATE_CHANGED,
                    adapterPosition = MID_POSITION,
                    currentDate = action.date
                )
            }
            else -> subState
        }
    }

    override fun defaultState(): CalendarViewState {
        return CalendarViewState(
            type = CalendarViewState.StateType.INITIAL,
            currentDate = LocalDate.now(),
            adapterPosition = MID_POSITION,
            adapterMidPosition = MID_POSITION
        )
    }

    private const val MID_POSITION = 49
}

sealed class CalendarAction : Action {
    data class SwipeChangeDate(val adapterPosition: Int) : CalendarAction()
    data class ChangeVisibleDate(val date: LocalDate) : CalendarAction()
    data class Load(val startDate: LocalDate) : CalendarAction()
}

data class CalendarViewState(
    val type: CalendarViewState.StateType,
    val currentDate: LocalDate,
    val adapterPosition: Int,
    val adapterMidPosition: Int
) : ViewState {
    enum class StateType {
        INITIAL, CALENDAR_DATE_CHANGED, SWIPE_DATE_CHANGED, IDLE
    }
}