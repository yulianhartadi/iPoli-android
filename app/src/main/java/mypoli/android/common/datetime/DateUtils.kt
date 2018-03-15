package mypoli.android.common.datetime

import android.util.Pair
import org.threeten.bp.*
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.TemporalAdjusters.firstDayOfMonth
import org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth
import org.threeten.bp.temporal.WeekFields
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

//    fun dayOfWeekText(val dayOfWeek : DayOfWeek, style: TextStyle): String {
//        return (0 until DAYS_IN_A_WEEK).map {
//            val dayOfWeek = DateUtils.firstDayOfWeek.plus(it.toLong())
//
//            dayOfWeek.getDisplayName(
//                style,
//                Locale.getDefault()
//            )
//        }
//    }

    const val DAYS_IN_A_WEEK = 7

    fun daysOfWeekText(style: TextStyle): List<String> {
        return (0 until DAYS_IN_A_WEEK).map {
            val dayOfWeek = DateUtils.firstDayOfWeek.plus(it.toLong())
            dayOfWeekText(dayOfWeek, style)
        }
    }

    fun dayOfWeekText(
        dayOfWeek: DayOfWeek,
        style: TextStyle
    ): String {
        return dayOfWeek.getDisplayName(
            style,
            Locale.getDefault()
        )
    }

    val localeDaysOfWeek: List<DayOfWeek>
        get() = (0 until DAYS_IN_A_WEEK).map {
            DateUtils.firstDayOfWeek.plus(it.toLong())
        }

    val firstDayOfWeek: DayOfWeek
        get() = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    val lastDayOfWeek: DayOfWeek
        get() = firstDayOfWeek.plus(6)

    val today: LocalDate
        get() = LocalDate.now()

    fun max(date1: LocalDate, date2: LocalDate): LocalDate {
        if (date1.isAfter(date2)) {
            return date1
        }

        return date2
    }

    fun min(date1: LocalDate, date2: LocalDate): LocalDate {
        if (date1.isBefore(date2)) {
            return date1
        }

        return date2
    }
}
