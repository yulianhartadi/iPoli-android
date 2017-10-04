package io.ipoli.android.quest.calendar.dayview.ui

import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.quest.calendar.dayview.ui.widget.CalendarEvent
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/4/17.
 */

interface DayView : ViewStateRenderer<DayViewState> {
    fun loadScheduleIntent(): Observable<LocalDate>
    fun addEventIntent(): Observable<CalendarEvent>
    fun editEventIntent(): Observable<Pair<CalendarEvent, String>>
}

sealed class DayViewState {
    object Loading : DayViewState()
    data class ScheduleLoaded(val scheduledQuests: List<DayViewController.QuestViewModel>,
                              val unscheduledQuests: List<DayViewController.UnscheduledQuestViewModel>) : DayViewState()

    object EventUpdated : DayViewState()
    object EventValidationError : DayViewState()
}