package io.ipoli.android.habit.usecase

import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.TestUtil
import io.ipoli.android.common.Reward
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.usecase.RemoveRewardFromPlayerUseCase
import io.ipoli.android.quest.Quest
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/17/18.
 */
class UndoCompleteHabitUseCaseSpek : Spek({


    describe("UndoCompleteHabitUseCase") {
        val completedEntry =
            CompletedEntry(completedAtTimes = listOf(Time.now()), reward = Reward(emptyMap(), 0, 1, 1, Quest.Bounty.None))

        fun executeUseCase(
            habit: Habit,
            dateTime: LocalDateTime = LocalDateTime.now(),
            removeRewardFromPlayerUseCase: RemoveRewardFromPlayerUseCase = mock(),
            player: Player = TestUtil.player
        ) =
            UndoCompleteHabitUseCase(
                TestUtil.habitRepoMock(
                    habit
                ),
                TestUtil.playerRepoMock(player),
                removeRewardFromPlayerUseCase
            ).execute(
                UndoCompleteHabitUseCase.Params(habit.id, dateTime)
            )

        it("should not remove from empty history") {
            val habit = executeUseCase(
                TestUtil.habit
            )
            habit.history.`should be empty`()
        }

        it("should remove time") {
            val today = LocalDate.now()
            val habit = executeUseCase(
                TestUtil.habit.copy(
                    history = mapOf(today to completedEntry)
                ),
                LocalDateTime.now()
            )
            habit.history[today]!!.completedAtTimes.`should be empty`()
        }

        it("should remove time with more than 1 times a day") {
            val today = LocalDate.now()
            val now = Time.now()

            val habit = executeUseCase(
                TestUtil.habit.copy(
                    timesADay = 2,
                    history = mapOf(
                        today to completedEntry.copy(
                            completedAtTimes = listOf(
                                now,
                                now.plus(30)
                            )
                        )
                    )
                ),
                LocalDateTime.now()
            )
            val completedAtTimes = habit.history[today]!!.completedAtTimes
            completedAtTimes.size.`should be equal to`(1)
            completedAtTimes.first().`should equal`(now)
        }

        it("should decrease current streak") {
            val today = LocalDate.now()
            val habit = executeUseCase(
                TestUtil.habit.copy(
                    history = mapOf(today to completedEntry),
                    prevStreak = 0,
                    currentStreak = 1
                ),
                LocalDateTime.now()
            )
            habit.currentStreak.`should be equal to`(0)
            habit.prevStreak.`should be equal to`(1)
        }

        it("should not decrease current streak with more thant 1 times a day") {
            val today = LocalDate.now()

            val habit = executeUseCase(
                TestUtil.habit.copy(
                    timesADay = 2,
                    history = mapOf(today to completedEntry),
                    currentStreak = 3
                ),
                LocalDateTime.now()
            )
            habit.currentStreak.`should be equal to`(3)
        }

        it("should decrease best streak") {
            val today = LocalDate.now()
            val habit = executeUseCase(
                TestUtil.habit.copy(
                    history = mapOf(today to completedEntry),
                    currentStreak = 2,
                    bestStreak = 2
                ),
                LocalDateTime.now()
            )
            habit.bestStreak.`should be equal to`(1)
        }

        it("should not decrease best streak") {
            val today = LocalDate.now()
            val habit = executeUseCase(
                TestUtil.habit.copy(
                    history = mapOf(today to completedEntry),
                    currentStreak = 2,
                    bestStreak = 3
                ),
                LocalDateTime.now()
            )
            habit.bestStreak.`should be equal to`(3)
        }

        it("should increase best streak of negative") {
            val today = LocalDate.now()
            val habit = executeUseCase(
                TestUtil.habit.copy(
                    history = mapOf(today to completedEntry),
                    currentStreak = 0,
                    prevStreak = 4,
                    bestStreak = 3,
                    isGood = false
                ),
                LocalDateTime.now()
            )
            habit.bestStreak.`should be equal to`(4)
        }

        it("should use prev streak of negative habit") {
            val today = LocalDate.now()
            val habit = executeUseCase(
                TestUtil.habit.copy(
                    history = mapOf(today to completedEntry),
                    currentStreak = 0,
                    prevStreak = 4,
                    isGood = false
                ),
                LocalDateTime.now()
            )
            habit.currentStreak.`should be equal to`(4)
            habit.prevStreak.`should be equal to`(4)
        }


        describe("Rewards") {
            it("should not remove rewards") {
                val today = LocalDateTime.now()
                val habit = executeUseCase(
                    TestUtil.habit.copy(
                        history = mapOf(
                            today.toLocalDate() to CompletedEntry(
                                completedAtTimes = listOf(Time.now()),
                                reward = completedEntry.reward
                            )
                        )
                    ),
                    today
                )
                habit.history[today.toLocalDate()]!!.reward.`should not be null`()
            }
        }

        it("should remove bounty when was already completed") {

            val removeRewardFromPlayerUseCaseMock = mock<RemoveRewardFromPlayerUseCase>()
            val today = LocalDateTime.now()
            executeUseCase(
                TestUtil.habit.copy(
                    history = mapOf(
                        today.toLocalDate() to completedEntry
                    )
                ),
                today,
                removeRewardFromPlayerUseCaseMock
            )
            Verify on removeRewardFromPlayerUseCaseMock that removeRewardFromPlayerUseCaseMock.execute(
                RemoveRewardFromPlayerUseCase.Params(completedEntry.reward!!)
            ) was called
        }

        it("should not remove bounty when was not already completed") {

            val removeRewardFromPlayerUseCaseMock = mock<RemoveRewardFromPlayerUseCase>()
            val today = LocalDateTime.now()
            executeUseCase(
                TestUtil.habit.copy(
                    history = mapOf(
                        today.toLocalDate() to completedEntry
                    ),
                    timesADay = 2
                ),
                today,
                removeRewardFromPlayerUseCaseMock
            )
            `Verify not called` on removeRewardFromPlayerUseCaseMock that removeRewardFromPlayerUseCaseMock.execute(
                RemoveRewardFromPlayerUseCase.Params(completedEntry.reward!!)
            )
        }

        it("should not remove bounty on negative habit") {

            val removeRewardFromPlayerUseCaseMock = mock<RemoveRewardFromPlayerUseCase>()
            val today = LocalDateTime.now()
            executeUseCase(
                TestUtil.habit.copy(
                    history = mapOf(
                        today.toLocalDate() to completedEntry
                    ),
                    isGood = false
                ),
                today,
                removeRewardFromPlayerUseCaseMock
            )
            `Verify not called` on removeRewardFromPlayerUseCaseMock that removeRewardFromPlayerUseCaseMock.execute(
                RemoveRewardFromPlayerUseCase.Params(completedEntry.reward!!)
            )
        }

        it("should undo last completed time after reset time a day") {
            val habit = TestUtil.habit.copy(
                timesADay = 2,
                isGood = true,
                history = mapOf(
                    LocalDate.now() to CompletedEntry(
                        completedAtTimes = listOf(
                            Time.at(12, 15),
                            Time.at(12, 40)
                        )
                    )
                )
            )

            val p = TestUtil.player.copy(
                preferences = TestUtil.player.preferences.copy(
                    resetDayTime = Time.at(12, 30)
                )
            )

            val newHabit = executeUseCase(
                habit = habit,
                dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 45)),
                player = p
            )

            newHabit.history.size.`should be`(1)
            val ce = newHabit.history.values.first()
            ce.completedAtTimes.size.`should be`(1)
            ce.completedAtTimes.first().`should equal`(Time.at(12, 15))
        }

        it("should not undo last completed time before reset time a day") {
            val habit = TestUtil.habit.copy(
                timesADay = 2,
                isGood = true,
                history = mapOf(
                    LocalDate.now() to CompletedEntry(
                        completedAtTimes = listOf(
                            Time.at(12, 15)
                        )
                    )
                )
            )

            val p = TestUtil.player.copy(
                preferences = TestUtil.player.preferences.copy(
                    resetDayTime = Time.at(12, 30)
                )
            )

            val newHabit = executeUseCase(
                habit = habit,
                dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 45)),
                player = p
            )

            newHabit.history.size.`should be`(1)
            val ce = newHabit.history.values.first()
            ce.completedAtTimes.size.`should be`(1)
            ce.completedAtTimes.first().`should equal`(Time.at(12, 15))
        }

        it("should remove time from current date history entry") {
            val expectedReward =
                Reward(emptyMap(), 0, 10, 1, Quest.Bounty.None)
            val removeRewardFromPlayerUseCaseMock = mock<RemoveRewardFromPlayerUseCase>()
            val today = LocalDate.now()
            val habit = TestUtil.habit.copy(
                timesADay = 2,
                isGood = true,
                history = mapOf(
                    today to CompletedEntry(
                        completedAtTimes = listOf(
                            Time.at(12, 15),
                            Time.at(12, 40),
                            Time.at(12, 45)
                        )
                    ),
                    today.plusDays(1) to CompletedEntry(
                        completedAtTimes = emptyList(),
                        reward = expectedReward
                    )
                )
            )

            val p = TestUtil.player.copy(
                preferences = TestUtil.player.preferences.copy(
                    resetDayTime = Time.at(12, 30)
                )
            )

            val newHabit = executeUseCase(
                habit = habit,
                dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 46)),
                player = p,
                removeRewardFromPlayerUseCase = removeRewardFromPlayerUseCaseMock
            )

            newHabit.history.size.`should be`(2)
            val ce1 = newHabit.history.values.first()
            val ce2 = newHabit.history.values.last()
            ce1.completedAtTimes.size.`should be`(2)
            ce2.completedAtTimes.`should be empty`()


            Verify on removeRewardFromPlayerUseCaseMock that removeRewardFromPlayerUseCaseMock.execute(
                RemoveRewardFromPlayerUseCase.Params(expectedReward)
            ) was called
        }

        it("should remove time with more than 1 times a day") {
            val today = LocalDate.now()
            val now = Time.now()

            val habit = executeUseCase(
                TestUtil.habit.copy(
                    timesADay = 2,
                    history = mapOf(
                        today to completedEntry.copy(
                            completedAtTimes = listOf(
                                now,
                                now.plus(30)
                            )
                        )
                    )
                ),
                LocalDateTime.now()
            )
            val completedAtTimes = habit.history[today]!!.completedAtTimes
            completedAtTimes.size.`should be equal to`(1)
            completedAtTimes.first().`should equal`(now)
        }

    }
})