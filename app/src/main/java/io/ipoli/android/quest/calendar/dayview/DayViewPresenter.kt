package io.ipoli.android.quest.calendar.dayview

import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.view.Color
import io.ipoli.android.quest.calendar.dayview.view.DayView
import io.ipoli.android.quest.calendar.dayview.view.DayViewController
import io.ipoli.android.quest.calendar.dayview.view.DayViewState
import io.ipoli.android.quest.calendar.dayview.view.DayViewState.StateType.*
import io.ipoli.android.quest.data.Quest
import io.ipoli.android.quest.usecase.AddQuestUseCase
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.ipoli.android.quest.usecase.Result
import io.ipoli.android.quest.usecase.Schedule
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */
class DayViewPresenter(private val loadScheduleUseCase: LoadScheduleForDateUseCase,
                       private val addQuestUseCase: AddQuestUseCase) :
    BaseMviPresenter<DayView, DayViewState>(DayViewState.Loading) {
    override fun bindIntents(): List<Observable<DayViewState>> =
        listOf(
            bindLoadScheduleIntent(),
            bindAddEventIntent(),
            bindEditEventIntent(),
            bindEditUnscheduledEventIntent()
        )

    private fun bindAddEventIntent() =
        on {
            it.addEventIntent()
                .map { event ->
                    val q = Quest(event.name, LocalDate.now())
                    q.startMinute = event.startMinute
                    q.setDuration(event.duration)
                    q
                }
        }
            .execute(addQuestUseCase)
            .map { (state, result) ->
                when (result) {
                    is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
                    else -> state.copy(type = EVENT_UPDATED)
                }
            }

    private fun bindEditEventIntent() =
        on {
            it.editEventIntent().map {
                val q = Quest(it.event.name, LocalDate.now())
                q.id = it.eventId
                q.startMinute = it.event.startMinute
                q.setDuration(it.event.duration)
                q
            }
        }.switchMap { (state, params) ->
            addQuestUseCase.execute(params).map { result ->

//                transformation {
//                    mapIntent: { intentParams ->
//                        val q = Quest(it.event.name, LocalDate.now())
//                        q.id = it.eventId
//                        q.startMinute = it.event.startMinute
//                        q.setDuration(it.event.duration)
//                        q
//                    }
//                    useCase: addQuestUseCase
//                    reducer: { (state, params) ->
//                        when (result) {
//                            is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
//                            else -> state.copy(type = EVENT_UPDATED)
//                        }
//                    }
//                }

                when (result) {
                    is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
                    else -> state.copy(type = EVENT_UPDATED)
                }
            }.compose(runOnIO())
        }

    private fun bindEditUnscheduledEventIntent() =
        on {
            it.editUnscheduledEventIntent().map {
                val q = Quest(it.event.name, LocalDate.now())
                q.id = it.eventId
                q.setDuration(it.event.duration)
                q
            }
        }.executeAndReduce(
            addQuestUseCase,
            { state, result ->
                when (result) {
                    is Result.Invalid -> state.copy(type = EVENT_VALIDATION_ERROR)
                    else -> state.copy(type = EVENT_UPDATED)
                }
            })

    private fun bindLoadScheduleIntent() =
        on { it.loadScheduleIntent() }
            .execute(loadScheduleUseCase)
            .map { (state, schedule) ->
                state.copy(
                    type = SCHEDULE_LOADED,
                    scheduledQuests = createScheduledViewModels(schedule),
                    unscheduledQuests = createUnscheduledViewModels(schedule)
                )
            }

    private fun createUnscheduledViewModels(schedule: Schedule): List<DayViewController.UnscheduledQuestViewModel> =
        schedule.unscheduled.map {
            DayViewController.UnscheduledQuestViewModel(
                it.id,
                it.name,
                it.actualDuration,
                Color.ORANGE
            )
        }

    private fun createScheduledViewModels(schedule: Schedule): List<DayViewController.QuestViewModel> =
        schedule.scheduled.map {
            val endTime = Time.of(it.startMinute!! + it.actualDuration)
            DayViewController.QuestViewModel(
                it.id,
                it.name,
                it.actualDuration,
                it.startMinute!!,
                it.startTime.toString(),
                endTime.toString(),
                Color.GREEN,
                R.color.md_green_900,
                it.isCompleted
            )
        }

}