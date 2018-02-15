package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesUntil
import mypoli.android.common.datetime.isBetween
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/14/2018.
 */
class FindQuestsForRepeatingQuest(private val questRepository: QuestRepository) :
    UseCase<FindQuestsForRepeatingQuest.Params, List<Quest>> {

    override fun execute(parameters: Params): List<Quest> {
        val rq = parameters.repeatingQuest
        val start = if (parameters.start.isBefore(rq.start)) rq.start else parameters.start
        val end = if (rq.end != null && rq.end.isBefore(parameters.end)) rq.end else parameters.end

        require(end.isAfter(start))

        val repeatingPattern = rq.repeatingPattern
        val scheduleDates =
            when (repeatingPattern) {
                is RepeatingPattern.Yearly -> yearlyDatesToScheduleInPeriod(
                    repeatingPattern,
                    start,
                    end
                )

                RepeatingPattern.Daily -> start.datesUntil(end).toSet()

                is RepeatingPattern.Weekly -> weeklyDatesToScheduleInPeriod(
                    repeatingPattern,
                    start,
                    end
                )
            }


        if (scheduleDates.isEmpty()) {
            return listOf()
        }

        val scheduledQuests = questRepository.findForRepeatingQuestBetween(rq.id, start, end)
        val (removed, existing) = scheduledQuests.partition { it.isRemoved }
        val schedule = existing.associateBy({ it.originalScheduledDate }, { it })
        val removedDates = removed.map { it.originalScheduledDate }
        val resultDates = scheduleDates.minus(removedDates)

        return resultDates.map {

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

    private fun weeklyDatesToScheduleInPeriod(
        repeatingPattern: RepeatingPattern.Weekly,
        start: LocalDate,
        end: LocalDate
    ): List<LocalDate> {

        var date = start
        val dates = mutableListOf<LocalDate>()
        while (date.isBefore(end.plusDays(1))) {
            if (date.dayOfWeek in repeatingPattern.daysOfWeek) {
                dates.add(date)
            }
            date = date.plusDays(1)
        }
        return dates

    }

    private fun yearlyDatesToScheduleInPeriod(
        repeatingPattern: RepeatingPattern.Yearly,
        start: LocalDate,
        end: LocalDate
    ): List<LocalDate> {
        if (start.year == end.year) {
            val date = LocalDate.of(
                start.year,
                repeatingPattern.month,
                repeatingPattern.dayOfMonth
            )
            return listOf(date).filter { it.isBetween(start, end) }
        }

        var startPeriodDate = start
        val dates = mutableListOf<LocalDate>()
        while (startPeriodDate <= end) {
            val lastDayOfYear = LocalDate.of(startPeriodDate.year, 12, 31)
            val date = LocalDate.of(
                startPeriodDate.year,
                repeatingPattern.month,
                repeatingPattern.dayOfMonth
            )
            val endPeriodDate = if (end.isBefore(lastDayOfYear)) end else lastDayOfYear
            if (date.isBetween(startPeriodDate, endPeriodDate)) {
                dates.add(date)
            }
            startPeriodDate = LocalDate.of(startPeriodDate.year + 1, 1, 1)
        }
        return dates

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