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
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isBeforeToday(Date d) {
        return d.compareTo(new Date()) < 0 ? true : false;
    }
}