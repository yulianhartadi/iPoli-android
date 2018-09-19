package io.ipoli.android.habit.usecase

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.toLocalDateTime
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/20/18.
 */
class UpdateHabitStreaksUseCaseSpek : Spek({

    describe("UpdateHabitStreaksUseCase") {

        fun executeUseCase(
            habits: List<Habit>,
            today: LocalDateTime = LocalDateTime.now(),
            resetDayTime: Time = Time.of(0)
        ) =
            UpdateHabitStreaksUseCase(
                mock {
                    on { findAllNotRemoved() } doReturn habits
                    on { save(any<List<Habit>>()) } doAnswer { invocation ->
                        invocation.getArgument(0)
                    }
                }
            ).execute(
                UpdateHabitStreaksUseCase.Params(today, resetDayTime)
            )

        it("should not reset good habit streak") {
            val today = LocalDate.now()
            val habits = executeUseCase(
                listOf(
                    TestUtil.habit.copy(
                        currentStreak = 3,
                        timesADay = 1,
                        history = mapOf(
                            today.minusDays(1) to CompletedEntry(
                                completedAtTimes = listOf(
                                    Time.atHours(10)
                                )
                            )
                        )
                    )
                ),
                today.toLocalDateTime()
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
                            today.minusDays(2) to CompletedEntry(
                                completedAtTimes = listOf(
                                    Time.now()
                                )
                            )
                        )
                    )
                ),
                today.toLocalDateTime()
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
                            today.minusDays(2) to CompletedEntry(
                                completedAtTimes = listOf(
                                    Time.now()
                                )
                            )
                        )
                    )
                ),
                today.toLocalDateTime()
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
                        history = mapOf(
                            today.minusDays(1) to CompletedEntry(
                                completedAtTimes = listOf(
                                    Time.now()
                                )
                            )
                        )
                    )
                ),
                today.toLocalDateTime()
            )

            habits.`should be empty`()
        }

        it("should not change streak if should not be done this day") {
            val today = LocalDate.now().with(DayOfWeek.TUESDAY)
            val habits = executeUseCase(
                listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        currentStreak = 3,
                        timesADay = 1,
                        history = emptyMap()
                    )
                ),
                today.toLocalDateTime()
            )

            habits.`should be empty`()
        }

        it("should not update streak when reset day time is 3:00 and habit is completed at 23:00 on previous day") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val resetDayTime = Time.atHours(3)

            val habits = executeUseCase(
                habits = listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        currentStreak = 0,
                        timesADay = 1,
                        history = mapOf(
                            today.minusDays(1)
                                to
                                CompletedEntry(
                                    completedAtTimes = listOf(Time.atHours(23))
                                )
                        )
                    )
                ),
                today = today.toLocalDateTime(),
                resetDayTime = resetDayTime
            )

            habits.`should be empty`()
        }

        it("should not update streak when reset day time is 3:00 and habit is completed at 1:00 on same day") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val resetDayTime = Time.atHours(3)

            val habits = executeUseCase(
                habits = listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        currentStreak = 0,
                        timesADay = 1,
                        history = mapOf(
                            today
                                to
                                CompletedEntry(
                                    completedAtTimes = listOf(Time.atHours(1))
                                )
                        )
                    )
                ),
                today = today.toLocalDateTime(),
                resetDayTime = resetDayTime
            )

            habits.`should be empty`()
        }

        it("should not update streak when reset day time is 23:00 and habit is completed at 22:00 on same day") {
            val today = LocalDate.now().with(DayOfWeek.SUNDAY)
            val resetDayTime = Time.atHours(23)

            val habits = executeUseCase(
                habits = listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        currentStreak = 0,
                        timesADay = 1,
                        history = mapOf(
                            today
                                to
                                CompletedEntry(
                                    completedAtTimes = listOf(Time.atHours(22))
                                )
                        )
                    )
                ),
                today = today.toLocalDateTime(),
                resetDayTime = resetDayTime
            )

            habits.`should be empty`()
        }

        it("should not update streak when reset day time is 23:00 and habit is completed at 23:30 on previous day") {
            val today = LocalDate.now().with(DayOfWeek.SUNDAY)
            val resetDayTime = Time.atHours(23)

            val habits = executeUseCase(
                habits = listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        currentStreak = 0,
                        timesADay = 1,
                        history = mapOf(
                            today.minusDays(1)
                                to
                                CompletedEntry(
                                    completedAtTimes = listOf(Time.at(23, 30))
                                )
                        )
                    )
                ),
                today = today.toLocalDateTime(),
                resetDayTime = resetDayTime
            )

            habits.`should be empty`()
        }

        it("should not update streak when reset day time is 12:00 and habit is completed at 13:00 on same day") {
            val today = LocalDate.now().with(DayOfWeek.SUNDAY)
            val resetDayTime = Time.atHours(12)

            val habits = executeUseCase(
                habits = listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        currentStreak = 0,
                        timesADay = 1,
                        history = mapOf(
                            today
                                to
                                CompletedEntry(
                                    completedAtTimes = listOf(Time.atHours(13))
                                )
                        )
                    )
                ),
                today = today.plusDays(1).toLocalDateTime(Time.at(12, 30)),
                resetDayTime = resetDayTime
            )

            habits.`should be empty`()
        }

        it("should not update streak when reset day time is 00:00 and habit is completed at 13:00 on same day") {
            val today = LocalDate.now().with(DayOfWeek.SUNDAY)
            val resetDayTime = Time.atHours(0)

            val habits = executeUseCase(
                habits = listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        currentStreak = 0,
                        timesADay = 1,
                        history = mapOf(
                            today
                                to
                                CompletedEntry(
                                    completedAtTimes = listOf(Time.atHours(13))
                                )
                        )
                    )
                ),
                today = today.toLocalDateTime(),
                resetDayTime = resetDayTime
            )

            habits.`should be empty`()
        }

        it("should reset streak when reset day time is 3:00 and habit is not completed") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val resetDayTime = Time.atHours(3)

            val habits = executeUseCase(
                habits = listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        isGood = true,
                        currentStreak = 4,
                        timesADay = 1,
                        history = emptyMap()
                    )
                ),
                today = today.toLocalDateTime(Time.atHours(4)),
                resetDayTime = resetDayTime
            )

            habits.size.`should be`(1)
            habits.first().currentStreak.`should be`(0)
        }

        it("should reset streak when reset day time is 23:00 and habit is not completed") {
            val today = LocalDate.now().with(DayOfWeek.SUNDAY)
            val resetDayTime = Time.atHours(23)

            val habits = executeUseCase(
                habits = listOf(
                    TestUtil.habit.copy(
                        days = setOf(DayOfWeek.SUNDAY),
                        isGood = true,
                        currentStreak = 4,
                        timesADay = 1,
                        history = emptyMap()
                    )
                ),
                today = today.toLocalDateTime(Time.atHours(23)),
                resetDayTime = resetDayTime
            )

            habits.size.`should be`(1)
            habits.first().currentStreak.`should be`(0)
        }
    }

})