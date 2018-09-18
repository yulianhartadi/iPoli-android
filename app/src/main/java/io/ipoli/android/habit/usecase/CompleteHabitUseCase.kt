package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.toTime
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.persistence.HabitRepository
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.usecase.RewardPlayerUseCase
import io.ipoli.android.quest.job.RewardScheduler
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/17/18.
 */
class CompleteHabitUseCase(
    private val habitRepository: HabitRepository,
    private val playerRepository: PlayerRepository,
    private val rewardPlayerUseCase: RewardPlayerUseCase,
    private val rewardScheduler: RewardScheduler
) : UseCase<CompleteHabitUseCase.Params, Habit> {

    override fun execute(parameters: Params): Habit {
        val habit = habitRepository.findById(parameters.habitId)
        requireNotNull(habit)

        val player = playerRepository.find()!!

        val dateTime = parameters.dateTime
        val date = dateTime.toLocalDate()
        val resetDayTime = player.preferences.resetDayTime

        require(habit!!.shouldBeDoneOn(dateTime, resetDayTime))
        require(!habit.isCompletedFor(dateTime, resetDayTime))

        val history = habit.history.toMutableMap()

        val completedEntry =
            if (history.containsKey(date)) history[date]!!
            else CompletedEntry()

        history[date] = completedEntry.complete(dateTime.toTime())

        val isCompleted = habit.copy(
            history = history
        ).isCompletedFor(dateTime, resetDayTime)


        val playerDate = player.currentDate(dateTime)

        if (isCompleted) {
            history[playerDate] = history[playerDate] ?: CompletedEntry()
        }

        val currentStreak =
            if (isCompleted && habit.isGood) habit.currentStreak + 1
            else if (!habit.isGood) 0
            else habit.currentStreak


        return if (isCompleted) {
            if (habit.isGood) {
                val reward = rewardPlayerUseCase.execute(
                    RewardPlayerUseCase.Params.ForHabit(
                        habit.copy(history = history),
                        playerDate = playerDate,
                        player = player
                    )
                ).reward
                history[playerDate] = history[playerDate]!!.copy(
                    reward = reward
                )
                val h = saveHabit(habit, history, currentStreak)
                rewardScheduler.schedule(
                    reward = reward,
                    type = RewardScheduler.Type.HABIT,
                    entityId = habit.id
                )
                h

            } else {
                val reward = rewardPlayerUseCase.execute(
                    RewardPlayerUseCase.Params.ForBadHabit(
                        habit.copy(history = history),
                        playerDate = playerDate,
                        player = player
                    )
                ).reward
                history[playerDate] = history[playerDate]!!.copy(
                    reward = reward
                )
                val h = saveHabit(habit, history, currentStreak)
                rewardScheduler.schedule(
                    reward = reward.copy(
                        experience = -reward.experience,
                        coins = -reward.coins,
                        attributePoints = reward.attributePoints.map {
                            it.key to -it.value
                        }.toMap()
                    ),
                    isPositive = false,
                    type = RewardScheduler.Type.HABIT,
                    entityId = habit.id
                )
                h
            }
        } else {
            saveHabit(habit, history, currentStreak)
        }
    }

    private fun saveHabit(
        habit: Habit,
        history: MutableMap<LocalDate, CompletedEntry>,
        currentStreak: Int
    ) =
        habitRepository.save(
            habit.copy(
                history = history,
                currentStreak = currentStreak,
                prevStreak = if (currentStreak != habit.currentStreak) habit.currentStreak else habit.prevStreak,
                bestStreak = Math.max(currentStreak, habit.bestStreak)
            )
        )

    data class Params(val habitId: String, val dateTime: LocalDateTime = LocalDateTime.now())
}