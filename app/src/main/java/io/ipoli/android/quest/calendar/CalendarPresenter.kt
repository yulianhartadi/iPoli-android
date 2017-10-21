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
                val dayText = calendarFormatter.day(date)
                val dateText = calendarFormatter.date(date)
                state.copy(
                    currentDate = intent.currentDate,
                    dayText = dayText,
                    dateText = dateText)
            }
            is ExpandToolbarIntent -> {
                when (state.toolbarState) {
                    SHRINKED -> state.copy(toolbarState = SHOW_WEEK)
                    SHOW_WEEK -> state.copy(toolbarState = SHOW_MONTH)
                    SHOW_MONTH -> state.copy(toolbarState = SHRINKED)
                }
            }
            else -> {
                state
            }
        }
}