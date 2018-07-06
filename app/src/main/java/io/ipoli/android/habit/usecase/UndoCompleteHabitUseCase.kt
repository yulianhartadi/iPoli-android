package io.ipoli.android.habit.usecase

import io.ipoli.android.common.SimpleReward
import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.persistence.HabitRepository
import io.ipoli.android.player.usecase.RemoveRewardFromPlayerUseCase
import io.ipoli.android.quest.Quest
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/17/18.
 */
class UndoCompleteHabitUseCase(
    private val habitRepository: HabitRepository,
    private val removeRewardFromPlayerUseCase: RemoveRewardFromPlayerUseCase
) : UseCase<UndoCompleteHabitUseCase.Params, Habit> {

    override fun execute(parameters: Params): Habit {
        val habit = habitRepository.findById(parameters.habitId)
        requireNotNull(habit)

        val history = habit!!.history.toMutableMap()
        val date = parameters.date

        if (!history.containsKey(date) || history[date]!!.completedCount == 0) {
            return habit
        }

        val wasCompleted = habit.isCompletedFor(date)

        val ce = history[date]!!
        history[date] = ce.undoLastComplete()

        if (wasCompleted && habit.isGood) {
            removeRewardFromPlayerUseCase.execute(
                SimpleReward(
                    coins = ce.coins!!,
                    experience = ce.experience!!,
                    bounty = Quest.Bounty.None
                )
            )
        }

        val currentStreak = habit.currentStreak
        val bestStreak = habit.bestStreak

        val newStreak = Math.max(
            if (wasCompleted && habit.isGood) currentStreak - 1
            else if (!habit.isGood) habit.prevStreak
            else currentStreak,
            0
        )

        return habitRepository.save(
            habit.copy(
                history = history,
                currentStreak = newStreak,
                prevStreak = if (newStreak != currentStreak && currentStreak != 0) currentStreak else habit.prevStreak,
                bestStreak = if (habit.isGood) {
                    if (wasCompleted && bestStreak == currentStreak) Math.max(bestStreak - 1, 0) else bestStreak
                } else {
                    if (habit.prevStreak > bestStreak) {
                        habit.prevStreak
                    } else {
                        bestStreak
                    }
                }

            )
        )
    }

    data class Params(val habitId: String, val date: LocalDate = LocalDate.now())
}