package mypoli.android.repeatingquest

import mypoli.android.common.datetime.DateUtils
import mypoli.android.repeatingquest.entity.RepeatingPattern
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be true`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.TemporalAdjusters

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/25/2018.
 */

class RepeatingPatternSpek : Spek({

    describe("RepeatingPattern") {

        fun shouldHaveNextDate(nextDate: LocalDate?, date: LocalDate?) {

            if (date == null) {
                nextDate.`should be null`()
                return
            }

            date.isEqual(nextDate).`should be true`()
        }

        it("should not return before start") {
            val pattern = RepeatingPattern.Daily(DateUtils.today)
            val nextDate = pattern.nextDate(DateUtils.today.minusDays(1))
            shouldHaveNextDate(nextDate!!, DateUtils.today)
        }

        it("should not return after end") {
            val pattern = RepeatingPattern.Daily(DateUtils.today, DateUtils.today.plusDays(1))
            val nextDate = pattern.nextDate(DateUtils.today.plusDays(2))
            shouldHaveNextDate(nextDate, null)
        }

        describe("Daily") {

            it("should give today for next date") {
                val pattern = RepeatingPattern.Daily(DateUtils.today)
                val nextDate = pattern.nextDate(DateUtils.today)
                shouldHaveNextDate(nextDate!!, DateUtils.today)
            }
        }

        describe("Weekly") {

            describe("Fixed") {

                it("should give today when today is monday and monday is chosen day") {
                    val monday = DateUtils.today.with(DayOfWeek.MONDAY)
                    val pattern = RepeatingPattern.Weekly(
                        setOf(DayOfWeek.MONDAY),
                        monday
                    )
                    shouldHaveNextDate(pattern.nextDate(monday), monday)
                }

                it("should find date after 2 days when today is monday and wednesday is chosen day") {
                    val today =
                        DateUtils.today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val wednesday = DateUtils.today.with(DayOfWeek.WEDNESDAY)
                    val pattern = RepeatingPattern.Weekly(
                        setOf(DayOfWeek.WEDNESDAY),
                        today
                    )
                    shouldHaveNextDate(pattern.nextDate(today), wednesday)
                }

                it("should find date considering day of week ordering") {
                    val today =
                        DateUtils.today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val tuesday = DateUtils.today.with(DayOfWeek.TUESDAY)
                    val pattern = RepeatingPattern.Weekly(
                        setOf(DayOfWeek.WEDNESDAY, DayOfWeek.TUESDAY),
                        today
                    )
                    shouldHaveNextDate(pattern.nextDate(today), tuesday)
                }
            }

            describe("Flexible") {

                it("should give today when today is first day of week") {
                    val today =
                        DateUtils.today.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))

                    val pattern = RepeatingPattern.Flexible.Weekly(
                        timesPerWeek = 2,
                        preferredDays = setOf(),
                        scheduledPeriods = mapOf(
                            today to listOf(today)
                        ),
                        start = today
                    )

                    shouldHaveNextDate(pattern.nextDate(today), today)
                }

                it("should give tomorrow when today is not scheduled") {

                    val today =
                        DateUtils.today.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))

                    val tomorrow = today.plusDays(1)

                    val pattern = RepeatingPattern.Flexible.Weekly(
                        timesPerWeek = 2,
                        preferredDays = setOf(),
                        scheduledPeriods = mapOf(
                            today to listOf(tomorrow)
                        ),
                        start = today
                    )

                    shouldHaveNextDate(pattern.nextDate(today), tomorrow)
                }
            }
        }
    }
})