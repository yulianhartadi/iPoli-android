package io.ipoli.android.common.datetime

import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import java.util.*

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */
fun LocalDate.startOfDayUTC(): Long {
    return toStartOfDayUTC().time
}

fun LocalDate.toStartOfDayUTC(): Date {
    return fromZonedDateTime(this.atStartOfDay(DateUtils.ZONE_UTC))
}

fun LocalDate.fromZonedDateTime(dateTime: ZonedDateTime): Date {
    return Date(dateTime.toInstant().toEpochMilli())
}

fun LocalDate.isBetween(start: LocalDate?, end: LocalDate?): Boolean {
    return if (start == null || end == null) {
        false
    } else !isBefore(start) && !isAfter(end)
}