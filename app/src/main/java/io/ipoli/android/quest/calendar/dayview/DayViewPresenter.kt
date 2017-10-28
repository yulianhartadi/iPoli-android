package io.ipoli.android.quest.calendar.dayview

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.calendar.dayview.view.*
import io.ipoli.android.quest.calendar.dayview.view.DayViewState.StateType.*
import io.ipoli.android.quest.usecase.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDate
import timber.log.Timber
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
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<DayViewState>, DayViewState, DayViewIntent>(
    DayViewState(type = DayViewState.StateType.LOADING), coroutineContext) {

    override fun reduceState(intent: DayViewIntent, state: DayViewState) =
        when (intent) {

            is LoadDataIntent -> {
                launch {
                    loadScheduleUseCase.execute(intent.currentDate).consumeEach {
//                        Timber.d("AAA Schedule Loaded")
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
                val quest = Quest(
                    name = event.name,
                    color = colorName,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduleDate = state.scheduledDate,
                    startTime =Time.of(event.startMinute),
                    duration = event.duration,
                    reminder = Reminder("Waga waga wag", Time.at(18, 0), LocalDate.now())
                )
                val result = saveQuestUseCase.execute(quest)
                Timber.d("AAAAA presenter $result")
                savedQuestViewState(result, state)
            }

            is EditEventIntent -> {
                val event = intent.event
                val colorName = Color.valueOf(event.backgroundColor.name)
                val quest = Quest(
                    id = event.id,
                    name = event.name,
                    color = colorName,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduleDate = state.scheduledDate,
                    startTime =Time.of(event.startMinute),
                    duration = event.duration
                )
                val result = saveQuestUseCase.execute(quest)
                savedQuestViewState(result, state)
            }

            is EditUnscheduledEventIntent -> {
                val event = intent.event
                val colorName = Color.valueOf(event.backgroundColor.name)
                val quest = Quest(
                    id = event.id,
                    name = event.name,
                    color = colorName,
                    category = Category("WELLNESS", Color.GREEN),
                    scheduleDate = state.scheduledDate,
                    startTime = null,
                    duration = event.duration
                )
                val result = saveQuestUseCase.execute(quest)
                savedQuestViewState(result, state)
            }

            is RemoveEventIntent -> {
                val eventId = intent.eventId
                val scheduledQuests = state.scheduledQuests.toMutableList()
                val unscheduledQuests = state.unscheduledQuests.toMutableList()
                scheduledQuests.find { it.id == eventId }?.let { scheduledQuests.remove(it) }
                unscheduledQuests.find { it.id == eventId }?.let { unscheduledQuests.remove(it) }
                removeQuestUseCase.execute(eventId)
                state.copy(
                    type = DayViewState.StateType.EVENT_REMOVED,
                    removedEventId = eventId,
                    scheduledQuests = scheduledQuests,
                    unscheduledQuests = unscheduledQuests
                )
            }

            is UndoRemoveEventIntent -> {
                val eventId = intent.eventId
                undoRemovedQuestUseCase.execute(eventId)
                state.copy(type = DayViewState.StateType.UNDO_REMOVED_EVENT, removedEventId = "")
            }
        }

    private fun savedQuestViewState(result: Result, state: DayViewState) =
        when (result) {
            is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
            else -> state.copy(type = EVENT_UPDATED)
        }

    private fun createUnscheduledViewModels(schedule: Schedule): List<DayViewController.UnscheduledQuestViewModel> =
        schedule.unscheduled.map {
            DayViewController.UnscheduledQuestViewModel(
                it.id,
                it.name,
                it.duration,
                AndroidColor.valueOf(it.color.name)
            )
        }

    private fun createScheduledViewModels(schedule: Schedule): List<DayViewController.QuestViewModel> =
        schedule.scheduled.map {
            val color = AndroidColor.valueOf(it.color.name)

            DayViewController.QuestViewModel(
                it.id,
                it.name,
                it.duration,
                it.startTime!!.toMinuteOfDay(),
                it.startTime.toString(),
                it.endTime.toString(),
                color,
                color.color900,
                it.isCompleted
            )
        }
}