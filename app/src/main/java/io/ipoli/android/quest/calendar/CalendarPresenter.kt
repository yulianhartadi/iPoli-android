package io.ipoli.android.quest.calendar

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.text.CalendarFormatter
import io.ipoli.android.quest.calendar.CalendarViewState.ToolbarState.*
import org.threeten.bp.LocalDate
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/21/17.
 */
class CalendarPresenter(
    private val calendarFormatter: CalendarFormatter,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<CalendarViewState>, CalendarViewState, CalendarIntent>(
    CalendarViewState(currentDate = LocalDate.now(), toolbarState = SHRINKED),
    coroutineContext
) {

    override fun reduceState(intent: CalendarIntent, state: CalendarViewState): CalendarViewState =
        when (intent) {
            is LoadDataIntent -> {
                val date = intent.currentDate
                val (dayText, dateText) = formatDayAndDate(date)
                state.copy(
                    currentDate = intent.currentDate,
                    dayText = dayText,
                    dateText = dateText
                )
            }
            is ExpandToolbarIntent -> {
                when (state.toolbarState) {
                    SHRINKED -> state.copy(toolbarState = SHOW_WEEK)
                    else -> state.copy(toolbarState = SHRINKED)
                }
            }
            is ExpandToolbarWeekIntent -> {
                when (state.toolbarState) {
                    SHOW_WEEK -> state.copy(toolbarState = SHOW_MONTH)
                    else -> state.copy(toolbarState = SHOW_WEEK)
                }
            }
            is SwipeChangeDateIntent -> {
                val newDate = state.currentDate.plusDays((intent.position - MID_POSITION).toLong())
                val (dayText, dateText) = formatDayAndDate(newDate)
                state.copy(
                    currentDate = newDate,
                    dayText = dayText,
                    dateText = dateText
                )
            }
            else -> {
                state
            }
        }

    private fun formatDayAndDate(date: LocalDate): Pair<String, String> {
        val dayText = calendarFormatter.day(date)
        val dateText = calendarFormatter.date(date)
        return Pair(dayText, dateText)
    }

    companion object {
        const val MID_POSITION = 49
        const val MAX_VISIBLE_DAYS = 100
    }
}