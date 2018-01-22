package mypoli.android.common.datetime

import java.util.concurrent.TimeUnit.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/17/2018.
 *
 * Heavily based on https://github.com/kizitonwose/Time
 */

interface TimeUnit {
    val inMillis: Long

    fun <OtherUnit : TimeUnit> conversionRate(otherTimeUnit: OtherUnit): Double {
        return inMillis.toDouble() / otherTimeUnit.inMillis.toDouble()
    }
}

class Duration<out T : TimeUnit>(value: Number, private val timeUnitFactory: () -> T) {

    companion object {
        inline operator fun <reified K : TimeUnit> invoke(value: Number) = Duration(value) {
            K::class.java.newInstance()
        }
    }

    val value = value.toDouble()

    val longValue = value.toLong()

    val intValue = longValue.toInt()

    val millisValue = asMilliseconds.longValue

    val asDays: Duration<Day>
        get() = convert()

    val asHours: Duration<Hour>
        get() = convert()

    val asMinutes: Duration<Minute>
        get() = convert()

    val asSeconds: Duration<Second>
        get() = convert()

    val asMilliseconds: Duration<Millisecond>
        get() = convert()

    private inline fun <reified OtherUnit : TimeUnit> convert(): Duration<OtherUnit> {
        val otherInstance = OtherUnit::class.java.newInstance()
        return Duration(value * timeUnitFactory().conversionRate(otherInstance))
    }

    operator fun plus(other: Duration<TimeUnit>): Duration<T> {
        val newValue =
            value + other.value * other.timeUnitFactory().conversionRate(timeUnitFactory())
        return Duration(newValue) { timeUnitFactory() }
    }

    operator fun minus(other: Duration<TimeUnit>): Duration<T> {
        val newValue =
            value - other.value * other.timeUnitFactory().conversionRate(timeUnitFactory())
        return Duration(newValue) { timeUnitFactory() }
    }

    operator fun times(other: Number) = Duration(value * other.toDouble()) { timeUnitFactory() }

    operator fun div(other: Number) = Duration(value / other.toDouble()) { timeUnitFactory() }

    operator fun inc() = Duration(value + 1, { timeUnitFactory() })

    operator fun dec() = Duration(value - 1, { timeUnitFactory() })

    operator fun compareTo(other: Duration<TimeUnit>) =
        asMilliseconds.value.compareTo(other.asMilliseconds.value)

    operator fun contains(other: Duration<TimeUnit>) =
        asMilliseconds.value >= other.asMilliseconds.value

    override operator fun equals(other: Any?): Boolean {
        if (other == null || other !is Duration<TimeUnit>) return false
        return compareTo(other) == 0
    }

    override fun hashCode() = asMilliseconds.value.hashCode()
}

class Week : TimeUnit {
    override val inMillis get() = DAYS.toMillis(7)
}

class Day : TimeUnit {
    override val inMillis = DAYS.toMillis(1)
}

class Hour : TimeUnit {
    override val inMillis get() = HOURS.toMillis(1)
}

class Minute : TimeUnit {
    override val inMillis get() = MINUTES.toMillis(1)
}

class Second : TimeUnit {
    override val inMillis get() = SECONDS.toMillis(1)
}

class Millisecond : TimeUnit {
    override val inMillis get() = 1L
}

val Number.weeks: Duration<Week>
    get() = Duration(this)

val Number.days: Duration<Day>
    get() = Duration(this)

val Number.hours: Duration<Hour>
    get() = Duration(this)

val Number.minutes: Duration<Minute>
    get() = Duration(this)

val Number.seconds: Duration<Second>
    get() = Duration(this)

val Number.milliseconds: Duration<Millisecond>
    get() = Duration(this)