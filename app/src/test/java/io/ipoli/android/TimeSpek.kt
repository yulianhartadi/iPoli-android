package io.ipoli.android

import io.ipoli.android.common.datetime.Time
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 11/30/17.
 */
class TimeSpek : Spek({

    describe("Time") {

        describe("is between") {

            it("should include startDate time") {
                val result = Time.atHours(9).isBetween(Time.atHours(9), Time.atHours(10))
                result.`should be true`()
            }

            it("should include end time") {
                val result = Time.atHours(9).isBetween(Time.atHours(8), Time.atHours(9))
                result.`should be true`()
            }

            it("should be inside time interval") {
                val result = Time.atHours(9).isBetween(Time.atHours(8), Time.atHours(10))
                result.`should be true`()
            }

            it("should be outside time interval") {
                val result = Time.atHours(7).isBetween(Time.atHours(8), Time.atHours(10))
                result.`should be false`()
            }

            it("should wrap around 00:00") {
                val result1 = Time.atHours(23).isBetween(Time.atHours(22), Time.atHours(9))
                val result2 = Time.atHours(6).isBetween(Time.atHours(22), Time.atHours(9))
                val result3 = Time.atHours(21).isBetween(Time.atHours(22), Time.atHours(9))
                val result4 = Time.atHours(10).isBetween(Time.atHours(22), Time.atHours(9))
                result1.`should be true`()
                result2.`should be true`()
                result3.`should be false`()
                result4.`should be false`()
            }

            it("should be inside interval when all times are same") {
                val result = Time.atHours(23).isBetween(Time.atHours(23), Time.atHours(23))
                result.`should be true`()
            }

            it("should be outside interval when startDate & end times are same") {
                val result = Time.atHours(22).isBetween(Time.atHours(23), Time.atHours(23))
                result.`should be false`()
            }

        }

        describe("minutes between") {

            it("should have 0 minutes between same times") {
                Time.atHours(22).minutesTo(Time.atHours(22)).`should be equal to`(0)
            }

            it("should have 1 minute between times") {
                Time.atHours(22).minutesTo(Time.at(22, 1)).`should be equal to`(1)
            }

            it("should have 120 minutes to midnight") {
                Time.atHours(22).minutesTo(Time.atHours(0)).`should be equal to`(120)
            }

            it("should have 240 minutes to 2 a.m.") {
                Time.atHours(22).minutesTo(Time.atHours(2)).`should be equal to`(240)
            }
        }

    }
})