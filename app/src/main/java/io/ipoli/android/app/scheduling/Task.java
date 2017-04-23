package io.ipoli.android.app.scheduling;

import android.support.annotation.NonNull;

import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */
public class Task implements Comparable<Task> {
    private final int startMinute;
    private final int duration;
    private final TimePreference startTimePreference;
    private Category category;

    public Task(int startMinute, int duration, TimePreference startTimePreference, Category category) {
        this.startMinute = startMinute;
        this.duration = duration;
        this.startTimePreference = startTimePreference;
        this.category = category;
    }

    public Task(int startMinute, int duration, Category category) {
        this(startMinute, duration, TimePreference.ANY, category);
    }

    public Task(int duration, Category category) {
        this(-1, duration, TimePreference.ANY, category);
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

    public Category getCategory() {
        return category;
    }
}
