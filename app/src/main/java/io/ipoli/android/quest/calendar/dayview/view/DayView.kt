package io.ipoli.android.quest.calendar.dayview.view

import io.ipoli.android.quest.calendar.dayview.view.widget.CalendarEvent
import io.ipoli.android.quest.calendar.dayview.view.widget.UnscheduledEvent
import io.ipoli.android.quest.usecase.Schedule
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/4/17.
 */

sealed class DayViewIntent : Intent

data class AddEventIntent(val event: CalendarEvent) : DayViewIntent()
data class EditEventIntent(val event: CalendarEvent) : DayViewIntent()
data class EditUnscheduledEventIntent(val event: UnscheduledEvent) : DayViewIntent()
data class RemoveEventIntent(val eventId: String) : DayViewIntent()
data class ScheduleLoadedIntent(val schedule: Schedule) : DayViewIntent()

interface ViewState

interface Intent

data class DayViewState(
    val type: StateType,
    val scheduledDate: LocalDate = LocalDate.now(),
    val scheduledQuests: List<DayViewController.QuestViewModel> = listOf(),
    val unscheduledQuests: List<DayViewController.UnscheduledQuestViewModel> = listOf()
) : ViewState {

    enum class StateType {
        LOADING, SCHEDULE_LOADED, EVENT_UPDATED, EVENT_VALIDATION_ERROR
    }
}