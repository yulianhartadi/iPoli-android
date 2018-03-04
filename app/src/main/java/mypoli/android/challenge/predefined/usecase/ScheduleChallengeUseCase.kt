package mypoli.android.challenge.usecase

import mypoli.android.challenge.predefined.entity.PredefinedChallengeData
import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesBetween
import mypoli.android.common.datetime.daysUntil
import mypoli.android.quest.Category
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class ScheduleChallengeUseCase(private val questRepository: QuestRepository) :
    UseCase<ScheduleChallengeUseCase.Params, List<Quest>> {

    private lateinit var startDate: LocalDate
    private lateinit var endDate: LocalDate

    override fun execute(parameters: Params): List<Quest> {
        val challenge = parameters.challenge

        require(challenge.quests.isNotEmpty(), { "Challenge must contain quests" })

        startDate = parameters.startDate
        endDate = startDate.plusDays((challenge.durationDays - 1).toLong())
        val randomSeed = parameters.randomSeed

        val quests = challenge.quests.map { q ->
            when (q) {
                is PredefinedChallengeData.Quest.Repeating -> {

                    startDate
                        .datesBetween(endDate)
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

                is PredefinedChallengeData.Quest.OneTime -> {

                    val scheduledDate = if (q.startAtDay != null) {
                        val startDay = startDate.plusDays((q.startAtDay - 1).toLong())
                        if (startDay.isAfter(endDate)) {
                            chooseRandomScheduledDate(randomSeed)
                        } else {
                            startDay
                        }
                    } else if (q.preferredDayOfWeek != null) {
                        val preferredDate =
                            startDate.with(TemporalAdjusters.nextOrSame(q.preferredDayOfWeek))
                        if (preferredDate.isAfter(endDate)) {
                            chooseRandomScheduledDate(randomSeed)
                        } else {
                            preferredDate
                        }
                    } else {
                        chooseRandomScheduledDate(randomSeed)
                    }

                    listOf(createFromOneTime(q, challenge, scheduledDate))
                }
            }
        }.flatten()

        return quests.map { questRepository.save(it) }
    }

    private fun chooseRandomScheduledDate(randomSeed: Long?): LocalDate {
        val dates = startDate.datesBetween(endDate)

        val random = randomSeed?.let {
            Random(it)
        } ?: Random()

        return dates[random.nextInt(dates.size)]
    }

    private fun isAfterStartDay(date: LocalDate, startAtDay: Int): Boolean {
        val s = startDate.daysUntil(date) + 1
        return s + 1 >= startAtDay
    }

    private fun createFromOneTime(
        it: PredefinedChallengeData.Quest.OneTime,
        challenge: PredefinedChallengeData,
        scheduledDate: LocalDate
    ) =
        Quest(
            name = it.name,
            color = it.color,
            icon = it.icon,
            startTime = it.startTime,
            category = Category(challenge.category.name, it.color),
            duration = it.duration,
            scheduledDate = scheduledDate
        )

    private fun createFromRepeating(
        it: PredefinedChallengeData.Quest.Repeating,
        challenge: PredefinedChallengeData,
        scheduledDate: LocalDate
    ) =
        Quest(
            name = it.name,
            color = it.color,
            icon = it.icon,
            startTime = it.startTime,
            category = Category(challenge.category.name, it.color),
            duration = it.duration,
            scheduledDate = scheduledDate
        )


    data class Params(
        val challenge: PredefinedChallengeData,
        val startDate: LocalDate = LocalDate.now(),
        val randomSeed: Long? = null
    )
}