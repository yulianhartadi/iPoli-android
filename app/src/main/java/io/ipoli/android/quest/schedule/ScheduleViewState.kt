package io.ipoli.android.quest.schedule

import android.content.Context
import io.ipoli.android.common.AppDataState
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction

import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.common.text.CalendarFormatter
import io.ipoli.android.quest.schedule.agenda.view.AgendaAction
import io.ipoli.android.quest.schedule.agenda.view.AgendaViewState
import io.ipoli.android.quest.schedule.calendar.CalendarAction
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/21/17.
 */

sealed class ScheduleAction : Action {
    object ExpandToolbar : ScheduleAction()
    object ExpandWeekToolbar : ScheduleAction()

    data class ScheduleChangeDate(val date: LocalDate) : ScheduleAction() {
        override fun toMap() = mapOf("date" to date)
    }

    data class ChangeMonth(val year: Int, val month: Int) : ScheduleAction() {
        override fun toMap() = mapOf("year" to year, "month" to month)
    }

    object ToggleViewMode : ScheduleAction()
    data class Load(val currentDate: LocalDate) : ScheduleAction()
}

object ScheduleReducer : BaseViewStateReducer<ScheduleViewState>() {

    override val stateKey = key<ScheduleViewState>()

    override fun defaultState() =
        ScheduleViewState(
            type = ScheduleViewState.StateType.LOADING,
            showDailyChallenge = false,
            currentMonth = YearMonth.now(),
            currentDate = LocalDate.now(),
            viewMode = ScheduleViewState.ViewMode.AGENDA,
            datePickerState = ScheduleViewState.DatePickerState.INVISIBLE
        )

    override fun reduce(state: AppState, subState: ScheduleViewState, action: Action) =
        when (action) {

            is ScheduleAction -> reduceCalendarAction(
                state.dataState,
                subState,
                action
            )

            is DataLoadedAction.PlayerChanged -> {
                subState.copy(
                    type = ScheduleViewState.StateType.SHOW_DAILY_CHALLENGE_CHANGED,
                    showDailyChallenge = action.player.preferences.planDays.contains(
                        LocalDate.now().dayOfWeek
                    )
                )
            }

            is CalendarAction.ChangeVisibleDate -> {
                subState.copy(
                    type = ScheduleViewState.StateType.SWIPE_DATE_CHANGED,
                    currentDate = action.date
                )
            }
            is AgendaAction.FirstVisibleItemChanged -> {

                val itemPos = action.itemPosition
                val startDate =
                    state.stateFor(AgendaViewState::class.java).agendaItems[itemPos].startDate()

                if (subState.currentDate.isEqual(startDate)) {
                    subState.copy(
                        type = ScheduleViewState.StateType.IDLE
                    )
                } else {
                    subState.copy(
                        type = ScheduleViewState.StateType.DATE_AUTO_CHANGED,
                        currentDate = startDate,
                        currentMonth = YearMonth.of(startDate.year, startDate.month)
                    )
                }

            }
            else -> subState
        }

    private fun reduceCalendarAction(
        dataState: AppDataState,
        state: ScheduleViewState,
        action: ScheduleAction
    ) =
        when (action) {
            is ScheduleAction.Load -> {
                if (state.type != ScheduleViewState.StateType.LOADING) {
                    state
                } else {
                    state.copy(
                        type = ScheduleViewState.StateType.INITIAL,
                        showDailyChallenge = dataState.player?.preferences?.planDays?.contains(
                            action.currentDate.dayOfWeek
                        ) ?: false
                    )
                }
            }

            ScheduleAction.ExpandWeekToolbar -> {
                when (state.datePickerState) {
                    ScheduleViewState.DatePickerState.SHOW_WEEK -> state.copy(
                        type = ScheduleViewState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = ScheduleViewState.DatePickerState.SHOW_MONTH
                    )
                    else -> state.copy(
                        type = ScheduleViewState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = ScheduleViewState.DatePickerState.SHOW_WEEK
                    )
                }
            }

            is ScheduleAction.ScheduleChangeDate -> {
                state.copy(
                    type = ScheduleViewState.StateType.CALENDAR_DATE_CHANGED,
                    currentDate = action.date,
                    currentMonth = YearMonth.of(action.date.year, action.date.month)
                )
            }

            ScheduleAction.ExpandToolbar -> {
                when (state.datePickerState) {
                    ScheduleViewState.DatePickerState.INVISIBLE -> state.copy(
                        type = ScheduleViewState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = ScheduleViewState.DatePickerState.SHOW_WEEK
                    )
                    else -> state.copy(
                        type = ScheduleViewState.StateType.DATE_PICKER_CHANGED,
                        datePickerState = ScheduleViewState.DatePickerState.INVISIBLE
                    )
                }
            }

            is ScheduleAction.ChangeMonth -> {
                state.copy(
                    type = ScheduleViewState.StateType.MONTH_CHANGED,
                    currentMonth = YearMonth.of(action.year, action.month)
                )
            }

            is ScheduleAction.ToggleViewMode -> {
                state.copy(
                    type = ScheduleViewState.StateType.VIEW_MODE_CHANGED,
                    viewMode = if (state.viewMode == ScheduleViewState.ViewMode.CALENDAR) ScheduleViewState.ViewMode.AGENDA else ScheduleViewState.ViewMode.CALENDAR
                )
            }
        }
}

data class ScheduleViewState(
    val type: StateType,
    val showDailyChallenge: Boolean,
    val currentMonth: YearMonth,
    val currentDate: LocalDate,
    val datePickerState: DatePickerState,
    val viewMode: ViewMode
) : BaseViewState() {

    enum class StateType {
        LOADING, INITIAL, IDLE,
        SHOW_DAILY_CHALLENGE_CHANGED,
        CALENDAR_DATE_CHANGED,
        SWIPE_DATE_CHANGED,
        DATE_PICKER_CHANGED,
        MONTH_CHANGED,
        VIEW_MODE_CHANGED,
        DATE_AUTO_CHANGED
    }

    enum class ViewMode {
        CALENDAR, AGENDA
    }

    enum class DatePickerState {
        INVISIBLE, SHOW_WEEK, SHOW_MONTH
    }
}


val ScheduleViewState.viewModeTitle
    get() = if (viewMode == ScheduleViewState.ViewMode.CALENDAR) "Agenda" else "Calendar"

fun ScheduleViewState.dayText(context: Context) =
    CalendarFormatter(context).day(currentDate)

fun ScheduleViewState.dateText(context: Context) =
    CalendarFormatter(context).date(currentDate)

val ScheduleViewState.monthText: String
    get() = DateTimeFormatter.ofPattern("MMMM").format(currentMonth)