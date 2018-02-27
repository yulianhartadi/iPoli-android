package mypoli.android.quest.schedule.calendar.dayview.view

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.quest.Icon
import mypoli.android.quest.Reminder
import mypoli.android.quest.schedule.calendar.CalendarViewState
import mypoli.android.quest.usecase.Result
import mypoli.android.quest.usecase.Schedule
import mypoli.android.reminder.view.picker.ReminderViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/4/17.
 */

sealed class DayViewIntent : Intent {
    data class DatePicked(val year: Int, val month: Int, val day: Int) : DayViewIntent()
}

data class LoadDataIntent(val currentDate: LocalDate) : DayViewIntent()

data class RemoveEventIntent(val eventId: String) : DayViewIntent()

data class ScheduleLoadedIntent(val schedule: Schedule) : DayViewIntent()
data class UndoRemoveEventIntent(val eventId: String) : DayViewIntent()
data class ReminderPickedIntent(val reminder: ReminderViewModel?) : DayViewIntent()
data class IconPickedIntent(val icon: Icon?) : DayViewIntent()
data class ColorPickedIntent(val color: AndroidColor) : DayViewIntent()
data class CompleteQuestIntent(val questId: String, val isStarted: Boolean) : DayViewIntent()
data class UndoCompleteQuestIntent(val questId: String) : DayViewIntent()
data class AddNewScheduledQuestIntent(val startTime: Time, val duration: Int) : DayViewIntent()
data class StartEditScheduledQuestIntent(val questViewModel: DayViewController.QuestViewModel) :
    DayViewIntent()

data class StartEditUnscheduledQuestIntent(val questViewModel: DayViewController.UnscheduledQuestViewModel) :
    DayViewIntent()


object EditQuestIntent : DayViewIntent()
object AddQuestIntent : DayViewIntent()
object EditUnscheduledQuestIntent : DayViewIntent()
data class DragMoveViewIntent(val startTime: Time?, val endTime: Time?) : DayViewIntent()
data class DragResizeViewIntent(val startTime: Time?, val endTime: Time?, val duration: Int) :
    DayViewIntent()

data class ChangeEditViewNameIntent(val name: String) : DayViewIntent()

sealed class DayViewAction : Action {
    data class Load(val currentDate: LocalDate) : DayViewAction()
}

object DayViewReducer : BaseViewStateReducer<DayViewState>() {

    override val stateKey = key<DayViewState>()

    override fun reduce(state: AppState, subState: DayViewState, action: Action): DayViewState {
        return when (action) {
            is DayViewAction.Load -> {

                val schedule = state.dataState.schedule
                if (schedule != null && schedule.date.isEqual(action.currentDate)) {
                    subState.copy(
                        type = DayViewState.StateType.SCHEDULE_LOADED,
                        schedule = schedule
                    )
                } else {
                    subState.copy(
                        type = DayViewState.StateType.LOADING
                    )
                }
            }

            is DataLoadedAction.ScheduledQuestsChanged -> {
                val schedule = action.schedule

                val calendarState = state.stateFor(CalendarViewState::class.java)
                val visibleDate = calendarState.currentDate

                if (schedule.date.isEqual(visibleDate)) {

                    subState.copy(
                        type = DayViewState.StateType.SCHEDULE_LOADED,
                        schedule = schedule
                    )
                } else {
                    subState.copy(
                        type = DayViewState.StateType.LOADING
                    )
                }
            }
            else -> subState
        }

    }


    private fun createDefaultReminder(scheduledDate: LocalDate, startMinute: Int) =
        Reminder("", Time.of(startMinute), scheduledDate)

    private fun createQuestReminder(
        reminder: ReminderViewModel?,
        scheduledDate: LocalDate,
        eventStartMinute: Int
    ) =
        reminder?.let {
            val time = Time.of(eventStartMinute)
            val questDateTime =
                LocalDateTime.of(scheduledDate, LocalTime.of(time.hours, time.getMinutes()))
            val reminderDateTime = questDateTime.minusMinutes(it.minutesFromStart)
            val toLocalTime = reminderDateTime.toLocalTime()
            Reminder(
                it.message,
                Time.at(toLocalTime.hour, toLocalTime.minute),
                reminderDateTime.toLocalDate()
            )
        }

    private fun savedQuestViewState(result: Result, state: DayViewState) =
        when (result) {
            is Result.Invalid -> {
                when (result.error) {

                    Result.ValidationError.EMPTY_NAME -> {
                        state.copy(type = DayViewState.StateType.EVENT_VALIDATION_EMPTY_NAME)
                    }

                    Result.ValidationError.TIMER_RUNNING -> {
                        state.copy(type = DayViewState.StateType.EVENT_VALIDATION_TIMER_RUNNING)
                    }
                }
            }
            else -> state.copy(
                type = DayViewState.StateType.EVENT_UPDATED,
                reminder = null,
                scheduledDate = null
            )
        }

    override fun defaultState() = DayViewState(type = DayViewState.StateType.LOADING)
}

data class DayViewState(
    val type: StateType,
//    val currentDate: LocalDate = LocalDate.now(),
    val schedule: Schedule? = null,
//    val scheduledQuests: List<DayViewController.QuestViewModel> = listOf(),
//    val unscheduledQuests: List<DayViewController.UnscheduledQuestViewModel> = listOf(),
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
        EVENT_VALIDATION_EMPTY_NAME,
        EVENT_VALIDATION_TIMER_RUNNING,
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