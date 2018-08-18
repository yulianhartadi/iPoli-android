package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.player.persistence.PlayerRepository
import org.threeten.bp.LocalDateTime

class CreateHabitItemsUseCase(private val playerRepository: PlayerRepository) :
    UseCase<CreateHabitItemsUseCase.Params, List<CreateHabitItemsUseCase.HabitItem>> {

    override fun execute(parameters: Params): List<HabitItem> {
        val currentDate = parameters.currentDate

        val resetTime = playerRepository.find()!!.preferences.resetDayTime

        val habits = parameters.habits.sortedWith(
            compareByDescending<Habit> { it.shouldBeDoneOn(currentDate, resetTime) }
                .thenBy { it.isCompletedFor(currentDate, resetTime) }
                .thenByDescending { it.timesADay }
                .thenByDescending { it.isGood }
        )

        val (todayHabits, otherDayHabits) = habits.partition {
            it.shouldBeDoneOn(
                currentDate,
                resetTime
            )
        }

        return when {
            todayHabits.isNotEmpty() && otherDayHabits.isNotEmpty() ->
                listOf(HabitItem.TodaySection) +
                    todayHabits.map {
                        val isCompleted = it.isCompletedFor(currentDate, resetTime)
                        HabitItem.Today(
                            habit = it,
                            isCompleted = if (it.isGood) isCompleted else !isCompleted,
                            completedCount = it.completedCountForDate(currentDate, resetTime),
                            isBestStreak = it.bestStreak != 0 && it.bestStreak == it.currentStreak
                        )
                    } +
                    HabitItem.AnyOtherDaySection +
                    otherDayHabits.map {
                        HabitItem.OtherDay(
                            habit = it,
                            isBestStreak = it.bestStreak != 0 && it.bestStreak == it.currentStreak
                        )
                    }

            todayHabits.isNotEmpty() && otherDayHabits.isEmpty() ->
                listOf(HabitItem.TodaySection) +
                    todayHabits.map {
                        val isCompleted = it.isCompletedFor(currentDate, resetTime)
                        HabitItem.Today(
                            habit = it,
                            isCompleted = if (it.isGood) isCompleted else !isCompleted,
                            completedCount = it.completedCountForDate(currentDate, resetTime),
                            isBestStreak = it.bestStreak != 0 && it.bestStreak == it.currentStreak
                        )
                    }

            otherDayHabits.isNotEmpty() && todayHabits.isEmpty() ->
                listOf(HabitItem.AnyOtherDaySection) +
                    otherDayHabits.map {
                        HabitItem.OtherDay(
                            habit = it,
                            isBestStreak = it.bestStreak != 0 && it.bestStreak == it.currentStreak
                        )
                    }

            else -> emptyList()
        }
    }

    data class Params(val habits: List<Habit>, val currentDate: LocalDateTime = LocalDateTime.now())

    sealed class HabitItem {
        object TodaySection : HabitItem()
        data class Today(
            val habit: Habit,
            val isCompleted: Boolean,
            val completedCount: Int,
            val isBestStreak: Boolean
        ) : HabitItem()

        object AnyOtherDaySection : HabitItem()
        data class OtherDay(val habit: Habit, val isBestStreak: Boolean) : HabitItem()
    }
}