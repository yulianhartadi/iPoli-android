package io.ipoli.android.app.ui.formatters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/15/16.
 */
public class FlexibleTimesFormatter {
    public static String formatReadable(int value) {
        if(value <= 0) {
            value = 1;
        }
        return value == 1 ? value + " time" : value + " times";
    }

    public static int parse(String flexibleTimes) {
        Pattern pattern = Pattern.compile("(\\d{1,2})\\stime(s)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(flexibleTimes);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        }

        throw new IllegalArgumentException("Wrong flexible time format");
    }
}
