package io.ipoli.android.app.utils;

import java.util.Calendar;
import java.util.Date;

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

    public static Date getNormalizedStartTime(Date startTime) {
        if(startTime == null) {
            return null;
        }
        Calendar sc = Calendar.getInstance();
        sc.setTime(startTime);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(0);
        c.set(Calendar.HOUR_OF_DAY, sc.get(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, sc.get(Calendar.MINUTE));
        return c.getTime();
    }
}