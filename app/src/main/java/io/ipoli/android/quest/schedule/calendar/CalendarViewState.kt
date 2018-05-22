package io.ipoli.android.quest.schedule.calendar

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.schedule.ScheduleAction
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
    ) =
        when (action) {

            is CalendarAction.Load -> {
                if (subState.type == CalendarViewState.StateType.LOADING) {
                    subState.copy(
                        type = CalendarViewState.StateType.INITIAL,
                        currentDate = action.startDate
                    )
                } else {
                    subState
                }
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

    override fun defaultState() =
        CalendarViewState(
            type = CalendarViewState.StateType.LOADING,
            currentDate = LocalDate.now(),
            adapterPosition = MID_POSITION,
            adapterMidPosition = MID_POSITION
        )

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
) : BaseViewState() {
    enum class StateType {
        INITIAL, CALENDAR_DATE_CHANGED, SWIPE_DATE_CHANGED, LOADING
    }
}