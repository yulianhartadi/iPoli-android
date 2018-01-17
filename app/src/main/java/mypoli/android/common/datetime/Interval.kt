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

class Interval<out T : TimeUnit>(value: Number, private val timeUnitFactory: () -> T) {

    companion object {
        inline operator fun <reified K : TimeUnit> invoke(value: Number) = Interval(value) {
            K::class.java.newInstance()
        }
    }

    val value = value.toDouble()

    val longValue = value.toLong()

    val asDays: Interval<Day>
        get() = convert()

    val asHours: Interval<Hour>
        get() = convert()

    val asMinutes: Interval<Minute>
        get() = convert()

    val asSeconds: Interval<Second>
        get() = convert()

    val asMilliseconds: Interval<Millisecond>
        get() = convert()

    private inline fun <reified OtherUnit : TimeUnit> convert(): Interval<OtherUnit> {
        val otherInstance = OtherUnit::class.java.newInstance()
        return Interval(value * timeUnitFactory().conversionRate(otherInstance))
    }

    operator fun plus(other: Interval<TimeUnit>): Interval<T> {
        val newValue = value + other.value * other.timeUnitFactory().conversionRate(timeUnitFactory())
        return Interval(newValue) { timeUnitFactory() }
    }

    operator fun minus(other: Interval<TimeUnit>): Interval<T> {
        val newValue = value - other.value * other.timeUnitFactory().conversionRate(timeUnitFactory())
        return Interval(newValue) { timeUnitFactory() }
    }

    operator fun times(other: Number) = Interval(value * other.toDouble()) { timeUnitFactory() }

    operator fun div(other: Number) = Interval(value / other.toDouble()) { timeUnitFactory() }

    operator fun inc() = Interval(value + 1, { timeUnitFactory() })

    operator fun dec() = Interval(value - 1, { timeUnitFactory() })

    operator fun compareTo(other: Interval<TimeUnit>) =
        asMilliseconds.value.compareTo(other.asMilliseconds.value)

    operator fun contains(other: Interval<TimeUnit>) =
        asMilliseconds.value >= other.asMilliseconds.value

    override operator fun equals(other: Any?): Boolean {
        if (other == null || other !is Interval<TimeUnit>) return false
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

val Number.weeks: Interval<Week>
    get() = Interval(this)

val Number.days: Interval<Day>
    get() = Interval(this)

val Number.hours: Interval<Hour>
    get() = Interval(this)

val Number.minutes: Interval<Minute>
    get() = Interval(this)

val Number.seconds: Interval<Second>
    get() = Interval(this)

val Number.milliseconds: Interval<Millisecond>
    get() = Interval(this)