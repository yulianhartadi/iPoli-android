package io.ipoli.android.reminder;

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

    public static int getNameRes(TimeOffsetType timeOffsetType) {
        switch (timeOffsetType) {
            case MINUTES:
                return R.string.minutes;
            case HOURS:
                return R.string.hours;
            case DAYS:
                return R.string.days;
            default:
                return R.string.weeks;
        }
    }

    public static int getNameSingleRes(TimeOffsetType timeOffsetType) {
        switch (timeOffsetType) {
            case MINUTES:
                return R.string.minute;
            case HOURS:
                return R.string.hour;
            case DAYS:
                return R.string.day;
            default:
                return R.string.week;
        }
    }

    public static int getNameBeforeRes(TimeOffsetType timeOffsetType) {
        switch (timeOffsetType) {
            case MINUTES:
                return R.string.minutes_before;
            case HOURS:
                return R.string.hours_before;
            case DAYS:
                return R.string.days_before;
            default:
                return R.string.weeks_before;
        }
    }
}