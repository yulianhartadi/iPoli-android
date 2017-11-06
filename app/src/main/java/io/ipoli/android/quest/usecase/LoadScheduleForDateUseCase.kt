package io.ipoli.android.quest.usecase

import io.ipoli.android.common.StreamingUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.produce
import org.threeten.bp.LocalDate
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */
data class Schedule(val date: LocalDate, val scheduled: List<Quest>, val unscheduled: List<Quest>)

class LoadScheduleForDateUseCase(private val questRepository: QuestRepository, coroutineContext: CoroutineContext) : StreamingUseCase<LocalDate, Schedule>(coroutineContext) {
    override fun execute(parameters: LocalDate) =
        createSchedule(parameters, questRepository.listenForDate(parameters))

    private fun createSchedule(date : LocalDate, channel: ReceiveChannel<List<Quest>>) = produce(coroutineContext) {
        channel.consumeEach { quests ->
            val (scheduled, unscheduled) = quests
                .partition { it.isScheduled }
            send(Schedule(date, scheduled, unscheduled.sortedBy { it.isCompleted }))
        }
    }
}