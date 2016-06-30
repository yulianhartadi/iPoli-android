package io.ipoli.android.quest.ui.formatters;

import java.util.concurrent.TimeUnit;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/30/16.
 */
public class ReminderTimeFormatter {

    public static String formatMinutesBeforeReadable(int minutes) {
        long hours = TimeUnit.MINUTES.toHours(minutes);
        long mins = minutes - hours * 60;
        if (hours <= 0 && mins <= 0) {
            return "";
        }
        if (hours > 0 && mins > 0) {
            return hours + "hours and " + mins + " minutes before";
        }

        if (hours > 0 && mins == 0) {
            return hours == 1 ? "1 hour before" : hours + " hours before";
        }

        return mins == 1 ? "1 minute before" : mins + " minutes before";
    }
}
