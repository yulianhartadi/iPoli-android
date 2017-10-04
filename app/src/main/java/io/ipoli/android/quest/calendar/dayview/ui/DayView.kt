package io.ipoli.android.quest.calendar.dayview.ui

import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.quest.calendar.dayview.ui.widget.CalendarEvent
import io.ipoli.android.quest.calendar.dayview.ui.widget.UnscheduledEvent
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/4/17.
 */

data class EditEventRequest(val event: CalendarEvent, val eventId: String)
data class EditUnscheduledEventRequest(val event: UnscheduledEvent, val eventId: String)

interface DayView : ViewStateRenderer<DayViewState> {
    fun loadScheduleIntent(): Observable<LocalDate>
    fun addEventIntent(): Observable<CalendarEvent>
    fun editEventIntent(): Observable<EditEventRequest>
    fun editUnscheduledEventIntent(): Observable<EditUnscheduledEventRequest>
}

sealed class DayViewState {
    object Loading : DayViewState()
    data class ScheduleLoaded(val scheduledQuests: List<DayViewController.QuestViewModel>,
                              val unscheduledQuests: List<DayViewController.UnscheduledQuestViewModel>) : DayViewState()

    object EventUpdated : DayViewState()
    object EventValidationError : DayViewState()
}