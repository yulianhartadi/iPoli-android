package io.ipoli.android.quest.calendar

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.ui.Color
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.ipoli.android.quest.usecase.Schedule
import io.reactivex.Observable


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */
class DayViewPresenter(private val loadScheduleUseCase: LoadScheduleForDateUseCase) :
    BaseMviPresenter<DayView, DayViewState>(DayViewState.Loading) {
    override fun bindIntents(): List<Observable<DayViewState>> {
        return listOf(
            bindLoadScheduleIntent() //,
//            bindAddQuestIntent()
        )
    }

//    private fun bindAddQuestIntent(): Observable<DayViewState> {
//        on { it.addEventIntent() }
//            .switchMap { event ->
//
//            }
//    }

    private fun bindLoadScheduleIntent(): Observable<DayViewState> =
        on { it.loadScheduleIntent() }
            .execute(loadScheduleUseCase)
            .map { schedule ->
                DayViewState.ScheduleLoaded(createScheduledViewModels(schedule), createUnscheduledViewModels(schedule))
            }


    private fun createUnscheduledViewModels(schedule: Schedule): List<DayViewController.UnscheduledQuestViewModel> =
        schedule.unscheduled.map {
            DayViewController.UnscheduledQuestViewModel(
                it.name,
                it.actualDuration,
                Color.ORANGE
            )
        }

    private fun createScheduledViewModels(schedule: Schedule): List<DayViewController.QuestViewModel> =
        schedule.scheduled.map {
            val endTime = Time.of(it.startMinute!! + it.actualDuration)
            DayViewController.QuestViewModel(
                it.name,
                it.actualDuration,
                it.startMinute!!,
                it.startTime.toString(),
                endTime.toString(),
                Color.GREEN,
                it.categoryType.color800,
                it.isCompleted
            )
        }

}