package mypoli.android.quest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesBetween
import mypoli.android.quest.Quest
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/27/17.
 */
data class Schedule(val date: LocalDate, val scheduled: List<Quest>, val unscheduled: List<Quest>)

class LoadScheduleForDateUseCase :
    UseCase<LoadScheduleForDateUseCase.Params, Map<LocalDate, Schedule>> {

    override fun execute(parameters: Params): Map<LocalDate, Schedule> {

        val data = parameters.startDate.datesBetween(parameters.endDate).map {
            it to Pair<MutableList<Quest>, MutableList<Quest>>(mutableListOf(), mutableListOf())
        }.toMap().toMutableMap()

        for (q in parameters.quests) {

            val key = q.scheduledDate

            if (q.isScheduled) {
                data[key]!!.first.add(q)
            } else {
                data[key]!!.second.add(q)
            }
        }

        return data.map {
            it.key to Schedule(it.key, it.value.first, it.value.second)
        }.toMap()
    }

    data class Params(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val quests: List<Quest>
    )
}