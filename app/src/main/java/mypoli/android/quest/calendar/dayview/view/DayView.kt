package mypoli.android.quest.calendar.dayview.view

import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.quest.Icon
import mypoli.android.quest.usecase.Schedule
import mypoli.android.reminder.view.picker.ReminderViewModel
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/4/17.
 */

sealed class DayViewIntent : Intent {
    data class DatePicked(val year: Int, val month: Int, val day: Int) : DayViewIntent()
}

data class LoadDataIntent(val currentDate: LocalDate) : DayViewIntent()
//data class StartEditQuestIntent(val questViewModel: DayViewController.QuestViewModel) : DayViewIntent()
//data class StartEditUnscheduledQuestIntent(val viewModel: DayViewController.UnscheduledQuestViewModel) : DayViewIntent()
//data class AddEventIntent(val event: CalendarEvent) : DayViewIntent()

//data class EditEventIntent(val event: CalendarEvent, val reminder: ReminderViewModel?) : DayViewIntent()


//data class EditUnscheduledEventIntent(val event: UnscheduledEvent) : DayViewIntent()
data class RemoveEventIntent(val eventId: String) : DayViewIntent()

data class ScheduleLoadedIntent(val schedule: Schedule) : DayViewIntent()
data class UndoRemoveEventIntent(val eventId: String) : DayViewIntent()
data class ReminderPickedIntent(val reminder: ReminderViewModel?) : DayViewIntent()
data class IconPickedIntent(val icon: Icon?) : DayViewIntent()
data class ColorPickedIntent(val color: AndroidColor) : DayViewIntent()
data class CompleteQuestIntent(val questId: String) : DayViewIntent()
data class UndoCompleteQuestIntent(val questId: String) : DayViewIntent()
data class AddNewScheduledQuestIntent(val startTime: Time, val duration: Int) : DayViewIntent()
data class StartEditScheduledQuestIntent(val questViewModel: DayViewController.QuestViewModel) : DayViewIntent()
data class StartEditUnscheduledQuestIntent(val questViewModel: DayViewController.UnscheduledQuestViewModel) : DayViewIntent()


object EditQuestIntent : DayViewIntent()
object AddQuestIntent : DayViewIntent()
object EditUnscheduledQuestIntent : DayViewIntent()
data class DragMoveViewIntent(val startTime: Time?, val endTime: Time?) : DayViewIntent()
data class DragResizeViewIntent(val startTime: Time?, val endTime: Time?, val duration: Int) : DayViewIntent()
data class ChangeEditViewNameIntent(val name: String) : DayViewIntent()

data class DayViewState(
    val type: StateType,
    val currentDate: LocalDate = LocalDate.now(),
    val scheduledQuests: List<DayViewController.QuestViewModel> = listOf(),
    val unscheduledQuests: List<DayViewController.UnscheduledQuestViewModel> = listOf(),
    val removedEventId: String = "",
    val editId: String = "",
    val name: String = "",
    val scheduledDate: LocalDate? = null,
    val startTime: Time? = null,
    val endTime: Time? = null,
    val duration: Int? = null,
    val color: AndroidColor? = null,
    val reminder: ReminderViewModel? = null,
    val icon: AndroidIcon? = null
) : ViewState {

    enum class StateType {
        LOADING,
        SCHEDULE_LOADED,
        ADD_NEW_SCHEDULED_QUEST,
        START_EDIT_SCHEDULED_QUEST,
        START_EDIT_UNSCHEDULED_QUEST,
        ICON_PICKED,
        COLOR_PICKED,
        EDIT_QUEST,
        EVENT_UPDATED,
        EVENT_VALIDATION_ERROR,
        NEW_EVENT_REMOVED,
        EVENT_REMOVED,
        UNDO_REMOVED_EVENT,
        QUEST_COMPLETED,
        UNDO_QUEST_COMPLETED,
        EDIT_VIEW_DRAGGED,
        EDIT_VIEW_NAME_CHANGED,
        REMINDER_PICKED,
        DATE_PICKED
    }
}