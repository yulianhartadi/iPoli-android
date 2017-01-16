package io.ipoli.android.app.ui.formatters;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/15/16.
 */
public class TimesADayFormatter {

    public static String formatReadable(int value) {
        if(value <= 0) {
            value = 1;
        }
        if(value == 1) {
            return "Once";
        } else if(value == 2) {
            return "Twice";
        }
        return value + " times";
    }

    public static String formatReadable(int value, String suffix) {
        if(value <= 0) {
            value = 1;
        }
        if(value == 1) {
            return "Once " + suffix;
        } else if(value == 2) {
            return "Twice " + suffix;
        }

        return value + " times " + suffix;
    }

}
