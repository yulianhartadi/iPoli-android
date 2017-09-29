package io.ipoli.android.quest.calendar

import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.ui.Color
import io.ipoli.android.quest.usecase.LoadScheduleForDateUseCase
import io.reactivex.Observable

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
        intent { it.loadScheduleIntent() }
            .switchMap {date -> loadScheduleUseCase.execute(date) }
            .map { schedule ->
                val scheduled = mutableListOf<DayViewController.QuestViewModel>()
                val unscheduled = mutableListOf<DayViewController.UnscheduledQuestViewModel>()
                schedule.scheduled.forEach {
                    val endTime = Time.of(it.startMinute!! + it.actualDuration)
                    scheduled.add(DayViewController.QuestViewModel(
                        it.name,
                        it.actualDuration,
                        it.startMinute!!,
                        it.startTime.toString(),
                        endTime.toString(),
                        Color.GREEN,
                        it.categoryType.color800,
                        it.isCompleted
                    ))
                }

                schedule.unscheduled.forEach {
                    unscheduled.add(DayViewController.UnscheduledQuestViewModel(
                        it.name,
                        it.actualDuration,
                        Color.ORANGE
                    ))
                }

                DayViewState.ScheduleLoaded(scheduled, unscheduled) }
            .cast(DayViewState::class.java)

}