package io.ipoli.android.habit.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/20/18.
 */
class UpdateHabitStreaksUseCaseSpek : Spek({

    describe("UpdateHabitStreaksUseCase") {

        fun executeUseCase(
            habits: List<Habit>,
            date: LocalDate = LocalDate.now()
        ) =
            UpdateHabitStreaksUseCase(
                mock {
                    on { findAll() } doReturn habits
                    on { save(any<List<Habit>>()) } doAnswer { invocation ->
                        invocation.getArgument(0)
                    }
                }
            ).execute(
                UpdateHabitStreaksUseCase.Params(date)
            )

        it("should not reset good habit streak") {
            val today = LocalDate.now()
            val habits = executeUseCase(
                listOf(
                    TestUtil.habit.copy(
                        currentStreak = 3,
                        timesADay = 1,
                        history = mapOf(today to CompletedEntry(completedAtTimes = listOf(Time.now())))
                    )
                ),
                today
            )

            habits.`should be empty`()
        }

        it("should reset good habit streak") {
            val habits = executeUseCase(
                listOf(
                    TestUtil.habit.copy(
                        currentStreak = 3,
                        timesADay = 1,
                        history = emptyMap()
                    )
                )
            )

            habits.first().currentStreak.`should be`(0)
        }

        it("should reset not completed good habit streak") {
            val today = LocalDate.now()
            val habits = executeUseCase(
                listOf(
                    TestUtil.habit.copy(
                        currentStreak = 3,
                        timesADay = 2,
                        history = mapOf(today to CompletedEntry(completedAtTimes = listOf(Time.now())))
                    )
                )
            )

            habits.first().currentStreak.`should be`(0)
        }

        it("should increase bad habit streak") {
            val today = LocalDate.now()
            val habits = executeUseCase(
                listOf(
                    TestUtil.habit.copy(
                        currentStreak = 3,
                        bestStreak = 5,
                        isGood = false,
                        history = mapOf(
                            today.minusDays(1) to CompletedEntry(
                                completedAtTimes = listOf(
                                    Time.now()
                                )
                            )
                        )
                    )
                ),
                today
            )

            habits.first().currentStreak.`should be`(4)
            habits.first().bestStreak.`should be`(5)
        }

        it("should increase bad habit best streak") {
            val today = LocalDate.now()
            val habits = executeUseCase(
                listOf(
                    TestUtil.habit.copy(
                        currentStreak = 3,
                        bestStreak = 3,
                        isGood = false,
                        history = mapOf(
                            today.minusDays(1) to CompletedEntry(
                                completedAtTimes = listOf(
                                    Time.now()
                                )
                            )
                        )
                    )
                ),
                today
            )

            habits.first().currentStreak.`should be`(4)
            habits.first().bestStreak.`should be`(4)
        }

        it("should not increase bad habit streak") {
            val today = LocalDate.now()
            val habits = executeUseCase(
                listOf(
                    TestUtil.habit.copy(
                        currentStreak = 3,
                        bestStreak = 3,
                        isGood = false,
                        history = mapOf(today to CompletedEntry(completedAtTimes = listOf(Time.now())))
                    )
                ),
                today
            )

            habits.`should be empty`()
        }

        it("should not change streak if should not be done this day") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val habits = executeUseCase(
                listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        currentStreak = 3,
                        timesADay = 1,
                        history = emptyMap()
                    )
                ),
                today
            )

            habits.`should be empty`()
        }
    }

})