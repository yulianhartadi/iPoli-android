package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesBetween
import mypoli.android.common.datetime.isBetween
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingQuest
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/3/18.
 */
class CreatePlaceholderQuestsForRepeatingQuestsUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository
) : UseCase<CreatePlaceholderQuestsForRepeatingQuestsUseCase.Params, List<Quest>> {


    override fun execute(parameters: Params): List<Quest> {
        val start = parameters.startDate
        val end = parameters.endDate
        val currentDate = parameters.currentDate

        if (currentDate.isAfter(end)) {
            return listOf()
        }

        val rqs = repeatingQuestRepository.findAllActive(currentDate)

        return rqs.filter { it.isFixed }.map {
            val scheduleDates = when (it.repeatingPattern) {

                is RepeatingPattern.Daily -> start.datesBetween(end).toSet()


                is RepeatingPattern.Weekly ->
                    weeklyDatesToScheduleInPeriod(
                        it.repeatingPattern,
                        start,
                        end
                    )

                is RepeatingPattern.Monthly ->
                    monthlyDatesToScheduleInPeriod(
                        it.repeatingPattern,
                        start,
                        end
                    )


                is RepeatingPattern.Yearly ->
                    yearlyDatesToScheduleInPeriod(
                        it.repeatingPattern,
                        start,
                        end
                    )

                else -> throw IllegalArgumentException("Cannot create placeholders for $it")
            }

            if (scheduleDates.isNotEmpty()) {
                val scheduledQuests =
                    questRepository.findScheduledForRepeatingQuestBetween(it.id, start, end)

                val (removed, existing) = scheduledQuests.partition { it.isRemoved }
                val scheduledDateToQuest =
                    existing.associateBy({ it.originalScheduledDate }, { it })
                val removedDates = removed.map { it.originalScheduledDate }
                val resultDates = scheduleDates - removedDates

                resultDates.filter { !scheduledDateToQuest.containsKey(it) }.map { date ->
                    createQuest(it, date)
                }
            } else {
                listOf()
            }
        }.flatten()
    }

    private fun createQuest(
        rq: RepeatingQuest,
        scheduleDate: LocalDate
    ) =
        Quest(
            name = rq.name,
            color = rq.color,
            icon = rq.icon,
            category = rq.category,
            startTime = rq.startTime,
            duration = rq.duration,
            scheduledDate = scheduleDate,
            reminder = rq.reminder?.copy(
                remindDate = scheduleDate
            ),
            repeatingQuestId = rq.id
        )

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


    data class Params(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val currentDate: LocalDate = LocalDate.now()
    )
}