package mypoli.android.quest.schedule

import mypoli.android.common.AppState
import mypoli.android.common.AppStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.State
import mypoli.android.player.Player
import mypoli.android.quest.schedule.ScheduleState.ViewMode.AGENDA
import mypoli.android.quest.schedule.ScheduleState.ViewMode.CALENDAR
import mypoli.android.quest.schedule.calendar.CalendarAction
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


sealed class ScheduleAction : Action {
    object ExpandToolbar : ScheduleAction()
    object ExpandWeekToolbar : ScheduleAction()

    //    data class SwipeChangeDate(val adapterPosition: Int) : ScheduleAction()
    data class ScheduleChangeDate(val year: Int, val month: Int, val day: Int) : ScheduleAction()

    data class ChangeMonth(val year: Int, val month: Int) : ScheduleAction()
    object ToggleViewMode : ScheduleAction()
}


data class ScheduleState(
    val type: StateType,
    val currentDate: LocalDate,
    val currentMonth: YearMonth,
    val datePickerState: ScheduleViewState.DatePickerState,
    val viewMode: ViewMode,
    val progress: Int,
    val maxProgress: Int,
    val level: Int,
    val coins: Int
) : State {
    enum class StateType {
        LOADING,
        INITIAL, CALENDAR_DATE_CHANGED, SWIPE_DATE_CHANGED, DATE_PICKER_CHANGED, MONTH_CHANGED,
        LEVEL_CHANGED, XP_AND_COINS_CHANGED, DATA_CHANGED,
        VIEW_MODE_CHANGED
    }

    enum class ViewMode {
        CALENDAR, AGENDA
    }
}

object ScheduleReducer : AppStateReducer<ScheduleState> {

    override fun reduce(state: AppState, action: Action) =
        when (action) {
            is DataLoadedAction.PlayerChanged -> reducePlayerChanged(
                state.scheduleState,
                action
            )
            is ScheduleAction -> reduceCalendarAction(
                state.scheduleState,
                action
            )
            is CalendarAction.SwipeChangeDate -> {
                val currentPos = state.calendarState.adapterPosition
                val newPos = action.adapterPosition
                val curDate = state.scheduleState.currentDate
                val newDate = if (newPos < currentPos)
                    curDate.minusDays(1)
                else
                    curDate.plusDays(1)

                state.scheduleState.copy(
                    currentDate = newDate
                )
            }
            else -> state.scheduleState
        }

    private fun reducePlayerChanged(
        state: ScheduleState,
        action: DataLoadedAction.PlayerChanged
    ): ScheduleState {
        val player = action.player
        val type = when {
            state.level == 0 -> ScheduleState.StateType.DATA_CHANGED
            state.level != player.level -> ScheduleState.StateType.LEVEL_CHANGED
            else -> ScheduleState.StateType.XP_AND_COINS_CHANGED
        }
        return state.copy(
            type = type,
            level = player.level,
            progress = player.experienceProgressForLevel,
            coins = player.coins,
            maxProgress = player.experienceForNextLevel
        )
    }

    private fun reduceCalendarAction(state: ScheduleState, action: ScheduleAction) =
        when (action) {
            ScheduleAction.ExpandWeekToolbar -> {
                when (state.datePickerState) {
                    ScheduleViewState.DatePickerState.SHOW_WEEK -> state.copy(
                        type = ScheduleState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = ScheduleViewState.DatePickerState.SHOW_MONTH
                    )
                    else -> state.copy(
                        type = ScheduleState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = ScheduleViewState.DatePickerState.SHOW_WEEK
                    )
                }
            }

            ScheduleAction.ExpandToolbar -> {
                when (state.datePickerState) {
                    ScheduleViewState.DatePickerState.INVISIBLE -> state.copy(
                        type = ScheduleState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = ScheduleViewState.DatePickerState.SHOW_WEEK
                    )
                    else -> state.copy(
                        type = ScheduleState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = ScheduleViewState.DatePickerState.INVISIBLE
                    )
                }
            }

            is ScheduleAction.ScheduleChangeDate -> {
                state.copy(
                    type = ScheduleState.StateType.CALENDAR_DATE_CHANGED,
                    currentDate = LocalDate.of(action.year, action.month, action.day)
                )
            }

            is ScheduleAction.ChangeMonth -> {
                state.copy(
                    type = ScheduleState.StateType.MONTH_CHANGED,
                    currentMonth = YearMonth.of(action.year, action.month)
                )
            }

            is ScheduleAction.ToggleViewMode -> {
                state.copy(
                    type = ScheduleState.StateType.VIEW_MODE_CHANGED,
                    viewMode = if (state.viewMode == CALENDAR) AGENDA else CALENDAR
                )
            }
        }


    override fun defaultState() =
        ScheduleState(
            type = ScheduleState.StateType.INITIAL,
            currentDate = LocalDate.now(),
            currentMonth = YearMonth.now(),
            datePickerState = ScheduleViewState.DatePickerState.INVISIBLE,
            progress = 0,
            maxProgress = 0,
            level = 0,
            coins = 0,
            viewMode = CALENDAR
        )
}

data class ScheduleViewState(
    val type: ScheduleState.StateType,
    val currentDate: LocalDate,
    val monthText: String = "",
    val dayText: String = "",
    val dateText: String = "",
    val datePickerState: DatePickerState,
    val progress: Int = 0,
    val maxProgress: Int = 0,
    val level: Int = 0,
    val coins: Int = 0,
    val viewMode: ScheduleState.ViewMode,
    val viewModeIcon: Int,
    val viewModeTitle: String
) : ViewState {
//
//    enum class StateType {
//        LOADING, INITIAL, CALENDAR_DATE_CHANGED, SWIPE_DATE_CHANGED, DEFAULT, DATE_PICKER_CHANGED,
//        MONTH_CHANGED,
//        LEVEL_CHANGED, XP_AND_COINS_CHANGED, DATA_CHANGED
//    }

    enum class DatePickerState {
        INVISIBLE, SHOW_WEEK, SHOW_MONTH
    }
}