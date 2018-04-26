package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.datesBetween
import io.ipoli.android.common.datetime.daysUntil
import io.ipoli.android.quest.Quest
import io.ipoli.android.repeatingquest.entity.RepeatPattern
import io.ipoli.android.repeatingquest.entity.RepeatPattern.Companion.findMonthlyPeriods
import io.ipoli.android.repeatingquest.entity.RepeatPattern.Companion.findWeeklyPeriods
import io.ipoli.android.repeatingquest.entity.RepeatPattern.Companion.monthlyDatesToScheduleInPeriod
import io.ipoli.android.repeatingquest.entity.RepeatPattern.Companion.weeklyDatesToScheduleInPeriod
import io.ipoli.android.repeatingquest.entity.RepeatPattern.Companion.yearlyDatesToScheduleInPeriod
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
            val end = if (rqEnd == null) challenge.endDate else DateUtils.min(rqEnd, challenge.endDate)
            val repeatingPattern = rq.repeatPattern

            val removedCount =
                challenge.quests.filter { it.repeatingQuestId == rq.id && it.isRemoved }.size

            val allCount = when (repeatingPattern) {
                is RepeatPattern.Daily -> {
                    start.daysUntil(end).toInt() + 1
                }

                is RepeatPattern.Weekly -> {
                    weeklyDatesToScheduleInPeriod(repeatingPattern, start, end).size
                }

                is RepeatPattern.Monthly -> {
                    monthlyDatesToScheduleInPeriod(repeatingPattern, start, end).size
                }

                is RepeatPattern.Yearly -> {
                    yearlyDatesToScheduleInPeriod(repeatingPattern, start, end).size
                }

                is RepeatPattern.Flexible.Weekly -> {
                    val periods = findWeeklyPeriods(start, end)
                    periods.sumBy {
                        if (repeatingPattern.scheduledPeriods.containsKey(it.start)) {
                            repeatingPattern.scheduledPeriods[it.start]!!.size
                        } else {
                            repeatingPattern.timesPerWeek
                        }
                    }
                }

                is RepeatPattern.Flexible.Monthly -> {
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

        val historyData = challenge.startDate.datesBetween(parameters.currentDate).map {
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