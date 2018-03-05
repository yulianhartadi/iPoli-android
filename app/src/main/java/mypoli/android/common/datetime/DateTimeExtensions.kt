package mypoli.android.common.datetime

import org.threeten.bp.*
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/20/17.
 */

val LocalDate.isToday get() = LocalDate.now().isEqual(this)

val LocalDate.isTomorrow get() = LocalDate.now().plusDays(1).isEqual(this)

val LocalDate.isYesterday get() = LocalDate.now().minusDays(1).isEqual(this)

val LocalDate.dayOfWeekText: String
    get() = dayOfWeek.getDisplayName(
        TextStyle.FULL,
        Locale.getDefault()
    )

fun LocalDate.startOfDayUTC() = toStartOfDayUTC().time

fun LocalDate.toStartOfDayUTC() = fromZonedDateTime(this.atStartOfDay(DateUtils.ZONE_UTC))

fun LocalDate.toStartOfDay() = fromZonedDateTime(this.atStartOfDay(ZoneId.systemDefault()))

fun LocalDate.fromZonedDateTime(dateTime: ZonedDateTime) = Date(dateTime.toInstant().toEpochMilli())

fun LocalDate.isBetween(start: LocalDate?, end: LocalDate?): Boolean {
    return if (start == null || end == null) {
        false
    } else !isBefore(start) && !isAfter(end)
}

fun LocalDate.isBeforeOrEqual(date: LocalDate) = !isAfter(date)
fun LocalDate.isAfterOrEqual(date: LocalDate) = !isBefore(date)

fun LocalDate.isNotEqual(otherDate: LocalDate) = !isEqual(otherDate)

fun LocalDate.datesBetween(date: LocalDate): List<LocalDate> {
    val days = ChronoUnit.DAYS.between(this, date)
    return (0..days).map { this.plusDays(it) }
}

fun LocalDate.daysUntil(date: LocalDate) =
    this.until(date, ChronoUnit.DAYS)

fun LocalDate.weeksUntil(date: LocalDate) =
    this.until(date, ChronoUnit.WEEKS)

fun LocalDateTime.toMillis(zoneId: ZoneId) = atZone(zoneId).toInstant().toEpochMilli()

fun LocalDateTime.toMillis() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

operator fun Instant.plus(other: Instant): Instant {
    return Instant.ofEpochMilli(toEpochMilli() + other.toEpochMilli())
}

operator fun Instant.minus(other: Instant): Instant {
    return Instant.ofEpochMilli(toEpochMilli() - other.toEpochMilli())
}

fun Instant.plusMinutes(minutes: Long): Instant = plus(minutes, ChronoUnit.MINUTES)

val Instant.milliseconds get() = toEpochMilli().milliseconds

val Long.instant: Instant get() = Instant.ofEpochMilli(this)

val Long.startOfDayUTC: LocalDate get() = DateUtils.fromMillis(this)