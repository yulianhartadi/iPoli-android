package mypoli.android.challenge.usecase

import mypoli.android.challenge.data.Challenge
import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesUntil
import mypoli.android.common.datetime.daysUntil
import mypoli.android.quest.Category
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class ScheduleChallengeUseCase(private val questRepository: QuestRepository) : UseCase<ScheduleChallengeUseCase.Params, List<Quest>> {

    private lateinit var startDate: LocalDate
    private lateinit var endDate: LocalDate

    override fun execute(parameters: Params): List<Quest> {
        val challenge = parameters.challenge

        require(challenge.quests.isNotEmpty(), { "Challenge must contain quests" })

        startDate = parameters.startDate
        endDate = startDate.plusDays((challenge.durationDays - 1).toLong())

        val quests = challenge.quests.map { q ->
            when (q) {
                is Challenge.Quest.Repeating -> {

                    startDate
                        .datesUntil(endDate)
                        .filter { date ->
                            val shouldScheduleForDay = q.weekDays.contains(date.dayOfWeek)
                            val isAfterStartDay = q.startAtDay?.let {
                                isAfterStartDay(date, it)
                            } ?: true
                            shouldScheduleForDay && isAfterStartDay
                        }
                        .map {
                            createFromRepeating(q, challenge, it)
                        }
                }

                is Challenge.Quest.OneTime -> {
                    val scheduledDate = q.preferredDayOfWeek?.let {
                        val scheduleDate = startDate.with(it)
                        if (scheduleDate.isAfter(endDate)) {
                            chooseRandomScheduledDate(q)
                        } else {
                            q.startAtDay?.let {
                                findDateAfterStartDay(scheduleDate, it)
                            } ?: scheduleDate
                        }

                    } ?: chooseRandomScheduledDate(q)
                    listOf(createFromOneTime(q, challenge, scheduledDate))
                }
            }
        }.flatten()

        return quests.map { questRepository.save(it) }
    }

    private fun findDateAfterStartDay(scheduleDate: LocalDate, startAtDay: Int) =
        if (isAfterStartDay(scheduleDate, startAtDay)) {
            scheduleDate
        } else {
            findDateToSchedule(scheduleDate, startAtDay)
        }

    private fun findDateToSchedule(scheduledDate: LocalDate, startAtDay: Int): LocalDate {
        var d = scheduledDate.plusWeeks(1)
        while (!d.isAfter(endDate)) {
            if (isAfterStartDay(d, startAtDay)) {
                return d
            }
            d = d.plusWeeks(1)
        }
        return endDate
    }

    /**
     * @TODO implement this
     */
    private fun chooseRandomScheduledDate(quest: Challenge.Quest) = endDate

    private fun isAfterStartDay(date: LocalDate, startAtDay: Int): Boolean {
        val s = startDate.daysUntil(date) + 1
        return s + 1 >= startAtDay
    }

    private fun createFromOneTime(it: Challenge.Quest.OneTime, challenge: Challenge, scheduledDate: LocalDate) =
        Quest(
            name = it.name,
            color = it.color,
            icon = it.icon,
            startTime = it.startTime,
            category = Category(challenge.category.name, it.color),
            duration = it.duration,
            scheduledDate = scheduledDate
        )

    private fun createFromRepeating(it: Challenge.Quest.Repeating, challenge: Challenge, scheduledDate: LocalDate) =
        Quest(
            name = it.name,
            color = it.color,
            icon = it.icon,
            startTime = it.startTime,
            category = Category(challenge.category.name, it.color),
            duration = it.duration,
            scheduledDate = scheduledDate
        )


    data class Params(val challenge: Challenge, val startDate: LocalDate = LocalDate.now())
}