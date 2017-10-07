package io.ipoli.android.quest.calendar.dayview

import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.view.Color
import io.ipoli.android.quest.calendar.dayview.view.*
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
    BaseMviPresenter<DayView, DayViewState, DayViewStateChange>(DayViewState.Loading) {
    override fun bindIntents(): List<Observable<DayViewStateChange>> {
        return listOf(
            bindLoadScheduleIntent(),
            bindAddEventIntent(),
            bindEditEventIntent(),
            bindEditUnscheduledEventIntent()
        )
    }

    private fun bindAddEventIntent(): Observable<DayViewStateChange> =
        on { it.addEventIntent() }
            .map { (state, event) ->
                val q = Quest(event.name, LocalDate.now())
                q.startMinute = event.startMinute
                q.setDuration(event.duration)
                Pair(state, q)
            }
            .execute(addQuestUseCase)
            .map { result ->
                when (result) {
                    is Result.Invalid -> EventValidationError
                    else -> EventUpdated
                }
            }

    private fun bindEditEventIntent(): Observable<DayViewStateChange> =
        on { it.editEventIntent() }
            .map { (state, editRequest) ->
                val q = Quest(editRequest.event.name, LocalDate.now())
                q.id = editRequest.eventId
                q.startMinute = editRequest.event.startMinute
                q.setDuration(editRequest.event.duration)
                Pair(state, q)
            }
            .execute(addQuestUseCase)
            .map { result ->
                when (result) {
                    is Result.Invalid -> EventValidationError
                    else -> EventUpdated
                }
            }

    private fun bindEditUnscheduledEventIntent(): Observable<DayViewStateChange> =
        on { it.editUnscheduledEventIntent() }
            .map { (state, editRequest) ->
                val q = Quest(editRequest.event.name, LocalDate.now())
                q.id = editRequest.eventId
                q.setDuration(editRequest.event.duration)
                Pair(state, q)
            }
            .execute(addQuestUseCase)
            .map { result ->
                when (result) {
                    is Result.Invalid -> EventValidationError
                    else -> EventUpdated
                }
            }

    private fun bindLoadScheduleIntent(): Observable<DayViewStateChange> =
        on { it.loadScheduleIntent() }
            .execute(loadScheduleUseCase)
            .map { schedule ->
                ScheduleLoaded(createScheduledViewModels(schedule), createUnscheduledViewModels(schedule))
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