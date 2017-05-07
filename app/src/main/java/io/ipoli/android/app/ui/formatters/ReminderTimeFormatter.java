package io.ipoli.android.app.ui.formatters;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.R;
import io.ipoli.android.reminder.TimeOffsetType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/30/16.
 */
public class ReminderTimeFormatter {

    public static String formatMinutesBeforeReadable(Context context, long minutes) {
        if(minutes < 0) {
            return "";
        }

        if(minutes == 0) {
            return context.getString(R.string.reminder_at_start);
        }

        long hours = TimeUnit.MINUTES.toHours(minutes);
        long mins = minutes - hours * 60;

        if (hours > 0 && mins > 0) {
            return String.format(context.getString(R.string.reminder_time_format_full), hours, mins);
        }

        if (hours > 0 && mins == 0) {
            return hours == 1 ? context.getString(R.string.reminder_time_format_1_hour) :
                    String.format(context.getString(R.string.reminder_time_format_hours), hours);
        }

        return mins == 1 ? context.getString(R.string.reminder_time_format_1_minute) :
                String.format(context.getString(R.string.reminder_time_format_minutes), mins);
    }

    public static String formatTimeOffset(Context context, long timeValue, TimeOffsetType timeOffsetType) {
        if(timeValue < 0) {
            return "";
        }

        if(timeValue == 0) {
            return context.getString(R.string.reminder_at_start);
        }

        String type = TimeOffsetType.getLocalTypeName(context, timeOffsetType).toLowerCase();
        if(timeValue == 1) {
            type = TimeOffsetType.getLocalTypeNameSingle(context, timeOffsetType).toLowerCase();
        }

        return String.format(context.getString(R.string.reminder_time_format_offset), timeValue, type);
    }
}
