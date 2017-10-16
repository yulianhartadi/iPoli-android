package io.ipoli.android.quest.usecase

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.quest.data.RealmQuest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.reactivex.Observable
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */
data class Schedule(val scheduled: List<RealmQuest>, val unscheduled: List<RealmQuest>)

class LoadScheduleForDateUseCase(private val questRepository: QuestRepository) : BaseRxUseCase<LocalDate, Schedule>() {
    override fun createObservable(parameters: LocalDate): Observable<Schedule> {
        return questRepository.listenForDate(parameters).map { quests ->
            val (scheduled, unscheduled) = quests
                .partition { it.isScheduled }
            Schedule(scheduled, unscheduled)
        }
    }
}