package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesUntil
import mypoli.android.common.datetime.isBetween
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/14/2018.
 */
class FindQuestsForRepeatingQuest(private val questRepository: QuestRepository) :
    UseCase<FindQuestsForRepeatingQuest.Params, List<Quest>> {

    override fun execute(parameters: Params): List<Quest> {
        val rq = parameters.repeatingQuest

        require(parameters.end.isAfter(parameters.start))

        if (parameters.end.isBefore(rq.start)) {
            return listOf()
        }

        if (rq.end != null && parameters.start.isAfter(rq.end)) {
            return listOf()
        }

        val start = if (parameters.start.isBefore(rq.start)) rq.start else parameters.start
        val end = if (rq.end != null && rq.end.isBefore(parameters.end)) rq.end else parameters.end

        val scheduleDates = when (rq.repeatingPattern) {

            RepeatingPattern.Daily -> start.datesUntil(end).toSet()

            is RepeatingPattern.Weekly -> weeklyDatesToScheduleInPeriod(
                rq.repeatingPattern,
                start,
                end
            )

            is RepeatingPattern.Monthly -> monthlyDatesToScheduleInPeriod(
                rq.repeatingPattern,
                start,
                end
            )

            is RepeatingPattern.Yearly -> yearlyDatesToScheduleInPeriod(
                rq.repeatingPattern,
                start,
                end
            )

            is RepeatingPattern.Flexible.Weekly -> {
                flexibleWeekly(rq.repeatingPattern, start, end, parameters.lastDayOfWeek)
            }

        }


        if (scheduleDates.isEmpty()) {
            return listOf()
        }

        val scheduledQuests = questRepository.findForRepeatingQuestBetween(rq.id, start, end)
        val (removed, existing) = scheduledQuests.partition { it.isRemoved }
        val schedule = existing.associateBy({ it.originalScheduledDate }, { it })
        val removedDates = removed.map { it.originalScheduledDate }
        val resultDates = scheduleDates - removedDates

        return resultDates.map {

            if (schedule.containsKey(it)) {
                schedule[it]!!
            } else {
                createQuest(rq, it)
            }
        }
    }

    private fun flexibleWeekly(
        pattern: RepeatingPattern.Flexible.Weekly,
        start: LocalDate,
        end: LocalDate,
        lastDayOfWeek: DayOfWeek
    ): List<LocalDate> {

        val preferredDays = pattern.preferredDays
        val timesPerWeek = pattern.timesPerWeek
        require(timesPerWeek != preferredDays.size)
        require(timesPerWeek >= 1)
        require(timesPerWeek <= 7)

        val periods = findWeeklyPeriods(start, end, lastDayOfWeek)

        val result = mutableListOf<LocalDate>()


        val daysOfWeek = DayOfWeek.values().toList()
        for (p in periods) {
            if (preferredDays.isNotEmpty()) {
                val weekDays = preferredDays.shuffled().take(timesPerWeek)
                val remainingWeekDays =
                    (daysOfWeek - weekDays).shuffled().take(timesPerWeek - weekDays.size)

                val periodStart = p.start
                val scheduledWeekDays = weekDays + remainingWeekDays
                scheduledWeekDays.forEach {
                    val d = periodStart.with(it)
                    if (d.isBetween(periodStart, p.end)) {
                        result.add(d)
                    }
                }

            } else {
                val weekDays = daysOfWeek.shuffled().take(timesPerWeek)
                val periodStart = p.start
                weekDays.forEach {
                    val d = periodStart.with(it)
                    if (d.isBetween(periodStart, p.end)) {
                        result.add(d)
                    }
                }
            }
        }

        return result
    }

    data class Period(val start: LocalDate, val end: LocalDate)

    private fun findWeeklyPeriods(
        start: LocalDate,
        end: LocalDate,
        lastDayOfWeek: DayOfWeek
    ): List<Period> {

        val periods = mutableListOf<Period>()

        var periodStart = start
        val dayAfterEnd = end.plusDays(1)
        while (periodStart.isBefore(dayAfterEnd)) {
            val periodEnd = periodStart.with(lastDayOfWeek)
            periods.add(Period(periodStart, if (periodEnd.isAfter(end)) end else periodEnd))
            periodStart = periodEnd.plusDays(1)
        }

        return periods
    }

    private fun createQuest(
        rq: RepeatingQuest,
        it: LocalDate
    ): Quest {
        return Quest(
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

    private fun monthlyDatesToScheduleInPeriod(
        repeatingPattern: RepeatingPattern.Monthly,
        start: LocalDate,
        end: LocalDate
    ): List<LocalDate> {

        var date = start
        val dates = mutableListOf<LocalDate>()
        while (date.isBefore(end.plusDays(1))) {
            if (date.dayOfMonth in repeatingPattern.daysOfMonth) {
                dates.add(date)
            }
            date = date.plusDays(1)
        }
        return dates

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
        val end: LocalDate,
        val firstDayOfWeek: DayOfWeek,
        val lastDayOfWeek: DayOfWeek
    )
}