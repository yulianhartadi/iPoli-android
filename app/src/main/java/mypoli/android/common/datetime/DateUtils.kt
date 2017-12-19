package mypoli.android.common.datetime

import android.util.Pair
import org.threeten.bp.*
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters.firstDayOfMonth
import org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/20/17.
 */
object DateUtils {

    val ZONE_UTC = ZoneId.of("UTC")

    /**
     * @param dateMillis in UTC timezone
     * @return LocalDate
     */
    fun fromMillis(dateMillis: Long): LocalDate {
        return Instant.ofEpochMilli(dateMillis).atZone(DateUtils.ZONE_UTC).toLocalDate()
    }

    fun fromMillis(dateMillis: Long, zoneId: ZoneId): LocalDate {
        return Instant.ofEpochMilli(dateMillis).atZone(zoneId).toLocalDate()
    }

    fun getShortName(month: Month): String {
        return month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    fun getMonthShortName(date: LocalDate): String {
        return getShortName(date.month)
    }

    fun now(): Date {
        return Calendar.getInstance().time
    }

    fun isToday(date: LocalDate): Boolean {
        return LocalDate.now().isEqual(date)
    }

    fun isTomorrow(date: LocalDate): Boolean {
        return LocalDate.now().plusDays(1).isEqual(date)
    }

    fun nowUTC(): Date {
        return Date(System.currentTimeMillis())
    }

    fun toStartOfDayUTC(localDate: LocalDate): Date {
        return fromZonedDateTime(localDate.atStartOfDay(ZONE_UTC))
    }

    fun toStartOfDay(localDate: LocalDate): Date {
        return fromZonedDateTime(localDate.atStartOfDay(ZoneId.systemDefault()))
    }

    /**
     * @param localDate
     * @return timestamp in UTC
     */
    fun toMillis(localDate: LocalDate): Long {
        return toStartOfDayUTC(localDate).time
    }

    private fun fromZonedDateTime(dateTime: ZonedDateTime): Date {
        return Date(dateTime.toInstant().toEpochMilli())
    }

    fun isTodayUTC(localDate: LocalDate): Boolean {
        return localDate.isEqual(toStartOfDayUTCLocalDate(LocalDate.now()))
    }

    fun isTomorrowUTC(localDate: LocalDate): Boolean {
        return localDate.isEqual(toStartOfDayUTCLocalDate(LocalDate.now().plusDays(1)))
    }

    fun toStartOfDayUTCLocalDate(localDate: LocalDate): LocalDate {
        return localDate.atStartOfDay(ZONE_UTC).toLocalDate()
    }

    fun isBetween(date: LocalDate?, start: LocalDate?, end: LocalDate?): Boolean {
        return if (date == null || start == null || end == null) {
            false
        } else !date.isBefore(start) && !date.isAfter(end)
    }

    fun getBoundsFor4MonthsInThePast(currentDate: LocalDate): List<Pair<LocalDate, LocalDate>> {
        var monthStart = currentDate.minusMonths(3).with(firstDayOfMonth())
        var monthEnd = monthStart.with(lastDayOfMonth())

        val monthBounds = ArrayList<Pair<LocalDate, LocalDate>>()
        monthBounds.add(Pair(monthStart, monthEnd))
        for (i in 0..2) {
            monthStart = monthStart.plusMonths(1)
            monthEnd = monthStart.with(lastDayOfMonth())
            monthBounds.add(Pair(monthStart, monthEnd))
        }
        return monthBounds
    }

    fun getBoundsFor4WeeksInThePast(currentDate: LocalDate): List<Pair<LocalDate, LocalDate>> {
        var weekStart = currentDate.minusWeeks(3).with(DayOfWeek.MONDAY)
        var weekEnd = weekStart.with(DayOfWeek.SUNDAY)

        val weekBounds = ArrayList<Pair<LocalDate, LocalDate>>()
        weekBounds.add(Pair(weekStart, weekEnd))
        for (i in 0..2) {
            weekStart = weekStart.plusWeeks(1)
            weekEnd = weekStart.with(DayOfWeek.SUNDAY)
            weekBounds.add(Pair(weekStart, weekEnd))
        }
        return weekBounds
    }

    fun isYesterday(date: LocalDate): Boolean {
        return LocalDate.now().minusDays(1).isEqual(date)
    }

    fun getDayNumberSuffix(day: Int): String {
        if (day >= 11 && day <= 13) {
            return "th"
        }
        when (day % 10) {
            1 -> return "st"
            2 -> return "nd"
            3 -> return "rd"
            else -> return "th"
        }
    }

    fun fromUserZoneToLocalDate(date: Date): LocalDate {
        return Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun toDaysOfWeek(daysOfWeek: Set<Int>): Set<DayOfWeek> {
        val result = HashSet<DayOfWeek>()
        for (day in daysOfWeek) {
            result.add(DayOfWeek.of(day))
        }
        return result
    }

    fun toIntegers(daysOfWeek: Set<DayOfWeek>): Set<Int> {
        val result = HashSet<Int>()
        for (day in daysOfWeek) {
            result.add(day.value)
        }
        return result
    }
}
