package io.ipoli.android.quest.ui.formatters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 1/28/16.
 */
public class StartTimeFormatter {
    private static SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static String format(Date startTime) {
        return DEFAULT_TIME_FORMAT.format(startTime);
    }
}
