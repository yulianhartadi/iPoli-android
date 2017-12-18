package io.ipoli.android.quest.calendar.dayview

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.AndroidIcon
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.calendar.dayview.view.*
import io.ipoli.android.quest.calendar.dayview.view.DayViewState.StateType.*
import io.ipoli.android.quest.usecase.*
import io.ipoli.android.reminder.view.picker.ReminderViewModel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */

class DayViewPresenter(
    private val loadScheduleUseCase: LoadScheduleForDateUseCase,
    private val saveQuestUseCase: SaveQuestUseCase,
    private val removeQuestUseCase: RemoveQuestUseCase,
    private val undoRemovedQuestUseCase: UndoRemovedQuestUseCase,
    private val completeQuestUseCase: CompleteQuestUseCase,
    private val undoCompletedQuestUseCase: UndoCompletedQuestUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<DayViewState>, DayViewState, DayViewIntent>(
    DayViewState(type = DayViewState.StateType.LOADING), coroutineContext) {

    override fun reduceState(intent: DayViewIntent, state: DayViewState) =
        when (intent) {

            is LoadDataIntent -> {
                launch {
                    loadScheduleUseCase.execute(intent.currentDate).consumeEach {
                        sendChannel.send(ScheduleLoadedIntent(it))
                    }
                }
                state.copy(type = LOADING)
            }

            is ScheduleLoadedIntent -> {
                val schedule = intent.schedule
                state.copy(
                    type = SCHEDULE_LOADED,
                    scheduledQuests = createScheduledViewModels(schedule),
                    unscheduledQuests = createUnscheduledViewModels(schedule),
                    scheduledDate = schedule.date
                )
            }

            is AddNewScheduledQuestIntent -> {
                state.copy(
                    type = ADD_NEW_SCHEDULED_QUEST,
                    editId = "",
                    name = "",
                    color = AndroidColor.GREEN,
                    icon = null,
                    startTime = intent.startTime,
                    duration = intent.duration,
                    endTime = Time.plusMinutes(intent.startTime, intent.duration)
                )
            }

            is StartEditScheduledQuestIntent -> {
                val vm = intent.questViewModel
                state.copy(
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

            is StartEditUnscheduledQuestIntent -> {
                val vm = intent.questViewModel
                state.copy(
                    type = START_EDIT_UNSCHEDULED_QUEST,
                    editId = vm.id,
                    name = vm.name,
                    color = vm.backgroundColor,
                    duration = vm.duration,
                    icon = vm.icon,
                    reminder = vm.reminder
                )
            }

            is AddQuestIntent -> {
                val color = Color.valueOf(state.color!!.name)

                val icon = state.icon?.let {
                    Icon.valueOf(it.name)
                }

                val reminder = if (state.reminder != null) {
                    createQuestReminder(state.reminder, state.scheduledDate, state.startTime!!.toMinuteOfDay())
                } else {
                    createDefaultReminder(state.scheduledDate, state.startTime!!.toMinuteOfDay())
                }

                val questParams = SaveQuestUseCase.Parameters(
                    name = state.name,
                    color = color,
                    icon = icon,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduledDate = state.scheduledDate,
                    startTime = state.startTime,
                    duration = state.duration!!,
                    reminder = reminder
                )
                val result = saveQuestUseCase.execute(questParams)
                savedQuestViewState(result, state)
            }

            is EditQuestIntent -> {
                val color = Color.valueOf(state.color!!.name)

                val icon = state.icon?.let {
                    Icon.valueOf(it.name)
                }

                val questParams = SaveQuestUseCase.Parameters(
                    id = state.editId,
                    name = state.name,
                    color = color,
                    icon = icon,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduledDate = state.scheduledDate,
                    startTime = state.startTime,
                    duration = state.duration!!,
                    reminder = createQuestReminder(state.reminder, state.scheduledDate, state.startTime!!.toMinuteOfDay())
                )
                val result = saveQuestUseCase.execute(questParams)
                savedQuestViewState(result, state)
            }

            is EditUnscheduledQuestIntent -> {
                val color = Color.valueOf(state.color!!.name)

                val icon = state.icon?.let {
                    Icon.valueOf(it.name)
                }

                val questParams = SaveQuestUseCase.Parameters(
                    id = state.editId,
                    name = state.name,
                    color = color,
                    icon = icon,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduledDate = state.scheduledDate,
                    startTime = state.startTime,
                    duration = state.duration!!,
//                    reminder = createQuestReminder(state.reminder, state.scheduledDate, state.startTime!!.toMinuteOfDay())
                    reminder = null
                )
                val result = saveQuestUseCase.execute(questParams)
                savedQuestViewState(result, state)
            }

//            is EditUnscheduledEventIntent -> {
//                val event = intent.event
//                val color = Color.valueOf(state.color!!.name)
//
//                val questParams = SaveQuestUseCase.Parameters(
//                    id = event.id,
//                    name = "",
//                    color = color,
//                    category = Category("WELLNESS", Color.GREEN),
//                    scheduledDate = state.scheduledDate,
//                    startTime = null,
//                    duration = event.duration
//                )
//                val result = saveQuestUseCase.execute(questParams)
//                savedQuestViewState(result, state)
//            }

            is RemoveEventIntent -> {
                val eventId = intent.eventId
                removeQuestUseCase.execute(eventId)
                if (eventId.isEmpty()) {
                    state.copy(
                        type = NEW_EVENT_REMOVED
                    )
                } else {
                    state.copy(
                        type = DayViewState.StateType.EVENT_REMOVED,
                        removedEventId = eventId,
                        reminder = null
                    )
                }
            }

            is UndoRemoveEventIntent -> {
                val eventId = intent.eventId
                undoRemovedQuestUseCase.execute(eventId)
                state.copy(type = DayViewState.StateType.UNDO_REMOVED_EVENT, removedEventId = "")
            }

            is ReminderPickedIntent -> {
                state.copy(
                    type = REMINDER_PICKED,
                    reminder = intent.reminder
                )
            }

            is IconPickedIntent -> {
                state.copy(
                    type = ICON_PICKED,
                    icon = intent.icon?.let {
                        AndroidIcon.valueOf(it.name)
                    }
                )
            }

            is ColorPickedIntent -> {
                state.copy(
                    type = COLOR_PICKED,
                    color = intent.color
                )
            }

            is CompleteQuestIntent -> {
                completeQuestUseCase.execute(intent.questId)
                state.copy(type = QUEST_COMPLETED)
            }

            is UndoCompleteQuestIntent -> {
                undoCompletedQuestUseCase.execute(intent.questId)
                state.copy(type = UNDO_QUEST_COMPLETED)
            }

            is DragMoveViewIntent -> {
                state.copy(
                    type = EDIT_VIEW_DRAGGED,
                    startTime = intent.startTime,
                    endTime = intent.endTime
                )
            }

            is DragResizeViewIntent -> {
                state.copy(
                    type = EDIT_VIEW_DRAGGED,
                    startTime = intent.startTime,
                    endTime = intent.endTime,
                    duration = intent.duration
                )
            }

            is ChangeEditViewNameIntent -> {
                state.copy(
                    type = EDIT_VIEW_NAME_CHANGED,
                    name = intent.name
                )
            }
        }

    private fun createDefaultReminder(scheduledDate: LocalDate, startMinute: Int) =
        Reminder("", Time.of(startMinute), scheduledDate)

    private fun createQuestReminder(reminder: ReminderViewModel?, scheduledDate: LocalDate, eventStartMinute: Int) =
        reminder?.let {
            val time = Time.of(eventStartMinute)
            val questDateTime = LocalDateTime.of(scheduledDate, LocalTime.of(time.hours, time.getMinutes()))
            val reminderDateTime = questDateTime.minusMinutes(it.minutesFromStart)
            val toLocalTime = reminderDateTime.toLocalTime()
            Reminder(it.message, Time.at(toLocalTime.hour, toLocalTime.minute), reminderDateTime.toLocalDate())
        }

    private fun savedQuestViewState(result: Result, state: DayViewState) =
        when (result) {
            is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
            else -> state.copy(type = EVENT_UPDATED, reminder = null)
        }

    private fun createUnscheduledViewModels(schedule: Schedule): List<DayViewController.UnscheduledQuestViewModel> =
        schedule.unscheduled.map {
            val color = AndroidColor.valueOf(it.color.name)
            DayViewController.UnscheduledQuestViewModel(
                it.id,
                it.name,
                it.duration,
                it.icon?.let { AndroidIcon.valueOf(it.name) },
                color,
                color.color900,
                it.isCompleted
            )
        }

    private fun createScheduledViewModels(schedule: Schedule): List<DayViewController.QuestViewModel> =
        schedule.scheduled.map { q ->
            val color = AndroidColor.valueOf(q.color.name)

            val reminder = q.reminder?.let {
                val daysDiff = ChronoUnit.DAYS.between(q.scheduledDate, it.remindDate)
                val minutesDiff = q.startTime!!.toMinuteOfDay() - it.remindTime.toMinuteOfDay()
                ReminderViewModel(it.message, minutesDiff + Time.MINUTES_IN_A_DAY * daysDiff)
            }

            DayViewController.QuestViewModel(
                q.id,
                q.name,
                q.duration,
                q.startTime!!.toMinuteOfDay(),
                q.startTime.toString(),
                q.endTime.toString(),
                q.icon?.let { AndroidIcon.valueOf(it.name) },
                color,
                color.color900,
                reminder,
                q.isCompleted
            )
        }
}