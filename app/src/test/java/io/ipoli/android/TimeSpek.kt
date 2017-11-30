package io.ipoli.android

import io.ipoli.android.common.datetime.Time
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/30/17.
 */
class TimeSpek : Spek({

    describe("Time") {

        describe("is between") {

            it("should include start time") {
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

            it("should be inside interval when all times are equal") {
                val result = Time.atHours(23).isBetween(Time.atHours(23), Time.atHours(23))
                result.`should be true`()
            }

            it("should be outside interval when start & end times are equal") {
                val result = Time.atHours(22).isBetween(Time.atHours(23), Time.atHours(23))
                result.`should be false`()
            }
        }

    }
})