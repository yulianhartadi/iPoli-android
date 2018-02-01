package mypoli.android.quest.schedule.calendar.dayview

import mypoli.android.common.datetime.Time
import mypoli.android.quest.schedule.calendar.dayview.view.widget.util.PositionToTimeMapper
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/6/17.
 */
class PositionToTimeMapperSpek : Spek({

    describe("A PositionToTimeMapper") {
        val minuteHeight = 4.983333f
        val positionToTimeMapper = PositionToTimeMapper(minuteHeight)

        it("should give first minute of day") {
            positionToTimeMapper.timeAt(-12f) `should equal` Time.of(0)
        }

        it("should give last minute of day") {
            positionToTimeMapper.timeAt(25 * 60 * minuteHeight) `should equal` Time.of(23 * 60 + 59)
        }

        it("should round to lower number") {
            positionToTimeMapper.timeAt(122 * minuteHeight) `should equal` Time.atHours(2)
        }

        it("should round to higher number") {
            positionToTimeMapper.timeAt(123 * minuteHeight) `should equal` Time.at(2, 5)
        }

        it("should round to whole hour") {
            positionToTimeMapper.timeAt(1183f) `should equal` Time.atHours(4)
        }
    }
})