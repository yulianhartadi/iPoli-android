package io.ipoli.android.app.utils;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.TextStyle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.threeten.bp.temporal.TemporalAdjusters.firstDayOfMonth;
import static org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth;

public class DateUtils {

    public static final ZoneId ZONE_UTC = ZoneId.of("UTC");

    public static LocalDate fromMillis(long dateMillis) {
        return Instant.ofEpochMilli(dateMillis).atZone(DateUtils.ZONE_UTC).toLocalDate();
    }

    public static LocalDate fromMillis(long dateMillis, ZoneId zoneId) {
        return Instant.ofEpochMilli(dateMillis).atZone(zoneId).toLocalDate();
    }

    public static String getShortName(Month month) {
        return month.getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }

    public static String getMonthShortName(LocalDate date) {
        return getShortName(date.getMonth());
    }

    public static Date now() {
        return Calendar.getInstance().getTime();
    }

    public static boolean isToday(LocalDate date) {
        return LocalDate.now().isEqual(date);
    }

    public static boolean isTomorrow(LocalDate date) {
        return LocalDate.now().plusDays(1).isEqual(date);
    }

    public static Date getTomorrow() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        return c.getTime();
    }

    public static Date nowUTC() {
        return new Date(System.currentTimeMillis());
    }

    public static Date toStartOfDayUTC(LocalDate localDate) {
        return fromZonedDateTime(localDate.atStartOfDay(ZONE_UTC));
    }

    public static Date toStartOfDay(LocalDate localDate) {
        return fromZonedDateTime(localDate.atStartOfDay(ZoneId.systemDefault()));
    }

    public static long toMillis(LocalDate localDate) {
        return toStartOfDayUTC(localDate).getTime();
    }

    private static Date fromZonedDateTime(ZonedDateTime dateTime) {
        return new Date(dateTime.toInstant().toEpochMilli());
    }

    public static boolean isTodayUTC(LocalDate localDate) {
        return localDate.isEqual(toStartOfDayUTCLocalDate(LocalDate.now()));
    }

    public static boolean isTomorrowUTC(LocalDate localDate) {
        return localDate.isEqual(toStartOfDayUTCLocalDate(LocalDate.now().plusDays(1)));
    }

    public static LocalDate toStartOfDayUTCLocalDate(LocalDate localDate) {
        return localDate.atStartOfDay(ZONE_UTC).toLocalDate();
    }

    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }

    @NonNull
    public static List<Pair<LocalDate, LocalDate>> getBoundsFor4MonthsInThePast(LocalDate currentDate) {
        LocalDate monthStart = currentDate.minusMonths(3).with(firstDayOfMonth());
        LocalDate monthEnd = monthStart.with(lastDayOfMonth());

        List<Pair<LocalDate, LocalDate>> monthBounds = new ArrayList<>();
        monthBounds.add(new Pair<>(monthStart, monthEnd));
        for (int i = 0; i < 3; i++) {
            monthStart = monthStart.plusMonths(1);
            monthEnd = monthStart.with(lastDayOfMonth());
            monthBounds.add(new Pair<>(monthStart, monthEnd));
        }
        return monthBounds;
    }

    @NonNull
    public static List<Pair<LocalDate, LocalDate>> getBoundsFor4WeeksInThePast(LocalDate currentDate) {
        LocalDate weekStart = currentDate.minusWeeks(3).with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.with(DayOfWeek.SUNDAY);

        List<Pair<LocalDate, LocalDate>> weekBounds = new ArrayList<>();
        weekBounds.add(new Pair<>(weekStart, weekEnd));
        for (int i = 0; i < 3; i++) {
            weekStart = weekStart.plusWeeks(1);
            weekEnd = weekStart.with(DayOfWeek.SUNDAY);
            weekBounds.add(new Pair<>(weekStart, weekEnd));
        }
        return weekBounds;
    }

    public static boolean isYesterday(LocalDate date) {
        return LocalDate.now().minusDays(1).isEqual(date);
    }

    public static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static LocalDate fromUserZoneToLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}