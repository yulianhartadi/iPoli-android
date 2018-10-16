package io.ipoli.android.habit.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.datetime.toStartOfDayUTC
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import org.amshove.kluent.`should be equal to`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/16/18.
 */
class CalculateHabitSuccessRateUseCaseSpek : Spek({
    describe("CalculateHabitSuccessRateUseCase") {
        fun habit(days: Long = 0): Habit =
        TestUtil.habit.copy(
            createdAt = LocalDate.now().minusDays(days).toStartOfDayUTC().time.instant
        )

        fun executeUseCase(
            habit: Habit,
            today: LocalDate = LocalDate.now()
        ) =
            CalculateHabitSuccessRateUseCase().execute(
                CalculateHabitSuccessRateUseCase.Params(
                    habit = habit,
                    today = today
                )
            )

        it("should be 0 when empty") {
            val res = executeUseCase(habit())
            res.`should be equal to`(0)
        }

        it("should be 100 for today") {
            val res = executeUseCase(habit().copy(
                history = mapOf(LocalDate.now() to CompletedEntry(listOf(Time.now())))
            ))
            res.`should be equal to`(100)
        }

        it("should not count should not be done days") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val res = executeUseCase(habit().copy(
                days = DayOfWeek.values().toSet() - DayOfWeek.MONDAY,
                history = mapOf(today.minusDays(1) to CompletedEntry(listOf(Time.now())))
            ), today)
            res.`should be equal to`(100)
        }

        it("should count should not be done completed days") {
            val today = LocalDate.now().with(DayOfWeek.MONDAY)
            val res = executeUseCase(habit().copy(
                days = DayOfWeek.values().toSet() - DayOfWeek.MONDAY,
                history = mapOf(
                    today.minusDays(1) to CompletedEntry(listOf(Time.now())),
                    today to CompletedEntry(listOf(Time.now()))
                )
            ), today)
            res.`should be equal to`(100)
        }
    }
})