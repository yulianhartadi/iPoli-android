package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesUntil
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/14/2018.
 */
class FindQuestsForRepeatingQuest(private val questRepository: QuestRepository) :
    UseCase<FindQuestsForRepeatingQuest.Params, List<Quest>> {

    override fun execute(parameters: Params): List<Quest> {

        val (rq, start, end) = parameters

        require(end.isAfter(start))

        val scheduledQuests = questRepository.findForRepeatingQuestBetween(rq.id, start, end)

        val (removed, existing) = scheduledQuests.partition { it.isRemoved }

        val schedule = existing.associateBy({ it.originalScheduledDate }, { it })

        val removedDates = removed.map { it.originalScheduledDate }

        val scheduleDates = start.datesUntil(end).toSet().minus(removedDates)

        return scheduleDates.map {

            if (schedule.containsKey(it)) {
                schedule[it]!!
            } else {

                Quest(
                    name = rq.name,
                    color = rq.color,
                    icon = rq.icon,
                    category = rq.category,
                    startTime = rq.startTime,
                    duration = rq.duration,
                    scheduledDate = it,
                    reminder = rq.reminder
                )
            }
        }
    }

    /**
     * @start inclusive
     * @end inclusive
     */
    data class Params(
        val repeatingQuest: RepeatingQuest,
        val start: LocalDate,
        val end: LocalDate
    )
}