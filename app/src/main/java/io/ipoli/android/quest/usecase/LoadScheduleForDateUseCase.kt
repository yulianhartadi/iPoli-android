package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.datesBetween
import io.ipoli.android.event.Event
import io.ipoli.android.quest.Quest
import org.threeten.bp.LocalDate
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/27/17.
 */
data class Schedule(
    val date: LocalDate,
    val scheduledQuests: List<Quest>,
    val unscheduledQuests: List<Quest>,
    val events: List<Event>
)

class LoadScheduleForDateUseCase :
    UseCase<LoadScheduleForDateUseCase.Params, Map<LocalDate, Schedule>> {

    override fun execute(parameters: Params): Map<LocalDate, Schedule> {
        val scheduleDates = parameters.startDate.datesBetween(parameters.endDate)
        
        val questData = scheduleDates.map {
            it to Pair<MutableList<Quest>, MutableList<Quest>>(mutableListOf(), mutableListOf())
        }.toMap().toMutableMap()

        for (q in parameters.quests) {

            val key = q.scheduledDate

            if (q.isScheduled) {
                questData[key]!!.first.add(q)
            } else {
                questData[key]!!.second.add(q)
            }
        }

        val eventData = scheduleDates.map {
            it to mutableListOf<Event>()
        }.toMap().toMutableMap()


        for (e in parameters.events) {
            eventData[e.startDate]!!.add(e)
        }

        return questData.map {
            it.key to Schedule(it.key, it.value.first, it.value.second, eventData[it.key]!!)
        }.toMap()
    }

    data class Params(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val quests: List<Quest>,
        val events: List<Event>
    )
}