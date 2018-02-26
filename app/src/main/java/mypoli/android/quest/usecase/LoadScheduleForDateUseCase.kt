package mypoli.android.quest.usecase

import mypoli.android.common.UseCase
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
        val data = mutableMapOf<LocalDate, Pair<MutableList<Quest>, MutableList<Quest>>>()

        for (q in parameters.quests) {
            if (!data.containsKey(q.scheduledDate)) {
                data[q.scheduledDate] = Pair(mutableListOf(), mutableListOf())
            }
            if (q.isScheduled) {
                data[q.scheduledDate]!!.first.add(q)
            } else {
                data[q.scheduledDate]!!.second.add(q)
            }
        }

        return data.entries.associate {
            it.key to Schedule(
                it.key,
                it.value.first,
                it.value.second
            )
        }
    }

    data class Params(val quests: List<Quest>)
}