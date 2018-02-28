package mypoli.android.quest.schedule.calendar.dayview.view

import mypoli.android.common.AppState
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.NamespaceViewStateReducer
import mypoli.android.common.datetime.Time
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.quest.Icon
import mypoli.android.quest.Reminder
import mypoli.android.quest.schedule.calendar.dayview.view.DayViewState.StateType.*
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
    data class Load(val currentDate: LocalDate) :
        DayViewAction()

    data class StartEditScheduledQuest(val questViewModel: DayViewController.QuestViewModel) :
        DayViewAction()

    data class CompleteQuest(val questId: String, val isStarted: Boolean) : DayViewAction()
    data class UndoCompleteQuest(val questId: String) : DayViewAction()
    data class AddNewScheduledQuest(val startTime: Time, val duration: Int) : DayViewAction()
    data class DragResizeView(val startTime: Time?, val endTime: Time?, val duration: Int) :
        DayViewAction()

    data class DragMoveView(val startTime: Time?, val endTime: Time?) : DayViewAction()
    object AddQuest : DayViewAction()
    object QuestAdded : DayViewAction()
    data class AddInvalidQuest(val result: Result.Invalid) : DayViewAction()
}

class DayViewReducer(namespace: String) : NamespaceViewStateReducer<DayViewState>(namespace) {

    override val stateKey = namespace + "/" + key<DayViewState>()

    override fun doReduce(state: AppState, subState: DayViewState, action: Action): DayViewState {
        if (action is DayViewAction) {
            return reduceDayViewAction(state, subState, action)
        }

        return when (action) {
            is DataLoadedAction.ScheduledQuestsChanged -> {
                val schedule = action.schedule

                if (schedule.date.isEqual(subState.currentDate)) {
                    subState.copy(
                        type = SCHEDULE_LOADED,
                        schedule = schedule
                    )
                } else {
                    subState
                }
            }

            else -> subState
        }

    }

    private fun reduceDayViewAction(
        state: AppState,
        subState: DayViewState,
        action: DayViewAction
    ): DayViewState {
        return when (action) {
            is DayViewAction.Load -> {
                val schedule = state.dataState.schedule
                if (schedule != null && schedule.date.isEqual(action.currentDate)) {
                    subState.copy(
                        type = SCHEDULE_LOADED,
                        schedule = schedule,
                        currentDate = action.currentDate
                    )
                } else {
                    subState.copy(
                        type = LOADING,
                        currentDate = action.currentDate
                    )
                }
            }

            is DayViewAction.StartEditScheduledQuest -> {
                val vm = action.questViewModel
                subState.copy(
                    type = START_EDIT_SCHEDULED_QUEST,
                    editId = vm.id,
                    name = vm.name,
                    color = vm.backgroundColor,
                    startTime = Time.of(vm.startMinute),
                    duration = vm.duration,
                    endTime = Time.plusMinutes(Time.of(vm.startMinute), vm.duration),
                    icon = vm.icon,
                    reminder = vm.reminder
                )
            }

            is DayViewAction.CompleteQuest -> {
                subState.copy(
                    type = QUEST_COMPLETED
                )
            }

            is DayViewAction.UndoCompleteQuest -> {
                subState.copy(
                    type = UNDO_QUEST_COMPLETED
                )
            }

            is DayViewAction.AddNewScheduledQuest -> {
                subState.copy(
                    type = ADD_NEW_SCHEDULED_QUEST,
                    editId = "",
                    name = "",
                    color = AndroidColor.GREEN,
                    icon = null,
                    startTime = action.startTime,
                    duration = action.duration,
                    endTime = Time.plusMinutes(action.startTime, action.duration)
                )
            }

            is DayViewAction.DragResizeView -> {
                subState.copy(
                    type = EDIT_VIEW_DRAGGED,
                    startTime = action.startTime,
                    endTime = action.endTime,
                    duration = action.duration
                )
            }

            is DayViewAction.DragMoveView -> {
                subState.copy(
                    type = EDIT_VIEW_DRAGGED,
                    startTime = action.startTime,
                    endTime = action.endTime
                )
            }

            DayViewAction.QuestAdded -> {
                subState.copy(type = EVENT_UPDATED, reminder = null, scheduledDate = null)
            }

            is DayViewAction.AddInvalidQuest -> {
                when (action.result.error) {

                    Result.ValidationError.EMPTY_NAME -> {
                        subState.copy(type = EVENT_VALIDATION_EMPTY_NAME)
                    }

                    Result.ValidationError.TIMER_RUNNING -> {
                        subState.copy(type = EVENT_VALIDATION_TIMER_RUNNING)
                    }
                }
            }

            else -> {
                subState
            }
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
                        state.copy(type = EVENT_VALIDATION_EMPTY_NAME)
                    }

                    Result.ValidationError.TIMER_RUNNING -> {
                        state.copy(type = EVENT_VALIDATION_TIMER_RUNNING)
                    }
                }
            }
            else -> state.copy(
                type = EVENT_UPDATED,
                reminder = null,
                scheduledDate = null
            )
        }

    override fun defaultState() =
        DayViewState(type = LOADING)
}

data class DayViewState(
    val type: StateType,
    val currentDate: LocalDate = LocalDate.now(),
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