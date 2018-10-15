package io.ipoli.android.habit

import io.ipoli.android.TestUtil
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.habit.data.CompletedEntry
import io.ipoli.android.habit.data.Habit
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/13/2018.
 */
class HabitSpek : Spek({

    describe("Habit") {

        describe("shouldBeDoneOn") {

            it("should be done when having only original days") {
                val today = LocalDate.now().with(DayOfWeek.MONDAY)
                val days = setOf(DayOfWeek.MONDAY)
                val h = TestUtil.habit.copy(
                    days = days,
                    preferenceHistory = Habit.PreferenceHistory(
                        days = sortedMapOf(today to days),
                        timesADay = TestUtil.habit.preferenceHistory.timesADay
                    )
                )
                h.shouldBeDoneOn(today).`should be true`()
            }

            it("should not be done when having only original days") {
                val today = LocalDate.now().with(DayOfWeek.TUESDAY)
                val days = setOf(DayOfWeek.MONDAY)
                val h = TestUtil.habit.copy(
                    days = days,
                    preferenceHistory = Habit.PreferenceHistory(
                        days = sortedMapOf(today to days),
                        timesADay = TestUtil.habit.preferenceHistory.timesADay
                    )
                )
                h.shouldBeDoneOn(today).`should be false`()
            }

            it("should be done based on history") {
                val today = LocalDate.now()
                val days = setOf(today.dayOfWeek)
                val yesterday = today.minusDays(1)
                val yesterdayDays = setOf(yesterday.dayOfWeek)
                val h = TestUtil.habit.copy(
                    days = days,
                    preferenceHistory = Habit.PreferenceHistory(
                        days = sortedMapOf(
                            yesterday to yesterdayDays,
                            today to days
                        ),
                        timesADay = TestUtil.habit.preferenceHistory.timesADay
                    )
                )
                h.shouldBeDoneOn(yesterday).`should be true`()
            }

            it("should not be done based on history") {
                val today = LocalDate.now()
                val days = setOf(today.dayOfWeek)
                val yesterday = today.minusDays(1)
                val yesterdayDays = setOf(yesterday.dayOfWeek)
                val h = TestUtil.habit.copy(
                    days = days,
                    preferenceHistory = Habit.PreferenceHistory(
                        days = sortedMapOf(
                            yesterday to yesterdayDays,
                            today to days
                        ),
                        timesADay = TestUtil.habit.preferenceHistory.timesADay
                    )
                )
                h.shouldBeDoneOn(yesterday.minusDays(1)).`should be false`()
            }

            it("should use first history entry for old date") {
                val today = LocalDate.now()
                val days = setOf(today.dayOfWeek)
                val yesterday = today.minusDays(1)
                val yesterdayDays = setOf(yesterday.dayOfWeek)
                val h = TestUtil.habit.copy(
                    days = days,
                    preferenceHistory = Habit.PreferenceHistory(
                        days = sortedMapOf(
                            yesterday to yesterdayDays,
                            today to days
                        ),
                        timesADay = TestUtil.habit.preferenceHistory.timesADay
                    )
                )
                h.shouldBeDoneOn(yesterday.minusWeeks(4)).`should be true`()
            }
        }

        describe("isCompletedOrShouldNotBeDoneForDate") {

            it("should be completed when having only original days") {
                val today = LocalDate.now().with(DayOfWeek.MONDAY)
                val timesADay = 1
                val h = TestUtil.habit.copy(
                    days = setOf(today.dayOfWeek),
                    timesADay = timesADay,
                    preferenceHistory = TestUtil.habit.preferenceHistory.copy(
                        timesADay = sortedMapOf(
                            today to timesADay
                        ),
                        days = sortedMapOf(today to setOf(today.dayOfWeek))
                    ),
                    history = mapOf(
                        today to CompletedEntry(listOf(Time.now()))
                    )
                )
                h.isCompletedForDate(today).`should be true`()
            }

            it("should be completed based on history") {
                val today = LocalDate.now().with(DayOfWeek.MONDAY)
                val timesADay = 3
                val yesterday = today.minusDays(1)
                val yesterdayTimesADay = 1
                val h = TestUtil.habit.copy(
                    timesADay = timesADay,
                    preferenceHistory = TestUtil.habit.preferenceHistory.copy(
                        timesADay = sortedMapOf(
                            yesterday to yesterdayTimesADay,
                            today to timesADay
                        )
                    ),
                    history = mapOf(
                        yesterday to CompletedEntry(
                            listOf(
                                Time.now()
                            )
                        ),
                        today to CompletedEntry(
                            listOf(
                                Time.now(),
                                Time.now(),
                                Time.now()
                            )
                        )
                    )
                )
                h.isCompletedForDate(yesterday).`should be true`()
            }

        }
    }
})