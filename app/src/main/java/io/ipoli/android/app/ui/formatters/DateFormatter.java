package io.ipoli.android.app.ui.formatters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/28/16.
 */
public class DateFormatter {
    public static final String DEFAULT_EMPTY_VALUE = "Don't know";
    private static SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd MMM yy", Locale.getDefault());
    private static SimpleDateFormat DATE_NO_YEAR_FORMAT = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public static String format(Date date) {
        if (date == null) {
            return DEFAULT_EMPTY_VALUE;
        }
        if (DateUtils.isToday(date)) {
            return "Today";
        }
        if (DateUtils.isTomorrow(date)) {
            return "Tomorrow";
        }
        return DEFAULT_DATE_FORMAT.format(date);
    }

    public static String formatWithoutYear(Date date, String emptyValue) {
        if (date == null) {
            return emptyValue;
        }
        if (DateUtils.isToday(date)) {
            return "Today";
        }
        if (DateUtils.isTomorrow(date)) {
            return "Tomorrow";
        }
        if (DateUtils.isYesterday(date)) {
            return "Yesterday";
        }
        DATE_NO_YEAR_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return DATE_NO_YEAR_FORMAT.format(date);
    }

    public static String formatWithoutYear(Date date) {
        return formatWithoutYear(date, DEFAULT_EMPTY_VALUE);
    }
}