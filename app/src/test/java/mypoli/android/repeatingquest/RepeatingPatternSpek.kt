package mypoli.android.repeatingquest

import mypoli.android.common.datetime.DateUtils
import mypoli.android.repeatingquest.entity.RepeatingPattern
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be true`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.threeten.bp.LocalDate

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

        describe("Daily") {

            it("should give today for next date") {
                val pattern = RepeatingPattern.Daily(DateUtils.today)
                val nextDate = pattern.nextDate(DateUtils.today)
                shouldHaveNextDate(nextDate!!, DateUtils.today)
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
        }
    }
})