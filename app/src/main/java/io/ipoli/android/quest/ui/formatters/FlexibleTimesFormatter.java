package io.ipoli.android.quest.ui.formatters;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/15/16.
 */
public class FlexibleTimesFormatter {
    public static String formatReadable(int timesPerDay) {
        if(timesPerDay <= 0) {
            timesPerDay = 1;
        }
        return timesPerDay == 1 ? timesPerDay + " time" : timesPerDay + " times";
    }
}
