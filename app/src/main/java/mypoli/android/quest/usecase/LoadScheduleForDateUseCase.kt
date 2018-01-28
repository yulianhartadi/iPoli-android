package mypoli.android.quest.usecase

import kotlinx.coroutines.experimental.channels.map
import mypoli.android.common.StreamingUseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/27/17.
 */
data class Schedule(val date: LocalDate, val scheduled: List<Quest>, val unscheduled: List<Quest>)

class LoadScheduleForDateUseCase(private val questRepository: QuestRepository) :
    StreamingUseCase<LocalDate, Schedule> {
    override fun execute(parameters: LocalDate) =
        questRepository.listenForScheduledAt(parameters).map {
            val (scheduled, unscheduled) = it.partition { it.isScheduled }
            Schedule(parameters, scheduled, unscheduled.sortedBy { it.isCompleted })
        }
}