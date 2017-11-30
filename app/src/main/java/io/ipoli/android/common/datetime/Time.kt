package io.ipoli.android.common.datetime

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */
data class Time constructor(private val minutesAfterMidnight: Int) {
    private val minutes: Int

    init {
        if (minutesAfterMidnight < 0) {
            throw IllegalArgumentException("Minutes must be >= 0. It was: " + minutesAfterMidnight)
        }
        this.minutes = minutesAfterMidnight % MINUTES_IN_A_DAY
    }

    private constructor(hours: Int, minutes: Int) : this(hours * 60 + minutes)

    fun toMinuteOfDay(): Int {
        return minutes
    }

    fun toMillisOfDay(): Long {
        return TimeUnit.MINUTES.toMillis(minutes.toLong())
    }

    val hours: Int
        get() = TimeUnit.MINUTES.toHours(minutes.toLong()).toInt()

    fun getMinutes(): Int {
        return minutes - hours * 60
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
            return Time(minutesAfterMidnight)
        }

        fun at(timeString: String): Time {
            return Time(parseHours(timeString), parseMinutes(timeString))
        }

        fun at(hours: Int, minutes: Int): Time {
            return Time(hours, minutes)
        }

        fun atHours(hours: Int): Time {
            return at(hours, 0)
        }

        fun after(hours: Int, minutes: Int): Time {
            val c = Calendar.getInstance()
            c.add(Calendar.HOUR_OF_DAY, hours)
            c.add(Calendar.MINUTE, minutes)
            return Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }

        fun ago(hours: Int, minutes: Int): Time {
            val c = Calendar.getInstance()
            c.add(Calendar.HOUR_OF_DAY, -hours)
            c.add(Calendar.MINUTE, -minutes)
            return Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
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
            return Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }

        fun of(date: Date?): Time? {
            if (date == null) {
                return null
            }
            val c = Calendar.getInstance()
            c.time = date
            return Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }

        fun plusMinutes(time: Time, minutes: Int): Time {
            return Time.of(time.minutes + minutes)
        }

        fun h2Min(hours: Int): Int {
            return TimeUnit.HOURS.toMinutes(hours.toLong()).toInt()
        }

        fun minutesBetween(endTime: Time?, startTime: Time?): Int? {
            if (endTime == null || startTime == null) {
                return null
            }
            return endTime.minutesAfterMidnight - startTime.minutesAfterMidnight
        }
    }

    fun isBetween(start: Time, end: Time): Boolean {
        if (minutes >= start.minutes && minutes <= end.minutes) {
            return true
        }

        if (start.minutes > end.minutes) {
            val isBetweenStartAndMidnight = isBetween(start, Time.at(23, 59))
            val isBetweenMidnightAndEnd = isBetween(Time.of(0), end)
            return isBetweenStartAndMidnight || isBetweenMidnightAndEnd
        }
        return false
    }
}

enum class TimePreference {
    WORK_HOURS, PERSONAL_HOURS, MORNING, AFTERNOON, EVENING, ANY
}