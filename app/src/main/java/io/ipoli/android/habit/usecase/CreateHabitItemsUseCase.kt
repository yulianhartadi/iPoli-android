package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.data.Habit
import org.threeten.bp.LocalDate

class CreateHabitItemsUseCase :
    UseCase<CreateHabitItemsUseCase.Params, List<CreateHabitItemsUseCase.HabitItem>> {

    override fun execute(parameters: Params): List<HabitItem> {
        val habits = parameters.habits.sortedWith(
            compareByDescending<Habit> { it.shouldBeDoneOn(LocalDate.now()) }
                .thenBy { it.isCompletedFor(LocalDate.now())}
                .thenByDescending { it.timesADay }
                .thenByDescending { it.isGood }
        )
        val currentDate = parameters.currentDate
        val (todayHabits, otherDayHabits) = habits.partition { it.shouldBeDoneOn(currentDate) }

        return when {
            todayHabits.isNotEmpty() && otherDayHabits.isNotEmpty() ->
                listOf(HabitItem.TodaySection) +
                    todayHabits.map {
                        HabitItem.Today(it)
                    } +
                    HabitItem.AnyOtherDaySection +
                    otherDayHabits.map {
                        HabitItem.OtherDay(it)
                    }

            todayHabits.isNotEmpty() && otherDayHabits.isEmpty() ->
                listOf(HabitItem.TodaySection) +
                    todayHabits.map {
                        HabitItem.Today(it)
                    }

            otherDayHabits.isNotEmpty() && todayHabits.isEmpty() ->
                listOf(HabitItem.AnyOtherDaySection) +
                    otherDayHabits.map {
                        HabitItem.OtherDay(it)
                    }

            else -> emptyList()
        }
    }

    data class Params(val habits: List<Habit>, val currentDate: LocalDate = LocalDate.now())

    sealed class HabitItem {
        object TodaySection : HabitItem()
        data class Today(val habit: Habit) : HabitItem()

        object AnyOtherDaySection : HabitItem()
        data class OtherDay(val habit: Habit) : HabitItem()
    }
}