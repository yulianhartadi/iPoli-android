package io.ipoli.android.quest.calendar.dayview

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.quest.Category
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.QuestSchedule
import io.ipoli.android.quest.calendar.dayview.view.*
import io.ipoli.android.quest.calendar.dayview.view.DayViewState.StateType.*
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.ipoli.android.quest.usecase.Result
import io.ipoli.android.quest.usecase.SaveQuestUseCase
import io.ipoli.android.quest.usecase.Schedule
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.consumeEach
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
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<DayViewState>, DayViewState, DayViewIntent>(coroutineContext) {

    override fun reduceState(intent: DayViewIntent, state: DayViewState) =
        when (intent) {

            is ScheduleLoadedIntent -> {
                val schedule = intent.schedule
                state.copy(
                    type = SCHEDULE_LOADED,
                    scheduledQuests = createScheduledViewModels(schedule),
                    unscheduledQuests = createUnscheduledViewModels(schedule)
                )
            }

            is AddEventIntent -> {
                val event = intent.event
                val colorName = Color.valueOf(event.backgroundColor.name)
                val quest = Quest(
                    name = event.name,
                    color = colorName,
                    category = Category("WELLNESS", Color.GREEN),
                    plannedSchedule = QuestSchedule(
                        date = LocalDate.now(),
                        time = null,
                        duration = event.duration
                    )
                )
                val result = saveQuestUseCase.execute(quest)
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
                    plannedSchedule = QuestSchedule(
                        date = LocalDate.now(),
                        time = Time.of(event.startMinute),
                        duration = event.duration
                    )
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
                    plannedSchedule = QuestSchedule(
                        date = LocalDate.now(),
                        time = null,
                        duration = event.duration
                    )
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
                state.copy(scheduledQuests = scheduledQuests, unscheduledQuests = unscheduledQuests)
            }
        }

    private fun savedQuestViewState(result: Result, state: DayViewState) =
        when (result) {
            is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
            else -> state.copy(type = EVENT_UPDATED)
        }

    override suspend fun loadStreamingData(actor: ActorJob<DayViewIntent>, initialState: DayViewState) {
        Timber.d("LoadStreamingData")
        loadScheduleUseCase.execute(initialState.scheduledDate).consumeEach {
            actor.send(ScheduleLoadedIntent(it))
        }
    }


    private fun createUnscheduledViewModels(schedule: Schedule): List<DayViewController.UnscheduledQuestViewModel> =
        schedule.unscheduled.map {
            DayViewController.UnscheduledQuestViewModel(
                it.id,
                it.name,
                it.actualDuration,
                AndroidColor.valueOf(it.color.name)
            )
        }

    private fun createScheduledViewModels(schedule: Schedule): List<DayViewController.QuestViewModel> =
        schedule.scheduled.map {
            val color = AndroidColor.valueOf(it.color.name)

            var startTime = it.plannedSchedule.time
            if (it.actualSchedule != null && it.actualSchedule.time != null && startTime == it.originalStartTime) {
                startTime = it.actualSchedule.time
            }

            DayViewController.QuestViewModel(
                it.id,
                it.name,
                it.actualDuration,
                startTime!!.toMinuteOfDay(),
                startTime.toString(),
                it.endTime.toString(),
                color,
                color.color900,
                it.isCompleted
            )
        }

    private val Quest.actualDuration: Int
        get() {
            var duration = plannedSchedule.duration
            if (actualSchedule != null) {
                duration = actualSchedule.duration
            }
            return duration
        }
}