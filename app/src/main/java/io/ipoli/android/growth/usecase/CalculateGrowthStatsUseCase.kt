package io.ipoli.android.growth.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.*
import io.ipoli.android.planday.usecase.CalculateAwesomenessScoreUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters

class CalculateGrowthStatsUseCase(
    private val calculateAwesomenessScoreUseCase: CalculateAwesomenessScoreUseCase,
    private val questRepository: QuestRepository
) : UseCase<CalculateGrowthStatsUseCase.Params, CalculateGrowthStatsUseCase.Result> {

    private fun calculateAwesomenessScore(quests: List<Quest>) =
        calculateAwesomenessScoreUseCase.execute(
            CalculateAwesomenessScoreUseCase.Params(
                quests
            )
        )

    private fun calculateProductiveMinutes(quests: List<Quest>) =
        quests
            .sumBy {
                if (it.hasTimer) {
                    it.actualDuration.asMinutes.intValue
                } else {
                    0
                }
            }.minutes

    override fun execute(parameters: Params): CalculateGrowthStatsUseCase.Result {

        val monthStart = parameters.currentDate.withDayOfMonth(1)
        val monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth())
        val weekStartDate =
            parameters.currentDate.with(TemporalAdjusters.previousOrSame(parameters.firstDayOfWeek))
        val weekEndDate =
            parameters.currentDate.with(TemporalAdjusters.nextOrSame(parameters.lastDayOfWeek))

        val periodStart =
            monthStart.with(TemporalAdjusters.previousOrSame(parameters.firstDayOfWeek))
        val periodEnd = monthEnd.with(TemporalAdjusters.nextOrSame(parameters.lastDayOfWeek))

        val quests =
            questRepository.findOriginallyScheduledOrCompletedInRange(periodStart, periodEnd)

        val focusTimeByDay = quests.filter { it.isCompleted }
            .groupBy { it.completedAtDate }
            .map {
                it.key!! to calculateProductiveMinutes(it.value)
            }.toMap()

        val awesomenessScoreByDay = quests
            .filter { it.originalScheduledDate != null }
            .groupBy { it.originalScheduledDate!! }
            .map {
                it.key to calculateAwesomenessScore(it.value)
            }.toMap()

        val todayQuests = quests.filter { it.completedAtDate == parameters.currentDate }

        val weeklyQuests = quests.filter {
            it.completedAtDate?.isBetween(
                weekStartDate,
                weekEndDate
            ) ?: false
        }

        val monthlyQuests = quests.filter { it.completedAtDate != null }

        return Result(
            todayGrowth = Growth.Today(
                date = parameters.currentDate,
                stats = createSummaryStats(todayQuests),
                challengeProgress = createTodayChallengeProgress(quests, parameters.currentDate),
                progressEntries = createTodayProgressEntries(todayQuests)
            ),
            weeklyGrowth = Growth.Week(
                startDate = weekStartDate,
                endDate = weekEndDate,
                stats = createSummaryStats(weeklyQuests),
                challengeProgress = createWeeklyChallengeProgress(
                    quests = quests,
                    weekStartDate = weekStartDate,
                    weekEndDate = weekEndDate
                ),
                progressEntries = createWeeklyProgressEntries(
                    weekStartDate = weekStartDate,
                    focusTimeByDay = focusTimeByDay,
                    awesomenessScoreByDay = awesomenessScoreByDay
                )
            ),
            monthlyGrowth = Growth.Month(
                stats = createSummaryStats(monthlyQuests),
                challengeProgress = createMonthlyChallengeProgress(
                    quests = quests,
                    periodStart = periodStart,
                    periodEnd = periodEnd
                ),
                progressEntries = createMonthlyProgressEntries(
                    periodStart = periodStart,
                    periodEnd = periodEnd,
                    focusTimeByDay = focusTimeByDay,
                    awesomenessScoreByDay = awesomenessScoreByDay
                )
            )
        )
    }

    private fun createSummaryStats(quests: List<Quest>): SummaryStats {
        val timeSpent = quests.map { it.actualDuration.asMinutes.intValue }.sum().minutes
        val xpEarned = quests.map { it.experience!! }.sum()
        val coinsEarned = quests.map { it.coins!! }.sum()

        return SummaryStats(
            timeSpent = timeSpent,
            experienceEarned = xpEarned,
            coinsEarned = coinsEarned
        )
    }

    private fun createTodayChallengeProgress(
        quests: List<Quest>,
        currentDate: LocalDate
    ): List<Challenge> {
        val todayQuests =
            quests
                .filter { (it.completedAtDate == currentDate || it.scheduledDate == currentDate) && it.challengeId != null }

        return createChallenges(todayQuests)
    }

    private fun createWeeklyChallengeProgress(
        quests: List<Quest>,
        weekStartDate: LocalDate,
        weekEndDate: LocalDate
    ): List<Challenge> {
        val weeklyQuests =
            quests.filter {

                val completed = it.completedAtDate?.isBetween(
                    weekStartDate,
                    weekEndDate
                ) ?: false

                val scheduled = it.scheduledDate?.isBetween(
                    weekStartDate,
                    weekEndDate
                ) ?: false

                (completed || scheduled) && it.challengeId != null
            }

        return createChallenges(weeklyQuests)
    }

    private fun createMonthlyChallengeProgress(
        quests: List<Quest>,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): List<Challenge> {
        val weeklyQuests =
            quests.filter {

                val completed = it.completedAtDate?.isBetween(
                    periodStart,
                    periodEnd
                ) ?: false

                val scheduled = it.scheduledDate?.isBetween(
                    periodStart,
                    periodEnd
                ) ?: false

                (completed || scheduled) && it.challengeId != null
            }

        return createChallenges(weeklyQuests)
    }

    private fun createChallenges(questsForPeriod: List<Quest>): List<Challenge> {
        val questsByChallenge = questsForPeriod.groupBy { it.challengeId!! }
        return questsByChallenge.map {
            val qs = it.value
            val completedCount = qs.count { it.isCompleted }
            Challenge(
                id = it.key,
                progressPercent = ((completedCount / qs.size.toFloat()) * 100f).toInt(),
                completeQuestCount = completedCount,
                totalQuestCount = qs.size,
                timeSpent = minutesSpentForQuests(qs)
            )
        }
    }

    private fun minutesSpentForQuests(qs: List<Quest>) =
        qs
            .filter { it.isCompleted }
            .map { it.actualDuration.asMinutes.intValue }
            .sum()
            .minutes

    private fun createTodayProgressEntries(
        todayQuests: List<Quest>
    ): List<ProgressEntry.Today> {

        val ranges = LinkedHashMap<Time, MutableList<Quest>>()
        (6..24 step 3).forEach {
            ranges[Time.atHours(it)] = mutableListOf()
        }

        todayQuests.forEach { q ->
            for (time in ranges.keys) {
                if (q.completedAtTime!! < time) {
                    ranges[time]!!.add(q)
                    break
                }
            }
        }

        var prodMins = 0
        var awesomeScore = 0.0

        return ranges.map { (t, qs) ->
            prodMins += calculateProductiveMinutes(qs).intValue
            awesomeScore += calculateAwesomenessScore(qs)
            ProgressEntry.Today(
                periodEnd = t,
                productiveMinutes = prodMins.minutes,
                awesomenessScore = awesomeScore
            )
        }
    }

    private fun createWeeklyProgressEntries(
        weekStartDate: LocalDate,
        focusTimeByDay: Map<LocalDate, Duration<Minute>>,
        awesomenessScoreByDay: Map<LocalDate, Double>
    ): List<ProgressEntry.Week> {

        val weekDays = weekStartDate.datesAhead(7)

        return weekDays.map {
            ProgressEntry.Week(
                date = it,
                productiveMinutes = focusTimeByDay[it] ?: 0.minutes,
                awesomenessScore = awesomenessScoreByDay[it] ?: 0.0
            )
        }
    }

    private fun createMonthlyProgressEntries(
        periodStart: LocalDate,
        periodEnd: LocalDate,
        focusTimeByDay: Map<LocalDate, Duration<Minute>>,
        awesomenessScoreByDay: Map<LocalDate, Double>
    ): MutableList<ProgressEntry.Month> {
        val monthProgressEntries = mutableListOf<ProgressEntry.Month>()
        var start = periodStart
        while (start.isBefore(periodEnd)) {
            val end = start.plusDays(6)

            val mins = start.datesBetween(end).map {
                focusTimeByDay[it] ?: 0.minutes
            }
            val scores = start.datesBetween(end).map {
                awesomenessScoreByDay[it] ?: 0.0
            }

            monthProgressEntries.add(
                ProgressEntry.Month(
                    weekStart = start,
                    weekEnd = end,
                    productiveMinutes = (mins.sumBy { it.intValue } / 7).minutes,
                    awesomenessScore = (scores.sum() / 7.0)
                )
            )
            start = start.plusWeeks(1)
        }
        return monthProgressEntries
    }

    data class Challenge(
        val id: String,
        val progressPercent: Int,
        val completeQuestCount: Int,
        val totalQuestCount: Int,
        val timeSpent: Duration<Minute>
    )

    data class SummaryStats(
        val timeSpent: Duration<Minute>,
        val experienceEarned: Int,
        val coinsEarned: Int
    )

    sealed class ProgressEntry(
        open val productiveMinutes: Duration<Minute>,
        open val awesomenessScore: Double
    ) {

        data class Today(
            val periodEnd: Time,
            override val productiveMinutes: Duration<Minute>,
            override val awesomenessScore: Double
        ) : ProgressEntry(productiveMinutes, awesomenessScore)

        data class Week(
            val date: LocalDate,
            override val productiveMinutes: Duration<Minute>,
            override val awesomenessScore: Double
        ) : ProgressEntry(productiveMinutes, awesomenessScore)

        data class Month(
            val weekStart: LocalDate,
            val weekEnd: LocalDate,
            override val productiveMinutes: Duration<Minute>,
            override val awesomenessScore: Double
        ) : ProgressEntry(productiveMinutes, awesomenessScore)
    }

    sealed class Growth {
        data class Today(
            val date: LocalDate,
            val stats: SummaryStats,
            val challengeProgress: List<Challenge>,
            val progressEntries: List<ProgressEntry.Today>
        ) : Growth()

        data class Week(
            val startDate: LocalDate,
            val endDate: LocalDate,
            val stats: SummaryStats,
            val challengeProgress: List<Challenge>,
            val progressEntries: List<ProgressEntry.Week>
        ) : Growth()

        data class Month(
            val stats: SummaryStats,
            val challengeProgress: List<Challenge>,
            val progressEntries: List<ProgressEntry.Month>
        ) : Growth()
    }

    data class Params(
        val currentDate: LocalDate = LocalDate.now(),
        val firstDayOfWeek: DayOfWeek = DateUtils.firstDayOfWeek,
        val lastDayOfWeek: DayOfWeek = DateUtils.lastDayOfWeek
    )

    data class Result(
        val todayGrowth: Growth.Today,
        val weeklyGrowth: Growth.Week,
        val monthlyGrowth: Growth.Month
    )
}