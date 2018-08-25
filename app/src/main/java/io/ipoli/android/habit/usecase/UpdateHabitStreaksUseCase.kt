package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.persistence.HabitRepository
import org.threeten.bp.LocalDateTime

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/20/18.
 */
class UpdateHabitStreaksUseCase(
    private val habitRepository: HabitRepository
) : UseCase<UpdateHabitStreaksUseCase.Params, List<Habit>> {

    override fun execute(parameters: Params): List<Habit> {
        val habits = habitRepository.findAllNotRemoved()

        val date = parameters.today.minusDays(1)

        return habitRepository.save(habits.mapNotNull {
            when {
                it.isCompletedFor(date, parameters.resetDayTime) -> null
                it.isGood ->
                    it.copy(
                        currentStreak = 0
                    )
                else ->
                    it.copy(
                        currentStreak = it.currentStreak + 1,
                        bestStreak = Math.max(it.bestStreak, it.currentStreak + 1)
                    )
            }
        })
    }

    data class Params(
        val today: LocalDateTime,
        val resetDayTime: Time
    )
}