package mypoli.android.challenge.usecase

import mypoli.android.challenge.entity.Challenge
import mypoli.android.common.UseCase
import mypoli.android.common.datetime.DateUtils
import mypoli.android.common.datetime.datesBetween
import mypoli.android.common.datetime.daysUntil
import mypoli.android.quest.Quest
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingPattern.Companion.findMonthlyPeriods
import mypoli.android.repeatingquest.entity.RepeatingPattern.Companion.findWeeklyPeriods
import mypoli.android.repeatingquest.entity.RepeatingPattern.Companion.monthlyDatesToScheduleInPeriod
import mypoli.android.repeatingquest.entity.RepeatingPattern.Companion.weeklyDatesToScheduleInPeriod
import mypoli.android.repeatingquest.entity.RepeatingPattern.Companion.yearlyDatesToScheduleInPeriod
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/13/18.
 */
class FindChallengeProgressUseCase : UseCase<FindChallengeProgressUseCase.Params, Challenge> {
    override fun execute(parameters: Params): Challenge {
        val challenge = parameters.challenge

        val repeatingCount = challenge.repeatingQuests.sumBy { rq ->
            val rqEnd = rq.end
            val start = rq.start
            val end = if (rqEnd == null) challenge.end else DateUtils.min(rqEnd, challenge.end)
            val repeatingPattern = rq.repeatingPattern

            val removedCount =
                challenge.quests.filter { it.repeatingQuestId == rq.id && it.isRemoved }.size

            val allCount = when (repeatingPattern) {
                is RepeatingPattern.Daily -> {
                    start.daysUntil(end).toInt() + 1
                }

                is RepeatingPattern.Weekly -> {
                    weeklyDatesToScheduleInPeriod(repeatingPattern, start, end).size
                }

                is RepeatingPattern.Monthly -> {
                    monthlyDatesToScheduleInPeriod(repeatingPattern, start, end).size
                }

                is RepeatingPattern.Yearly -> {
                    yearlyDatesToScheduleInPeriod(repeatingPattern, start, end).size
                }

                is RepeatingPattern.Flexible.Weekly -> {
                    val periods = findWeeklyPeriods(start, end, parameters.lastDayOfWeek)
                    periods.sumBy {
                        if (repeatingPattern.scheduledPeriods.containsKey(it.start)) {
                            repeatingPattern.scheduledPeriods[it.start]!!.size
                        } else {
                            repeatingPattern.timesPerWeek
                        }
                    }
                }

                is RepeatingPattern.Flexible.Monthly -> {
                    val periods = findMonthlyPeriods(start, end)
                    periods.sumBy {
                        if (repeatingPattern.scheduledPeriods.containsKey(it.start)) {
                            repeatingPattern.scheduledPeriods[it.start]!!.size
                        } else {
                            repeatingPattern.timesPerMonth
                        }
                    }
                }
            }

            allCount - removedCount
        }

        val completedCount = challenge.quests.filter { it.isCompleted && !it.isRemoved }.size
        val allCount = repeatingCount + challenge.baseQuests.filter { it is Quest }.size

        val increasePerQuest = (1f / allCount) * 100f

        val historyData = challenge.start.datesBetween(parameters.currentDate).map {
            it to 0f
        }.toMap().toMutableMap()

        challenge.quests
            .filter { it.isCompleted }
            .forEach {
                historyData[it.completedAtDate!!] = historyData[it.completedAtDate]!! +
                    increasePerQuest
            }

        return challenge.copy(
            progress = Challenge.Progress(
                completedCount = completedCount,
                allCount = allCount,
                history = historyData
            )
        )
    }

    data class Params(
        val challenge: Challenge,
        val lastDayOfWeek: DayOfWeek = DateUtils.lastDayOfWeek,
        val currentDate: LocalDate = LocalDate.now()
    )
}