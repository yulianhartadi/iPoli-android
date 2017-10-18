package io.ipoli.android.quest.calendar.dayview

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.CoroutineMviPresenter
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
import io.reactivex.Observable
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */

class DayViewPres(intentChannel: ReceiveChannel<DayViewIntent>) : CoroutineMviPresenter<DayView, DayViewState>(intentChannel, DayViewState.Loading) {

    override fun reduceState(intent: DayViewIntent, state: DayViewState): DayViewState {
        when (intent) {
            is AddEventIntent -> {

            }

            is LoadScheduleIntent -> {

            }
        }
    }

}

class DayViewPresenter(private val loadScheduleUseCase: LoadScheduleForDateUseCase,
                       private val saveQuestUseCase: SaveQuestUseCase) :
    BaseMviPresenter<DayView, DayViewState>(DayViewState.Loading) {
    override fun bindIntents(): List<Observable<DayViewState>> =
        listOf(
            bindLoadScheduleIntent(),
            bindAddEventIntent(),
            bindEditEventIntent(),
            bindEditUnscheduledEventIntent(),
            bindRemoveEventIntent()
        )

    private fun bindRemoveEventIntent() =
        on {
            it.removeEventIntent()
        }.map { (state, eventId) ->
            val scheduledQuests = state.scheduledQuests.toMutableList()
            val unscheduledQuests = state.unscheduledQuests.toMutableList()
            scheduledQuests.find { it.id == eventId }?.let { scheduledQuests.remove(it) }
            unscheduledQuests.find { it.id == eventId }?.let { unscheduledQuests.remove(it) }
            state.copy(scheduledQuests = scheduledQuests, unscheduledQuests = unscheduledQuests)
        }

    private fun bindAddEventIntent() =
        on {
            it.addEventIntent()
                .map { event ->
                    val colorName = Color.valueOf(event.backgroundColor.name)
                    Quest(name = event.name, color = colorName, category = Category("WELLNESS", Color.GREEN),
                        plannedSchedule = QuestSchedule(LocalDate.now(), Time.of(event.startMinute), event.duration))
                }
        }.executeAndReduce(
            saveQuestUseCase,
            { state, result ->
                getSavedQuestViewState(result, state)
            }
        )

    private fun bindEditEventIntent() =
        on {
            it.editEventIntent().map {
                val colorName = Color.valueOf(it.backgroundColor.name)
                Quest(id = it.id, name = it.name, color = colorName, category = Category("WELLNESS", Color.GREEN),
                    plannedSchedule = QuestSchedule(date = LocalDate.now(), time = Time.of(it.startMinute), duration = it.duration))
            }
        }.executeAndReduce(
            saveQuestUseCase,
            { state, result ->
                getSavedQuestViewState(result, state)
            }
        )

//                transformation {
//                    mapIntent: { intentParams ->
//                        val q = Quest(it.event.name, LocalDate.now())
//                        q.id = it.eventId
//                        q.startMinute = it.event.startMinute
//                        q.setDuration(it.event.duration)
//                        q
//                    }
//                    useCase: saveQuestUseCase
//                    reducer: { (state, params) ->
//                        when (result) {
//                            is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
//                            else -> state.copy(type = EVENT_UPDATED)
//                        }
//                    }
//                }

    private fun bindEditUnscheduledEventIntent() =
        on {
            it.editUnscheduledEventIntent().map {
                val colorName = Color.valueOf(it.backgroundColor.name)
                Quest(id = it.id, name = it.name, color = colorName, category = Category("WELLNESS", Color.GREEN),
                    plannedSchedule = QuestSchedule(date = LocalDate.now(), duration = it.duration))
            }
        }.executeAndReduce(
            saveQuestUseCase,
            { state, result ->
                getSavedQuestViewState(result, state)
            }
        )

    private fun getSavedQuestViewState(result: Result, state: DayViewState): DayViewState {
        return when (result) {
            is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
            else -> state.copy(type = EVENT_UPDATED)
        }
    }

    private fun bindLoadScheduleIntent() =
        on {
            it.loadScheduleIntent()
        }.executeAndReduce(
            loadScheduleUseCase,
            { state, schedule ->
                state.copy(
                    type = SCHEDULE_LOADED,
                    scheduledQuests = createScheduledViewModels(schedule),
                    unscheduledQuests = createUnscheduledViewModels(schedule)
                )

            }
        )

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