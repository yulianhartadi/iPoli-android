package com.curiousily.ipoli.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/15.
 */
public class DateUtils {

    public static Date getTodayAtMidnight() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        return c.getTime();
    }

    public static Date getNow() {
        return Calendar.getInstance().getTime();
    }
}
