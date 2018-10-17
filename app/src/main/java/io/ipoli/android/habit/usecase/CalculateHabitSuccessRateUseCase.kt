package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.datesBetween
import io.ipoli.android.habit.data.Habit
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/16/18.
 */
class CalculateHabitSuccessRateUseCase :
    UseCase<CalculateHabitSuccessRateUseCase.Params, Int> {

    override fun execute(parameters: Params): Int {
        val habit = parameters.habit
        val createdAt = DateUtils.fromMillis(habit.createdAt.toEpochMilli())

        val firstCompletedDate = habit.history.toSortedMap().entries.firstOrNull {
            it.value.completedCount > 0
        }?.key
        val firstDate = if (firstCompletedDate == null) createdAt else DateUtils.min(
            createdAt,
            firstCompletedDate
        )

        var allDays = 0
        var completedDays = 0
        firstDate.datesBetween(parameters.today).forEach {
            val isCompleted = habit.isCompletedForDate(it)
            if ((isCompleted && habit.isGood) || (!isCompleted && !habit.isGood)) completedDays++
            if (habit.shouldBeDoneOn(it) || isCompleted) allDays++
        }

        return (completedDays.toFloat() / allDays * 100).toInt()
    }

    data class Params(val habit: Habit, val today: LocalDate = LocalDate.now())
}