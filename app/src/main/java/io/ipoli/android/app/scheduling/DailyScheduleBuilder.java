package io.ipoli.android.app.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.ipoli.android.app.TimeOfDay;

public class DailyScheduleBuilder {
    private int startMinute;
    private int endMinute;
    private int timeSlotDuration = 15;
    private List<Task> scheduledTasks = new ArrayList<>();
    private int workStartMinute;
    private int workEndMinute;
    private List<TimeOfDay> productiveTimes;
    private Random seed = new Random();

    public DailyScheduleBuilder setStartMinute(int startMinute) {
        this.startMinute = startMinute;
        return this;
    }

    public DailyScheduleBuilder setEndMinute(int endMinute) {
        this.endMinute = endMinute;
        return this;
    }

    public DailyScheduleBuilder setTimeSlotDuration(int timeSlotDuration) {
        this.timeSlotDuration = timeSlotDuration;
        return this;
    }

    public DailyScheduleBuilder setScheduledTasks(List<Task> scheduledTasks) {
        this.scheduledTasks = scheduledTasks;
        return this;
    }

    public DailyScheduleBuilder setWorkStartMinute(int workStartMinute) {
        this.workStartMinute = workStartMinute;
        return this;
    }

    public DailyScheduleBuilder setWorkEndMinute(int workEndMinute) {
        this.workEndMinute = workEndMinute;
        return this;
    }

    public DailyScheduleBuilder setProductiveTimes(List<TimeOfDay> productiveTimes) {
        this.productiveTimes = productiveTimes;
        return this;
    }

    public DailyScheduleBuilder setSeed(Random seed) {
        this.seed = seed;
        return this;
    }

    public DailySchedule createDailySchedule() {
        return new DailySchedule(startMinute, endMinute, timeSlotDuration, workStartMinute, workEndMinute, productiveTimes, scheduledTasks, seed);
    }
}