package io.ipoli.android.quest.calendar.dayview.view

import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.quest.calendar.dayview.view.DayViewState.StateType.*
import io.ipoli.android.quest.calendar.dayview.view.widget.CalendarEvent
import io.ipoli.android.quest.calendar.dayview.view.widget.UnscheduledEvent
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

data class DayViewState(
    val type: StateType,
    val scheduledQuests: List<DayViewController.QuestViewModel> = listOf(),
    val unscheduledQuests: List<DayViewController.UnscheduledQuestViewModel> = listOf()
) {

    companion object {
        val Loading = DayViewState(type = LOADING)
    }

    enum class StateType {
        LOADING, SCHEDULE_LOADED, EVENT_UPDATED, EVENT_VALIDATION_ERROR
    }
}