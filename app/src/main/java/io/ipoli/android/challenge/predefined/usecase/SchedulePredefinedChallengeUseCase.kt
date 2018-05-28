package io.ipoli.android.challenge.predefined.usecase

import io.ipoli.android.challenge.predefined.entity.PredefinedChallengeData
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.datesBetween
import io.ipoli.android.quest.BaseQuest
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.repeatingquest.entity.RepeatPattern
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
class SchedulePredefinedChallengeUseCase :
    UseCase<SchedulePredefinedChallengeUseCase.Params, List<BaseQuest>> {

    private lateinit var startDate: LocalDate
    private lateinit var endDate: LocalDate

    override fun execute(parameters: Params): List<BaseQuest> {
        val challenge = parameters.challenge

        require(challenge.quests.isNotEmpty(), { "Challenge must contain quests" })

        startDate = parameters.startDate
        endDate = startDate.plusDays((challenge.durationDays - 1).toLong())
        val randomSeed = parameters.randomSeed

        return challenge.quests.map { q ->
            when (q) {
                is PredefinedChallengeData.Quest.Repeating -> {
                    val rq = RepeatingQuest(
                        name = q.name,
                        color = q.color,
                        icon = q.icon,
                        startTime = q.startTime,
                        duration = q.duration,
                        repeatPattern = if (q.weekDays.isEmpty()) {
                            RepeatPattern.Daily(startDate, endDate)
                        } else {
                            RepeatPattern.Weekly(
                                daysOfWeek = q.weekDays.toSet(),
                                startDate = startDate,
                                endDate = endDate
                            )
                        }
                    )
                    listOf<BaseQuest>(rq)
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

                    listOf<BaseQuest>(createFromOneTime(q, scheduledDate))
                }
            }
        }.flatten()
    }

    private fun chooseRandomScheduledDate(randomSeed: Long?): LocalDate {
        val dates = startDate.datesBetween(endDate)

        val random = randomSeed?.let {
            Random(it)
        } ?: Random()

        return dates[random.nextInt(dates.size)]
    }

    private fun createFromOneTime(
        it: PredefinedChallengeData.Quest.OneTime,
        scheduledDate: LocalDate
    ) =
        Quest(
            name = it.name,
            color = it.color,
            icon = it.icon,
            startTime = it.startTime,
            duration = it.duration,
            scheduledDate = scheduledDate
        )

    data class Params(
        val challenge: PredefinedChallengeData,
        val startDate: LocalDate = LocalDate.now(),
        val randomSeed: Long? = null
    )
}