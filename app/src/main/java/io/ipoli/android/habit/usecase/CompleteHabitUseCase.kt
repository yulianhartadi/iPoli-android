package io.ipoli.android.habit.usecase

import io.ipoli.android.common.SimpleReward
import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.HabitReward
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.persistence.HabitRepository
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RemoveRewardFromPlayerUseCase
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.job.RewardScheduler
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/17/18.
 */
class CompleteHabitUseCase(
    private val habitRepository: HabitRepository,
    private val playerRepository: PlayerRepository,
    private val rewardScheduler: RewardScheduler,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val removeRewardFromPlayerUseCase: RemoveRewardFromPlayerUseCase,
    private val randomSeed: Long? = null
) : UseCase<CompleteHabitUseCase.Params, Habit> {

    override fun execute(parameters: Params): Habit {
        val habit = habitRepository.findById(parameters.habitId)
        requireNotNull(habit)

        val date = parameters.date
        if (!habit!!.shouldBeDoneOn(date)) {
            return habit
        }

        val history = habit.history.toMutableMap()
        val completedEntry =
            if (history.containsKey(date)) history[date]!!
            else CompletedEntry()

        if (completedEntry.completedCount == habit.timesADay) {
            return habit
        }

        history[date] = completedEntry.complete()

        val isCompleted = history[date]!!.completedCount == habit.timesADay

        if (isCompleted) {
            val pet = playerRepository.find()!!.pet
            val ce = history[date]!!
            val reward = HabitReward(randomSeed).generate(pet.coinBonus, pet.experienceBonus)
            history[date] = ce.copy(
                experience = ce.coins ?: reward.coins,
                coins = ce.experience ?: reward.experience
            )
        }

        val currentStreak =
            if (isCompleted && habit.isGood) habit.currentStreak + 1
            else if (!habit.isGood) 0
            else habit.currentStreak


        val newHabit = habitRepository.save(
            habit.copy(
                history = history,
                currentStreak = currentStreak,
                prevStreak = if (currentStreak != habit.currentStreak) habit.currentStreak else habit.prevStreak,
                bestStreak = Math.max(currentStreak, habit.bestStreak)
            )
        )

        if (isCompleted) {
            val reward = SimpleReward(
                coins = history[date]!!.coins!!,
                experience = history[date]!!.experience!!,
                bounty = Quest.Bounty.None
            )
            if (habit.isGood) {
                rewardPlayerUseCase.execute(reward)
                rewardScheduler.schedule(
                    reward
                )
            } else {
                removeRewardFromPlayerUseCase.execute(reward)
                rewardScheduler.schedule(
                    reward.copy(
                        coins = -reward.coins,
                        experience = -reward.experience
                    ),
                    false
                )
            }
        }

        return newHabit
    }

    data class Params(val habitId: String, val date: LocalDate = LocalDate.now())
}