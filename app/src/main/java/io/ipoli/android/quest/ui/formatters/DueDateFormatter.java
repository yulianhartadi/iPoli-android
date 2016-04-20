package io.ipoli.android.quest.ui.formatters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/28/16.
 */
public class DueDateFormatter {
    private static SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd MMM yy", Locale.getDefault());
    private static SimpleDateFormat DATE_NO_YEAR_FORMAT = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public static String format(Date due) {
        return DEFAULT_DATE_FORMAT.format(due);
    }

    public static String formatWithoutYear(Date due) {
        DATE_NO_YEAR_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        return DATE_NO_YEAR_FORMAT.format(due);
    }
}