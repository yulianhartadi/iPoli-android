package io.ipoli.android.app.scheduling;

import io.ipoli.android.app.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class TimeSlot {
    private final int startMinute;
    private final int endMinute;

    public TimeSlot(int startMinute, int endMinute) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public int getDuration() {
        return endMinute - startMinute;
    }

    public Time getStartTime() {
        return Time.of(startMinute);
    }

    public Time getEndTime() {
        return Time.of(endMinute);
    }

    public boolean doOverlap(int startMinute, int endMinute) {
        return !(getEndMinute() < startMinute || endMinute < getStartMinute());
    }
}
