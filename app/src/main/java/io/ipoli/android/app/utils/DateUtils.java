package io.ipoli.android.app.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    public static Calendar getTodayAtMidnight() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        return c;
    }

    public static Date getNow() {
        return Calendar.getInstance().getTime();
    }

    public static boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return false;
        }

        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }

    public static boolean isTomorrow(Date date) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        return isSameDay(date, c.getTime());
    }

    public static boolean isBeforeToday(Date d) {
        if (d == null) {
            return false;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(d);
        Calendar today = Calendar.getInstance();
        if (c.get(Calendar.YEAR) < today.get(Calendar.YEAR)) {
            return true;
        }
        return c.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR);
    }

    public static Date getTomorrow() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        return c.getTime();
    }

    public static Date getNormalizedDueDate(Date dueDate) {
        if (dueDate == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(dueDate);
        Calendar normalizedDueDate = getTodayAtMidnight();
        normalizedDueDate.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR));
        normalizedDueDate.set(Calendar.YEAR, c.get(Calendar.YEAR));
        return normalizedDueDate.getTime();
    }

    public static String toDateString(Date date) {
        return toDateString(date, TimeZone.getDefault());
    }

    public static String toDateString(Date date, TimeZone timeZone) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        formatter.setTimeZone(timeZone);
        return formatter.format(date);
    }

    public static List<String> getNext7Days() {
        List<String> dates = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        dates.add(toDateString(c.getTime()));
        for(int i = 1; i < 7; i++) {
            c.add(Calendar.DAY_OF_YEAR, 1);
            dates.add(toDateString(c.getTime()));
        }

        return dates;
    }
}