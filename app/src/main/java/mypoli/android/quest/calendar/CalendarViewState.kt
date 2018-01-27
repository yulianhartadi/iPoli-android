package mypoli.android.quest.calendar

import mypoli.android.common.AppState
import mypoli.android.common.AppStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.player.Player
import mypoli.android.quest.calendar.CalendarViewState.StateType.INITIAL
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/21/17.
 */

sealed class CalendarIntent : Intent {
    data class LoadData(val currentDate: LocalDate) : CalendarIntent()
    data class SwipeChangeDate(val position: Int) : CalendarIntent()
    data class CalendarChangeDate(val year: Int, val month: Int, val day: Int) : CalendarIntent()
    data class ChangeMonth(val year: Int, val month: Int) : CalendarIntent()
    object ExpandToolbar : CalendarIntent()
    object ExpandToolbarWeek : CalendarIntent()
    data class ChangePlayer(val player: Player) : CalendarIntent()
}


sealed class CalendarAction : Action {
    object ExpandToolbar : CalendarAction()
    object ExpandWeekToolbar : CalendarAction()

    data class SwipeChangeDate(val position: Int) : CalendarAction()
    data class CalendarChangeDate(val year: Int, val month: Int, val day: Int) : CalendarAction()
    data class ChangeMonth(val year: Int, val month: Int) : CalendarAction()
}


data class CalendarState(
    val type: StateType,
    val currentDate: LocalDate,
    val currentMonth: YearMonth,
    val datePickerState: CalendarViewState.DatePickerState,
    val adapterPosition: Int,
    val adapterMidPosition: Int,
    val progress: Int,
    val maxProgress: Int,
    val level: Int,
    val coins: Int
) : State {
    enum class StateType {
        INITIAL, CALENDAR_DATE_CHANGED, SWIPE_DATE_CHANGED, DATE_PICKER_CHANGED, MONTH_CHANGED,
        LEVEL_CHANGED, XP_AND_COINS_CHANGED, DATA_CHANGED
    }
}

object CalendarReducer : AppStateReducer<CalendarState> {

    override fun reduce(state: AppState, action: Action) =
        when (action) {
            is DataLoadedAction.PlayerChanged -> reducePlayerChanged(state.calendarState, action)
            is CalendarAction -> reduceCalendarAction(state.calendarState, action)
            else -> state.calendarState
        }

    private fun reducePlayerChanged(
        state: CalendarState,
        action: DataLoadedAction.PlayerChanged
    ): CalendarState {
        val player = action.player
        val type = when {
            state.level == 0 -> CalendarState.StateType.DATA_CHANGED
            state.level != player.level -> CalendarState.StateType.LEVEL_CHANGED
            else -> CalendarState.StateType.XP_AND_COINS_CHANGED
        }
        return state.copy(
            type = type,
            level = player.level,
            progress = player.experienceProgressForLevel,
            coins = player.coins,
            maxProgress = player.experienceForNextLevel
        )
    }

    private fun reduceCalendarAction(state: CalendarState, action: CalendarAction) =
        when (action) {
            CalendarAction.ExpandWeekToolbar -> {
                when (state.datePickerState) {
                    CalendarViewState.DatePickerState.SHOW_WEEK -> state.copy(
                        type = CalendarState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = CalendarViewState.DatePickerState.SHOW_MONTH
                    )
                    else -> state.copy(
                        type = CalendarState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = CalendarViewState.DatePickerState.SHOW_WEEK
                    )
                }
            }

            CalendarAction.ExpandToolbar -> {
                when (state.datePickerState) {
                    CalendarViewState.DatePickerState.INVISIBLE -> state.copy(
                        type = CalendarState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = CalendarViewState.DatePickerState.SHOW_WEEK
                    )
                    else -> state.copy(
                        type = CalendarState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = CalendarViewState.DatePickerState.INVISIBLE
                    )
                }
            }

            is CalendarAction.SwipeChangeDate -> {

                val newDate =
                    state.currentDate.plusDays((action.position - state.adapterPosition).toLong())
                state.copy(
                    currentDate = newDate,
                    adapterPosition = action.position
                )
            }

            is CalendarAction.CalendarChangeDate -> {
                state.copy(
                    type = CalendarState.StateType.CALENDAR_DATE_CHANGED,
                    currentDate = LocalDate.of(action.year, action.month, action.day),
                    adapterPosition = MID_POSITION
                )
            }

            is CalendarAction.ChangeMonth -> {
                state.copy(
                    type = CalendarState.StateType.MONTH_CHANGED,
                    currentMonth = YearMonth.of(action.year, action.month)
                )
            }
        }


    override fun defaultState() =
        CalendarState(
            type = CalendarState.StateType.INITIAL,
            currentDate = LocalDate.now(),
            currentMonth = YearMonth.now(),
            datePickerState = CalendarViewState.DatePickerState.INVISIBLE,
            adapterPosition = -1,
            adapterMidPosition = MID_POSITION,
            progress = 0,
            maxProgress = 0,
            level = 0,
            coins = 0
        )


    private const val MID_POSITION = 49
}

data class CalendarViewState(
    val type: StateType = INITIAL,
    val currentDate: LocalDate,
    val monthText: String = "",
    val dayText: String = "",
    val dateText: String = "",
    val datePickerState: DatePickerState,
    val adapterPosition: Int,
    val progress: Int = 0,
    val maxProgress: Int = 0,
    val level: Int = 0,
    val coins: Int = 0
) : ViewState {

    enum class StateType {
        LOADING, INITIAL, CALENDAR_DATE_CHANGED, SWIPE_DATE_CHANGED, DEFAULT, DATE_PICKER_CHANGED,
        MONTH_CHANGED,
        LEVEL_CHANGED, XP_AND_COINS_CHANGED, DATA_CHANGED
    }

    enum class DatePickerState {
        INVISIBLE, SHOW_WEEK, SHOW_MONTH
    }
}