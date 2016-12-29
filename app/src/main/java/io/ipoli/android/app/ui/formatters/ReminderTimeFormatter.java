package io.ipoli.android.app.ui.formatters;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.reminder.TimeOffsetType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/30/16.
 */
public class ReminderTimeFormatter {

    public static String formatMinutesBeforeReadable(long minutes) {
        if(minutes < 0) {
            return "";
        }

        if(minutes == 0) {
            return "At start";
        }

        long hours = TimeUnit.MINUTES.toHours(minutes);
        long mins = minutes - hours * 60;

        if (hours > 0 && mins > 0) {
            return hours + "hours and " + mins + " minutes before";
        }

        if (hours > 0 && mins == 0) {
            return hours == 1 ? "1 hour before" : hours + " hours before";
        }

        return mins == 1 ? "1 minute before" : mins + " minutes before";
    }

    public static String formatTimeOffset(long timeValue, TimeOffsetType timeOffsetType) {
        if(timeValue < 0) {
            return "";
        }

        if(timeValue == 0) {
            return "At start";
        }

        String type = timeOffsetType.name().toLowerCase();
        if(timeValue == 1) {
            type = type.substring(0, type.length() - 1);
        }

        return timeValue + " " + type + " before";
    }
}
