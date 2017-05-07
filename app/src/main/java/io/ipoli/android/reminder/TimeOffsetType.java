package io.ipoli.android.reminder;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.R;

public enum TimeOffsetType {
    MINUTES(1), HOURS(60), DAYS(TimeUnit.DAYS.toMinutes(1)), WEEKS(TimeUnit.DAYS.toMinutes(7));

    private final long minutes;

    TimeOffsetType(long minutes) {
        this.minutes = minutes;
    }

    public long getMinutes() {
        return minutes;
    }

    public static String getLocalTypeName(Context context, TimeOffsetType timeOffsetType) {
        switch (timeOffsetType) {
            case MINUTES:
                return context.getString(R.string.minutes);
            case HOURS:
                return context.getString(R.string.hours);
            case DAYS:
                return context.getString(R.string.days);
            default:
                return context.getString(R.string.weeks);
        }
    }

    public static String getLocalTypeNameSingle(Context context, TimeOffsetType timeOffsetType) {
        switch (timeOffsetType) {
            case MINUTES:
                return context.getString(R.string.minute);
            case HOURS:
                return context.getString(R.string.hour);
            case DAYS:
                return context.getString(R.string.day);
            default:
                return context.getString(R.string.week);
        }
    }

    public static String getLocalTypeNameBefore(Context context, TimeOffsetType timeOffsetType) {
        switch (timeOffsetType) {
            case MINUTES:
                return context.getString(R.string.minutes_before);
            case HOURS:
                return context.getString(R.string.hours_before);
            case DAYS:
                return context.getString(R.string.days_before);
            default:
                return context.getString(R.string.weeks_before);
        }
    }
}