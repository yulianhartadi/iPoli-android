package mypoli.android.quest.schedule

import android.content.Context
import mypoli.android.R
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter
import mypoli.android.common.text.CalendarFormatter
import org.threeten.bp.format.DateTimeFormatter

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/22/2018.
 */
class SchedulePresenter : AndroidStatePresenter<AppState, ScheduleViewState> {

    override fun present(state: AppState, context: Context): ScheduleViewState {

        val calendarState = state.scheduleState

        val calendarFormatter = CalendarFormatter(context)

        val dayText = calendarFormatter.day(calendarState.currentDate)
        val dateText = calendarFormatter.date(calendarState.currentDate)

        val monthFormatter = DateTimeFormatter.ofPattern("MMMM")

        return ScheduleViewState(
            type = calendarState.type,
            currentDate = calendarState.currentDate,
            datePickerState = calendarState.datePickerState,
            dateText = dateText,
            dayText = dayText,
            monthText = monthFormatter.format(calendarState.currentMonth),
            progress = calendarState.progress,
            maxProgress = calendarState.maxProgress,
            level = calendarState.level,
            coins = calendarState.coins,
            viewMode = calendarState.viewMode,
            viewModeIcon = if (calendarState.viewMode == ScheduleState.ViewMode.CALENDAR)
                R.drawable.ic_format_list_bulleted_white_24dp
            else
                R.drawable.ic_event_white_24dp,
            viewModeTitle = if (calendarState.viewMode == ScheduleState.ViewMode.CALENDAR) "Agenda" else "Calendar"
        )
    }

    override fun presentInitial(state: ScheduleViewState) =
        state.copy(
            type = ScheduleState.StateType.INITIAL
        )

}