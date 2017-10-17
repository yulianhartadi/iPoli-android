package io.ipoli.android.quest.usecase

import io.ipoli.android.common.StreamingUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/27/17.
 */
data class Schedule(val scheduled: List<Quest>, val unscheduled: List<Quest>)

class LoadScheduleForDateUseCase(private val questRepository: QuestRepository) : StreamingUseCase<LocalDate, Schedule>() {
    override fun execute(parameters: LocalDate): Channel<Schedule> {
        val channel = Channel<Schedule>()
        launch {
            val repoChannel = questRepository.listenForDate(parameters)
            repoChannel
                .consumeEach { quests ->
                    val (scheduled, unscheduled) = quests
                        .partition { it.isScheduled }
                    channel.send(Schedule(scheduled, unscheduled))
                }
        }
        return channel
    }
}