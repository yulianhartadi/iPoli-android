package mypoli.android.quest.schedule.calendar.dayview.view.widget.util

import mypoli.android.common.datetime.Time

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/6/17.
 */
class PositionToTimeMapper(private val minuteHeight: Float) {

    fun timeAt(yPosition: Float, roundedToMinutes: Int = 5): Time {
        val minutes = getMinutesFor(yPosition)
        val remainder = minutes % roundedToMinutes.toFloat()
        val minutesAfterMidnight = if (remainder >= cutoff(roundedToMinutes)) {
            minutes + roundedToMinutes - remainder
        } else {
            minutes - remainder
        }
        return Time.of(Math.min(minutesAfterMidnight.toInt(), 23 * 60 + 59))
    }

    private fun cutoff(roundedToMinutes: Int): Float =
        Math.floor(roundedToMinutes.toDouble() / 2).toFloat()

    private fun getMinutesFor(height: Float): Float =
        height / minuteHeight
}