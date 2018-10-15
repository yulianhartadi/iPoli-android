package io.ipoli.android.habit.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

class CalculateHabitStreakUseCaseSpek : Spek({

    describe("CalculateHabitStreakUseCase") {

        fun streakFor(habit: Habit) =
            CalculateHabitStreakUseCase()
                .execute(CalculateHabitStreakUseCase.Params(habit))

        fun streakOf(current: Int, best: Int) =
            Habit.Streak(
                current = current,
                best = best
            )

        describe("Good") {

            it("should have no current & best streak without history") {
                val h = TestUtil.habit.copy(
                    timesADay = 1,
                    history = emptyMap()
                )
                streakFor(h).`should equal`(streakOf(0, 0))
            }

            it("should have current & best streak of 1") {

                val h = TestUtil.habit.copy(
                    timesADay = 1,
                    history = mapOf(
                        LocalDate.now() to CompletedEntry(listOf(Time.now()))
                    )
                )
                streakFor(h).`should equal`(streakOf(1, 1))
            }

            it("should have no current streak & best of 1") {

                val h = TestUtil.habit.copy(
                    timesADay = 1,
                    history = mapOf(
                        LocalDate.now().minusDays(1) to CompletedEntry(listOf(Time.now()))
                    )
                )
                streakFor(h).`should equal`(streakOf(0, 1))
            }

            it("should have current of 2 & best of 3") {

                val today = LocalDate.now()
                val h = TestUtil.habit.copy(
                    timesADay = 1,
                    history = mapOf(
                        today to CompletedEntry(listOf(Time.now())),
                        today.minusDays(1) to CompletedEntry(listOf(Time.now())),
                        today.minusDays(5) to CompletedEntry(listOf(Time.now())),
                        today.minusDays(6) to CompletedEntry(listOf(Time.now())),
                        today.minusDays(7) to CompletedEntry(listOf(Time.now()))
                    )
                )
                streakFor(h).`should equal`(streakOf(2, 3))
            }
        }

        describe("Bad") {

            it("should have current & best streak of 1") {
                val h = TestUtil.habit.copy(
                    isGood = false,
                    timesADay = 1,
                    history = emptyMap()
                )
                streakFor(h).`should equal`(streakOf(1, 1))
            }

            it("should have current & best streak of 0") {
                val today = LocalDate.now()
                val h = TestUtil.habit.copy(
                    isGood = false,
                    timesADay = 1,
                    history = mapOf(
                        today to CompletedEntry(completedAtTimes = listOf(Time.now()))
                    )
                )
                streakFor(h).`should equal`(streakOf(0, 0))
            }

            it("should have current of 0 & best streak of 1") {
                val today = LocalDate.now()
                val h = TestUtil.habit.copy(
                    isGood = false,
                    timesADay = 1,
                    createdAt = today.minusDays(1).startOfDayUTC().instant,
                    history = mapOf(
                        today to CompletedEntry(completedAtTimes = listOf(Time.now()))
                    )
                )
                streakFor(h).`should equal`(streakOf(0, 1))
            }

            it("should have current of 2 & best streak of 2") {
                val today = LocalDate.now()
                val h = TestUtil.habit.copy(
                    isGood = false,
                    timesADay = 1,
                    createdAt = today.minusDays(1).startOfDayUTC().instant,
                    history = emptyMap()
                )
                streakFor(h).`should equal`(streakOf(2, 2))
            }

            it("should have current of 1 & best streak of 3") {
                val today = LocalDate.now()
                val h = TestUtil.habit.copy(
                    isGood = false,
                    timesADay = 1,
                    createdAt = today.minusDays(4).startOfDayUTC().instant,
                    history = mapOf(
                        today.minusDays(1) to CompletedEntry(completedAtTimes = listOf(Time.now()))
                    )
                )
                streakFor(h).`should equal`(streakOf(1, 3))
            }

        }
    }

})