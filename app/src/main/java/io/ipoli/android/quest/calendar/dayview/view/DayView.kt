package io.ipoli.android.quest.calendar.dayview.view

import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.quest.calendar.dayview.view.DayViewState.StateType.LOADING
import io.ipoli.android.quest.calendar.dayview.view.widget.CalendarEvent
import io.ipoli.android.quest.calendar.dayview.view.widget.UnscheduledEvent
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/4/17.
 */

interface DayView : ViewStateRenderer<DayViewState> {
    fun loadScheduleIntent(): Observable<LocalDate>
    fun addEventIntent(): Observable<CalendarEvent>
    fun editEventIntent(): Observable<CalendarEvent>
    fun editUnscheduledEventIntent(): Observable<UnscheduledEvent>
    fun removeEventIntent(): Observable<String>
}

sealed class DayViewIntent

data class LoadScheduleIntent(val date: LocalDate) : DayViewIntent()
data class AddEventIntent(val event: CalendarEvent) : DayViewIntent()

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