package io.ipoli.android.app.scheduling;

import android.support.annotation.NonNull;

import io.ipoli.android.app.utils.TimePreference;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */
public class Task implements Comparable<Task> {
    private final int startMinute;
    private final int duration;
    private final TimePreference startTimePreference;

    public Task(int startMinute, int duration, TimePreference startTimePreference) {
        this.startMinute = startMinute;
        this.duration = duration;
        this.startTimePreference = startTimePreference;
    }

    public Task(int startMinute, int duration) {
        this(startMinute, duration, TimePreference.ANY);
    }

    public Task(int duration) {
        this(-1, duration, TimePreference.ANY);
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getDuration() {
        return duration;
    }

    public TimePreference getStartTimePreference() {
        return startTimePreference;
    }

    @Override
    public int compareTo(@NonNull Task otherTask) {
        return Integer.valueOf(startMinute).compareTo(otherTask.startMinute);
    }
}
