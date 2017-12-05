package io.ipoli.android.quest.usecase

import io.ipoli.android.common.StreamingUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import kotlinx.coroutines.experimental.channels.map
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */
data class Schedule(val date: LocalDate, val scheduled: List<Quest>, val unscheduled: List<Quest>)

class LoadScheduleForDateUseCase(private val questRepository: QuestRepository) : StreamingUseCase<LocalDate, Schedule> {
    override fun execute(parameters: LocalDate) =
        questRepository.listenForDate(parameters).map {
            val (scheduled, unscheduled) = it.partition { it.isScheduled }
            Schedule(parameters, scheduled, unscheduled.sortedBy { it.isCompleted })
        }
}