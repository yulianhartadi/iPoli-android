package io.ipoli.android.quest.calendar.dayview.view

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.quest.calendar.dayview.view.widget.CalendarEvent
import io.ipoli.android.quest.calendar.dayview.view.widget.UnscheduledEvent
import io.ipoli.android.quest.usecase.Schedule
import io.ipoli.android.reminder.view.picker.ReminderViewModel
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/4/17.
 */

sealed class DayViewIntent : Intent

data class LoadDataIntent(val currentDate: LocalDate) : DayViewIntent()
data class AddEventIntent(val event: CalendarEvent) : DayViewIntent()
data class EditEventIntent(val event: CalendarEvent, val reminder: ReminderViewModel?) : DayViewIntent()
data class EditUnscheduledEventIntent(val event: UnscheduledEvent) : DayViewIntent()
data class RemoveEventIntent(val eventId: String) : DayViewIntent()
data class ScheduleLoadedIntent(val schedule: Schedule) : DayViewIntent()
data class UndoRemoveEventIntent(val eventId: String) : DayViewIntent()
data class ReminderPickedIntent(val reminder: ReminderViewModel?) : DayViewIntent()
data class CompleteQuestIntent(val questId: String) : DayViewIntent()
data class UndoCompleteQuestIntent(val questId: String) : DayViewIntent()

data class DayViewState(
    val type: StateType,
    val scheduledDate: LocalDate = LocalDate.now(),
    val scheduledQuests: List<DayViewController.QuestViewModel> = listOf(),
    val unscheduledQuests: List<DayViewController.UnscheduledQuestViewModel> = listOf(),
    val removedEventId: String = "",
    val reminder: ReminderViewModel? = null,
    val isReminderEdited: Boolean = false
) : ViewState {

    enum class StateType {
        LOADING, SCHEDULE_LOADED, EVENT_UPDATED, EVENT_VALIDATION_ERROR, EVENT_REMOVED,

        UNDO_REMOVED_EVENT,
        QUEST_COMPLETED,
        UNDO_QUEST_COMPLETED
    }
}