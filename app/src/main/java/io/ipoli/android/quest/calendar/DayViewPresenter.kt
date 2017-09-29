package io.ipoli.android.quest.calendar

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.ui.Color
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.ipoli.android.quest.usecase.Schedule
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */
class DayViewPresenter(private val loadScheduleUseCase: LoadScheduleForDateUseCase) : BaseMviPresenter<DayView, DayViewState>(DayViewState.Loading) {
    override fun bindIntents(): List<Observable<DayViewState>> {
        return listOf(
            bindLoadScheduleIntent()
        )
    }

    private fun bindLoadScheduleIntent(): Observable<DayViewState> =
        intent {
            it.loadScheduleIntent()
        }.switchMap { date ->
            loadScheduleUseCase.execute(date)
                .map { schedule ->
                    DayViewState.ScheduleLoaded(createScheduledViewModels(schedule), createUnscheduledViewModels(schedule))
                }
                .cast(DayViewState::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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