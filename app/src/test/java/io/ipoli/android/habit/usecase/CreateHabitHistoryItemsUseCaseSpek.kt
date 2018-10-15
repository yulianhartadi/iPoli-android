package io.ipoli.android.habit.usecase

import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.instant
import io.ipoli.android.common.datetime.toStartOfDayUTC
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.usecase.CreateHabitHistoryItemsUseCase.HabitHistoryItem.State.*
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/12/18.
 */
class CreateHabitHistoryItemsUseCaseSpek : Spek({

    describe("CreateHabitHistoryItemsUseCase") {

        val habit = TestUtil.habit.copy(
            createdAt = LocalDate.now().minusDays(7).toStartOfDayUTC().time.instant
        )

        fun executeUseCase(
            habit: Habit,
            today: LocalDate = LocalDate.now(),
            startDate: LocalDate = today,
            endDate: LocalDate = today
        ) =
            CreateHabitHistoryItemsUseCase().execute(
                CreateHabitHistoryItemsUseCase.Params(
                    habit = habit,
                    startDate = startDate,
                    endDate = endDate,
                    today = today
                )
            )

        describe("Good") {

            it("should return not completed for today") {
                val today = LocalDate.now()
                val res = executeUseCase(habit, today)
                res.size.`should be equal to`(1)
                res.first().state.`should be`(NOT_COMPLETED_TODAY)
            }

            it("should return completed for today") {
                val today = LocalDate.now()
                val h = habit.copy(
                    history = mapOf(
                        today to CompletedEntry(listOf(Time.now()), TestUtil.reward)
                    )
                )
                val res = executeUseCase(h, today)
                res.size.`should be equal to`(1)
                res.first().state.`should be`(COMPLETED)
            }

            it("should return failed for yesterday") {
                val today = LocalDate.now()
                val h = habit.copy(
                    history = mapOf(
                        today to CompletedEntry(listOf(Time.now()), TestUtil.reward)
                    )
                )
                val res = executeUseCase(h, today, today.minusDays(1), today)
                res.size.`should be equal to`(2)
                res.first().state.`should be`(FAILED)
            }

            it("should return empty") {
                val today = LocalDate.now().with(DayOfWeek.MONDAY)
                val h = habit.copy(
                    days = setOf(DayOfWeek.TUESDAY)
                )
                val res = executeUseCase(h, today)
                res.size.`should be equal to`(1)
                res.first().state.`should be`(EMPTY)
            }

            it("should return empty for future") {
                val today = LocalDate.now()
                val h = habit.copy(
                    history = mapOf(
                        today to CompletedEntry(listOf(Time.now()), TestUtil.reward)
                    )
                )
                val res = executeUseCase(h, today, today, today.plusDays(1))
                res.size.`should be equal to`(2)
                res[1].state.`should be`(EMPTY)
            }

            it("should return not completed with times a day 2") {
                val today = LocalDate.now()
                val h = habit.copy(
                    timesADay = 2,
                    history = mapOf(
                        today to CompletedEntry(listOf(Time.now()))
                    )
                )
                val res = executeUseCase(h, today)
                res.first().state.`should be`(NOT_COMPLETED_TODAY)
                res.first().completedCount.`should be equal to`(1)
            }

            it("should return completed with times a day 2") {
                val today = LocalDate.now()
                val h = habit.copy(
                    timesADay = 2,
                    history = mapOf(
                        today to CompletedEntry(listOf(Time.now(), Time.now()), TestUtil.reward)
                    )
                )
                val res = executeUseCase(h, today)
                res.first().state.`should be`(COMPLETED)
            }

            it("should return failed with times a day 2") {
                val today = LocalDate.now()
                val h = habit.copy(
                    timesADay = 2,
                    history = mapOf(
                        today.minusDays(1) to CompletedEntry(listOf(Time.now()))
                    )
                )
                val res = executeUseCase(h, today, today.minusDays(1), today)
                res.first().state.`should be`(FAILED)
                res.first().completedCount.`should be equal to`(1)
            }

            it("should return empty for day before creation") {
                val today = LocalDate.now()
                val h = habit.copy(
                    history = mapOf(today.minusDays(1) to CompletedEntry(listOf(Time.now()))),
                    createdAt = today.toStartOfDayUTC().time.instant
                )
                val res = executeUseCase(h, today, today.minusDays(1), today)
                res.first().state.`should be`(COMPLETED)
                res[1].state.`should be`(NOT_COMPLETED_TODAY)
            }

            it("should return completed on empty day") {
                val today = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val h = habit.copy(
                    days = setOf(DayOfWeek.SATURDAY),
                    history = mapOf(today to CompletedEntry(completedAtTimes = listOf(Time.now())))
                )
                val res = executeUseCase(h, today)
                res.first().state.`should be`(COMPLETED)
            }

        }

        describe("Bad") {
            it("should return completed for yesterday") {
                val today = LocalDate.now()
                val h = habit.copy(
                    isGood = false
                )
                val res = executeUseCase(h, today, today.minusDays(1), today)
                res.first().state.`should be`(COMPLETED)
            }

            it("should return completed for today") {
                val today = LocalDate.now()
                val h = habit.copy(
                    isGood = false
                )
                val res = executeUseCase(h, today)
                res.first().state.`should be`(COMPLETED)
            }

            it("should return failed for yesterday") {
                val today = LocalDate.now()
                val h = habit.copy(
                    isGood = false,
                    history = mapOf(
                        today.minusDays(1) to CompletedEntry(listOf(Time.now()), TestUtil.reward)
                    )
                )
                val res = executeUseCase(h, today, today.minusDays(1), today)
                res.first().state.`should be`(FAILED)
            }


            it("should return failed for today") {
                val today = LocalDate.now()
                val h = habit.copy(
                    isGood = false,
                    history = mapOf(
                        today to CompletedEntry(listOf(Time.now()), TestUtil.reward)
                    )
                )
                val res = executeUseCase(h, today)
                res.first().state.`should be`(FAILED)
            }

            it("should return failed on empty day") {
                val today = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val h = habit.copy(
                    isGood = false,
                    days = setOf(DayOfWeek.SATURDAY),
                    history = mapOf(today to CompletedEntry(completedAtTimes = listOf(Time.now())))
                )
                val res = executeUseCase(h, today)
                res.first().state.`should be`(FAILED)
            }

        }

    }

})
