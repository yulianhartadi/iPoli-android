package io.ipoli.android.common.datetime

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/20/17.
 */
data class Time constructor(private val minutesAfterMidnight: Int) {

    fun toMinuteOfDay(): Int {
        return minutesAfterMidnight
    }

    fun toMillisOfDay(): Long {
        return TimeUnit.MINUTES.toMillis(minutesAfterMidnight.toLong())
    }

    val hours: Int
        get() = TimeUnit.MINUTES.toHours(minutesAfterMidnight.toLong()).toInt()

    fun getMinutes(): Int {
        return minutesAfterMidnight - hours * 60
    }

    override fun toString(): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hours, getMinutes())
    }

    fun toString(use24HourFormat: Boolean): String {
        val c = Calendar.getInstance()
        c.set(Calendar.MINUTE, getMinutes())
        c.set(Calendar.HOUR_OF_DAY, hours)

        var format = "HH:mm"
        if (!use24HourFormat) {
            format = if (getMinutes() > 0) "h:mm a" else "h a"
        }
        return SimpleDateFormat(format, Locale.getDefault()).format(c.time)
    }

    companion object {

        val MINUTES_IN_AN_HOUR = 60
        val MINUTES_IN_A_DAY = 24 * MINUTES_IN_AN_HOUR

        fun of(minutesAfterMidnight: Int): Time {

            val minutes = if (minutesAfterMidnight < 0) {
                MINUTES_IN_A_DAY + minutesAfterMidnight
            } else {
                minutesAfterMidnight % MINUTES_IN_A_DAY
            }

            return Time(minutes)
        }

        fun at(timeString: String): Time {
            return at(parseHours(timeString), parseMinutes(timeString))
        }

        fun at(hours: Int, minutes: Int): Time {
            return of(hours * 60 + minutes)
        }

        fun atHours(hours: Int): Time {
            return at(hours, 0)
        }

        fun after(hours: Int, minutes: Int): Time {
            val c = Calendar.getInstance()
            c.add(Calendar.HOUR_OF_DAY, hours)
            c.add(Calendar.MINUTE, minutes)
            return at(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }

        fun ago(hours: Int, minutes: Int): Time {
            val c = Calendar.getInstance()
            c.add(Calendar.HOUR_OF_DAY, -hours)
            c.add(Calendar.MINUTE, -minutes)
            return at(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }

        fun afterHours(hours: Int): Time {
            return after(hours, 0)
        }

        fun afterMinutes(minutes: Int): Time {
            return after(0, minutes)
        }

        fun hoursAgo(hours: Int): Time {
            return ago(hours, 0)
        }

        fun minutesAgo(minutes: Int): Time {
            return ago(0, minutes)
        }

        private fun parseHours(time: String): Int {
            val pieces = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return Integer.parseInt(pieces[0])
        }

        private fun parseMinutes(time: String): Int {
            val pieces = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return Integer.parseInt(pieces[1])
        }

        fun now(): Time {
            val c = Calendar.getInstance()
            return at(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }

        fun of(date: Date?): Time? {
            if (date == null) {
                return null
            }
            val c = Calendar.getInstance()
            c.time = date
            return at(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }

        fun plusMinutes(time: Time, minutes: Int): Time {
            return of(time.minutesAfterMidnight + minutes)
        }

        fun h2Min(hours: Int): Int {
            return TimeUnit.HOURS.toMinutes(hours.toLong()).toInt()
        }
    }

    operator fun plus(minutes: Int): Time {
        return Time((this.minutesAfterMidnight + minutes) % MINUTES_IN_A_DAY)
    }

    operator fun plus(time: Time): Time {
        return Time((minutesAfterMidnight + time.minutesAfterMidnight) % MINUTES_IN_A_DAY)
    }

    operator fun minus(time: Time): Time {
        return Time((minutesAfterMidnight - time.minutesAfterMidnight) % MINUTES_IN_A_DAY)
    }

    operator fun minus(minutes: Int): Time {
        return Time((this.minutesAfterMidnight - minutes) % MINUTES_IN_A_DAY)
    }

    operator fun compareTo(time: Time) =
        minutesAfterMidnight.compareTo(time.minutesAfterMidnight)

    /**
     * @param start inclusive
     * @param end inclusive
     */
    fun isBetween(start: Time, end: Time): Boolean {
        if (minutesAfterMidnight >= start.minutesAfterMidnight && minutesAfterMidnight <= end.minutesAfterMidnight) {
            return true
        }

        if (start.minutesAfterMidnight > end.minutesAfterMidnight) {
            val isBetweenStartAndMidnight = isBetween(start, Time.at(23, 59))
            val isBetweenMidnightAndEnd = isBetween(Time.of(0), end)
            return isBetweenStartAndMidnight || isBetweenMidnightAndEnd
        }
        return false
    }

    fun minutesTo(time: Time): Int {
        return if (minutesAfterMidnight > time.minutesAfterMidnight) {
            MINUTES_IN_A_DAY - minutesAfterMidnight + time.minutesAfterMidnight
        } else {
            time.minutesAfterMidnight - minutesAfterMidnight
        }
    }
}

enum class TimePreference {
    WORK_HOURS, PERSONAL_HOURS, MORNING, AFTERNOON, EVENING, ANY
}