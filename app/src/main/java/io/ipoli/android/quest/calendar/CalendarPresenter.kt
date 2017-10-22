package io.ipoli.android.quest.calendar

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.text.CalendarFormatter
import io.ipoli.android.quest.calendar.CalendarViewState.DatePickerState.*
import io.ipoli.android.quest.calendar.CalendarViewState.StateType.DATE_CHANGED
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/21/17.
 */
class CalendarPresenter(
    private val calendarFormatter: CalendarFormatter,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<CalendarViewState>, CalendarViewState, CalendarIntent>(
    CalendarViewState(
        currentDate = LocalDate.now(),
        datePickerState = INVISIBLE,
        adapterPosition = MID_POSITION
    ),
    coroutineContext
) {

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM")

    override fun reduceState(intent: CalendarIntent, state: CalendarViewState): CalendarViewState =
        when (intent) {
            is LoadDataIntent -> {
                val date = intent.currentDate
                val (dayText, dateText) = formatDayAndDate(date)
                state.copy(
                    currentDate = intent.currentDate,
                    dayText = dayText,
                    monthText = monthFormatter.format(date),
                    dateText = dateText
                )
            }
            is ExpandToolbarIntent -> {
                when (state.datePickerState) {
                    INVISIBLE -> state.copy(datePickerState = SHOW_WEEK)
                    else -> state.copy(datePickerState = INVISIBLE)
                }
            }
            is ExpandToolbarWeekIntent -> {
                when (state.datePickerState) {
                    SHOW_WEEK -> state.copy(datePickerState = SHOW_MONTH)
                    else -> state.copy(datePickerState = SHOW_WEEK)
                }
            }
            is ChangeDateIntent -> {
                val newDate = LocalDate.of(intent.year, intent.month, intent.day)
                val (dayText, dateText) = formatDayAndDate(newDate)
                state.copy(
                    type = DATE_CHANGED,
                    adapterPosition = MID_POSITION,
                    currentDate = newDate,
                    dayText = dayText,
                    monthText = monthFormatter.format(newDate),
                    dateText = dateText
                )
            }
            is SwipeChangeDateIntent -> {
                val newDate = state.currentDate.plusDays((intent.position - MID_POSITION).toLong())
                val (dayText, dateText) = formatDayAndDate(newDate)
                state.copy(
                    currentDate = newDate,
                    dayText = dayText,
                    monthText = monthFormatter.format(newDate),
                    dateText = dateText
                )
            }
            is ChangeMonthIntent -> {
                val newDate = LocalDate.of(intent.year, intent.month, 1)
                state.copy(
                    monthText = monthFormatter.format(newDate)
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