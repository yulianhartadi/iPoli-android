package io.ipoli.android.quest.calendar.dayview

import io.ipoli.android.common.data.ColorName
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.view.Color
import io.ipoli.android.quest.calendar.dayview.view.DayView
import io.ipoli.android.quest.calendar.dayview.view.DayViewController
import io.ipoli.android.quest.calendar.dayview.view.DayViewState
import io.ipoli.android.quest.calendar.dayview.view.DayViewState.StateType.*
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.ipoli.android.quest.usecase.Result
import io.ipoli.android.quest.usecase.SaveQuestUseCase
import io.ipoli.android.quest.usecase.Schedule
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */
class DayViewPresenter(private val loadScheduleUseCase: LoadScheduleForDateUseCase,
                       private val saveQuestUseCase: SaveQuestUseCase) :
    BaseMviPresenter<DayView, DayViewState>(DayViewState.Loading) {
    override fun bindIntents(): List<Observable<DayViewState>> =
        listOf(
            bindLoadScheduleIntent(),
            bindAddEventIntent(),
            bindEditEventIntent(),
            bindEditUnscheduledEventIntent()
//            bindDeleteEventIntent()
        )


    private fun bindAddEventIntent() =
        on {
            it.addEventIntent()
                .map { event ->
                    val colorName = ColorName.valueOf(event.backgroundColor.name)
                    val q = Quest(event.name, LocalDate.now(), colorName = colorName)
                    q.startMinute = event.startMinute
                    q.setDuration(event.duration)
                    q
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
                val colorName = ColorName.valueOf(it.backgroundColor.name)
                val q = Quest(it.name, LocalDate.now(), colorName = colorName)
                q.id = it.id
                q.startMinute = it.startMinute
                q.setDuration(it.duration)
                q
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
                val colorName = ColorName.valueOf(it.backgroundColor.name)
                val q = Quest(it.name, LocalDate.now(), colorName = colorName)
                q.id = it.id
                q.setDuration(it.duration)
                q
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
                Color.valueOf(it.colorName!!)
            )
        }

    private fun createScheduledViewModels(schedule: Schedule): List<DayViewController.QuestViewModel> =
        schedule.scheduled.map {
            val endTime = Time.of(it.startMinute!! + it.actualDuration)
            val color = Color.valueOf(it.colorName!!)
            DayViewController.QuestViewModel(
                it.id,
                it.name,
                it.actualDuration,
                it.startMinute!!,
                it.startTime.toString(),
                endTime.toString(),
                color,
                color.color900,
                it.isCompleted
            )
        }
}