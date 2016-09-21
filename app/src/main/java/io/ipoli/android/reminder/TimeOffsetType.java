package io.ipoli.android.reminder;

import java.util.concurrent.TimeUnit;

public enum TimeOffsetType {
        MINUTES(1), HOURS(60), DAYS(TimeUnit.DAYS.toMinutes(1)), WEEKS(TimeUnit.DAYS.toMinutes(7));

        private final long minutes;

        TimeOffsetType(long minutes) {
            this.minutes = minutes;
        }

        public long getMinutes() {
            return minutes;
        }
    }