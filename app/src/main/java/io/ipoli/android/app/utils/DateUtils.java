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
}