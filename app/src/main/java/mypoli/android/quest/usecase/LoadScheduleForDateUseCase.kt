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
    UseCase<LoadScheduleForDateUseCase.Params, Schedule> {

    override fun execute(parameters: Params): Schedule {

        val data = Pair<MutableList<Quest>, MutableList<Quest>>(mutableListOf(), mutableListOf())

        for (q in parameters.quests) {

            if (q.isScheduled) {
                data.first.add(q)
            } else {
                data.second.add(q)
            }
        }

        return Schedule(parameters.date, data.first, data.second)
    }

    data class Params(
        val date: LocalDate,
        val quests: List<Quest>
    )
}