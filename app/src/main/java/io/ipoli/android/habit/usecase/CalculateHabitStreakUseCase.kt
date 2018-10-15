package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.datesBetween
import io.ipoli.android.common.datetime.daysBetween
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.habit.data.Habit
import org.threeten.bp.LocalDate

class CalculateHabitStreakUseCase :
    UseCase<CalculateHabitStreakUseCase.Params, Habit.Streak> {

    override fun execute(parameters: Params): Habit.Streak {
        val h = parameters.habit
        val today = parameters.today
        val createdDate = h.createdAt.toEpochMilli().startOfDayUTC
        if (h.history.isEmpty()) {
            return if (h.isGood) {
                Habit.Streak(0, 0)
            } else {
                Habit.Streak(
                    createdDate.daysBetween(today).toInt(),
                    createdDate.daysBetween(today).toInt()
                )
            }
        }
        val hs = DateUtils.min(h.history.toSortedMap().firstKey(), createdDate)
        val ds = hs.datesBetween(today).reversed()

        return Habit.Streak(
            current = findCurrentStreak(h, ds),
            best = findBestStreak(h, ds)
        )
    }

    private fun findCurrentStreak(habit: Habit, dates: List<LocalDate>): Int {
        var streak = 0
        dates.forEach {
            val s = streakStatusForDate(habit, it)
            if (s == DateStatus.FAILED) {
                return streak
            } else if (s == DateStatus.COMPLETE) {
                streak++
            }
        }
        return streak
    }

    private fun findBestStreak(habit: Habit, dates: List<LocalDate>): Int {
        var cs = 0
        var bs = 0
        dates.forEach {
            val s = streakStatusForDate(habit, it)
            if (s == DateStatus.FAILED) {
                if (cs > bs) {
                    bs = cs
                }
                cs = 0
            } else if (s == DateStatus.COMPLETE) {
                cs++
            }
        }
        return Math.max(bs, cs)
    }

    private fun streakStatusForDate(habit: Habit, date: LocalDate) =
        when {
            !habit.isGood && habit.isCompletedForDate(date) -> DateStatus.FAILED
            !habit.isGood && habit.shouldBeDoneOn(date) -> DateStatus.COMPLETE
            habit.isCompletedForDate(date) -> DateStatus.COMPLETE
            !habit.shouldBeDoneOn(date) -> DateStatus.SHOULD_NOT_COMPLETE
            else -> DateStatus.FAILED
        }

    enum class DateStatus {
        COMPLETE, SHOULD_NOT_COMPLETE, FAILED
    }

    data class Params(val habit: Habit, val today: LocalDate = LocalDate.now())

}