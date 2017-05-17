package io.ipoli.android.app.scheduling;

import android.support.annotation.NonNull;

import java.util.List;

import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */
public class Task implements Comparable<Task> {
    private final String id;
    private final int startMinute;
    private final int duration;
    private final int priority;
    private final TimePreference startTimePreference;
    private final Category category;
    private int currentTimeSlotIndex = -1;
    private List<TimeSlot> recommendedSlots;

    public Task(String id, int startMinute, int duration, int priority, TimePreference startTimePreference, Category category) {
        this.id = id;
        this.startMinute = startMinute;
        this.duration = duration;
        this.priority = priority;
        this.startTimePreference = startTimePreference;
        this.category = category;
    }

    public Task(int startMinute, int duration, int priority, TimePreference startTimePreference, Category category) {
        this("", startMinute, duration, priority, startTimePreference, category);
    }

    public Task(String id, int duration, int priority, TimePreference startTimePreference, Category category) {
        this(id, -1, duration, priority, startTimePreference, category);
    }

    public Task(int duration, int priority, TimePreference startTimePreference, Category category) {
        this("", -1, duration, priority, startTimePreference, category);
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

    public String getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public int getPriority() {
        return priority;
    }

    void setRecommendedSlots(List<TimeSlot> recommendedSlots) {
        this.recommendedSlots = recommendedSlots;
    }

    List<TimeSlot> getRecommendedSlots() {
        return recommendedSlots;
    }

    int getCurrentTimeSlotIndex() {
        return currentTimeSlotIndex;
    }

    void setCurrentTimeSlotIndex(int currentTimeSlotIndex) {
        this.currentTimeSlotIndex = currentTimeSlotIndex;
    }

    public TimeSlot getCurrentTimeSlot() {
        if(recommendedSlots == null || recommendedSlots.isEmpty() || currentTimeSlotIndex >= recommendedSlots.size()
                || currentTimeSlotIndex < 0) {
            return null;
        }
        return recommendedSlots.get(currentTimeSlotIndex);
    }

    public int getEndMinute() {
        return (getStartMinute() + getDuration()) % Time.MINUTES_IN_A_DAY;
    }

    @Override
    public boolean equals(Object t) {
        if (this == t) return true;
        if (!(t instanceof Task)) return false;

        Task task = (Task) t;

        if (getStartMinute() != task.getStartMinute()) return false;
        if (getDuration() != task.getDuration()) return false;
        if (getPriority() != task.getPriority()) return false;
        if (!getId().equals(task.getId())) return false;
        if (getStartTimePreference() != task.getStartTimePreference()) return false;
        return getCategory() == task.getCategory();

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getStartMinute();
        result = 31 * result + getDuration();
        result = 31 * result + getPriority();
        result = 31 * result + (getStartTimePreference() != null ? getStartTimePreference().hashCode() : 0);
        result = 31 * result + getCategory().hashCode();
        return result;
    }
}
