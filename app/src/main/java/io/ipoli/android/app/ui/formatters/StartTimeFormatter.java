package io.ipoli.android.app.ui.formatters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/28/16.
 */
public class StartTimeFormatter {
    private static SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static SimpleDateFormat SHORT_TIME_FORMAT = new SimpleDateFormat("H:mm", Locale.getDefault());
    private static SimpleDateFormat SHORT_12_HOUR_TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public static String format(Date time) {
        if(time == null) {
            return formatEmptyTime();
        }
        return DEFAULT_TIME_FORMAT.format(time);
    }

    public static String formatShort(Date time, boolean use24HourFormat) {
        if(time == null) {
            formatEmptyTime();
        }
        if(use24HourFormat) {
            return SHORT_TIME_FORMAT.format(time);
        }
        return SHORT_12_HOUR_TIME_FORMAT.format(time);
    }

    private static String formatEmptyTime() {
        return "Don't know";
    }
}
