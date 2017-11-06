package io.ipoli.android.quest.calendar.dayview

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.calendar.dayview.view.*
import io.ipoli.android.quest.calendar.dayview.view.DayViewState.StateType.*
import io.ipoli.android.quest.calendar.dayview.view.widget.CalendarEvent
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
    private val undoCompleteQuestUseCase: UndoCompleteQuestUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<DayViewState>, DayViewState, DayViewIntent>(
    DayViewState(type = DayViewState.StateType.LOADING), coroutineContext) {

    override fun reduceState(intent: DayViewIntent, state: DayViewState) =
        when (intent) {

            is LoadDataIntent -> {
                launch {
                    loadScheduleUseCase.execute(intent.currentDate).consumeEach {
                        actor.send(ScheduleLoadedIntent(it))
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

            is AddEventIntent -> {
                val event = intent.event
                val colorName = Color.valueOf(event.backgroundColor.name)

                val reminder = createQuestReminder(state.reminder, state.scheduledDate, event)

                val questParams = SaveQuestUseCase.Parameters(
                    name = event.name,
                    color = colorName,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduledDate = state.scheduledDate,
                    startTime = Time.of(event.startMinute),
                    duration = event.duration,
                    reminder = reminder
                )
                val result = saveQuestUseCase.execute(questParams)
                savedQuestViewState(result, state)
            }

            is EditEventIntent -> {
                val event = intent.event
                val colorName = Color.valueOf(event.backgroundColor.name)

                val reminderVM = if (state.isReminderEdited) state.reminder else intent.reminder

                val questParams = SaveQuestUseCase.Parameters(
                    id = event.id,
                    name = event.name,
                    color = colorName,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduledDate = state.scheduledDate,
                    startTime = Time.of(event.startMinute),
                    duration = event.duration,
                    reminder = createQuestReminder(reminderVM, state.scheduledDate, event)
                )
                val result = saveQuestUseCase.execute(questParams)
                savedQuestViewState(result, state)
            }

            is EditUnscheduledEventIntent -> {
                val event = intent.event
                val colorName = Color.valueOf(event.backgroundColor.name)
                val questParams = SaveQuestUseCase.Parameters(
                    id = event.id,
                    name = event.name,
                    color = colorName,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduledDate = state.scheduledDate,
                    startTime = null,
                    duration = event.duration
                )
                val result = saveQuestUseCase.execute(questParams)
                savedQuestViewState(result, state)
            }

            is RemoveEventIntent -> {
                val eventId = intent.eventId
                val scheduledQuests = state.scheduledQuests.toMutableList()
                val unscheduledQuests = state.unscheduledQuests.toMutableList()
                scheduledQuests.find { it.id == eventId }?.let { scheduledQuests.remove(it) }
                unscheduledQuests.find { it.id == eventId }?.let { unscheduledQuests.remove(it) }
                removeQuestUseCase.execute(eventId)
                if (eventId.isEmpty()) {
                    state
                } else {
                    state.copy(
                        type = DayViewState.StateType.EVENT_REMOVED,
                        removedEventId = eventId,
                        scheduledQuests = scheduledQuests,
                        unscheduledQuests = unscheduledQuests,
                        reminder = null,
                        isReminderEdited = false
                    )
                }
            }

            is UndoRemoveEventIntent -> {
                val eventId = intent.eventId
                undoRemovedQuestUseCase.execute(eventId)
                state.copy(type = DayViewState.StateType.UNDO_REMOVED_EVENT, removedEventId = "")
            }

            is ReminderPickedIntent -> {
                state.copy(reminder = intent.reminder, isReminderEdited = true)
            }

            is CompleteQuestIntent -> {
                completeQuestUseCase.execute(intent.questId)
                state.copy(type = QUEST_COMPLETED)
            }

            is UndoCompleteQuestIntent -> {
                undoCompleteQuestUseCase.execute(intent.questId)
                state.copy(type = UNDO_QUEST_COMPLETED)
            }
        }

    private fun createQuestReminder(reminder: ReminderViewModel?, scheduledDate: LocalDate, event: CalendarEvent): Reminder? {
        return reminder?.let {
            val time = Time.of(event.startMinute)
            val questDateTime = LocalDateTime.of(scheduledDate, LocalTime.of(time.hours, time.getMinutes()))
            val reminderDateTime = questDateTime.minusMinutes(it.minutesFromStart)
            val toLocalTime = reminderDateTime.toLocalTime()
            Reminder(it.message, Time.at(toLocalTime.hour, toLocalTime.minute), reminderDateTime.toLocalDate())
        }
    }

    private fun savedQuestViewState(result: Result, state: DayViewState) =
        when (result) {
            is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
            else -> state.copy(type = EVENT_UPDATED, reminder = null, isReminderEdited = false)
        }

    private fun createUnscheduledViewModels(schedule: Schedule): List<DayViewController.UnscheduledQuestViewModel> =
        schedule.unscheduled.map {
            val color = AndroidColor.valueOf(it.color.name)
            DayViewController.UnscheduledQuestViewModel(
                it.id,
                it.name,
                it.duration,
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
                color,
                color.color900,
                reminder,
                q.isCompleted
            )
        }
}