package io.ipoli.android.app.scheduling;

import org.threeten.bp.DayOfWeek;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import io.ipoli.android.app.TimeOfDay;

public class DailySchedulerBuilder {
    private int startMinute;
    private int endMinute;
    private int timeSlotDuration = 15;
    private int workStartMinute;
    private int workEndMinute;
    private Set<DayOfWeek> workDays = new HashSet<>();
    private Set<TimeOfDay> productiveTimes = new HashSet<>();
    private Random seed = new Random();

    public DailySchedulerBuilder setStartMinute(int startMinute) {
        this.startMinute = startMinute;
        return this;
    }

    public DailySchedulerBuilder setEndMinute(int endMinute) {
        this.endMinute = endMinute;
        return this;
    }

    public DailySchedulerBuilder setTimeSlotDuration(int timeSlotDuration) {
        this.timeSlotDuration = timeSlotDuration;
        return this;
    }

    public DailySchedulerBuilder setWorkStartMinute(int workStartMinute) {
        this.workStartMinute = workStartMinute;
        return this;
    }

    public DailySchedulerBuilder setWorkEndMinute(int workEndMinute) {
        this.workEndMinute = workEndMinute;
        return this;
    }

    public DailySchedulerBuilder setProductiveTimes(Set<TimeOfDay> productiveTimes) {
        this.productiveTimes = productiveTimes;
        return this;
    }

    public DailySchedulerBuilder setSeed(Random seed) {
        this.seed = seed;
        return this;
    }

    public DailySchedulerBuilder setSeed(int seed) {
        this.seed = new Random(seed);
        return this;
    }

    public DailySchedulerBuilder setWorkDays(Set<DayOfWeek> workDays) {
        this.workDays = workDays;
        return this;
    }

    public DailyScheduler create() {
        return new DailyScheduler(startMinute, endMinute, timeSlotDuration, workStartMinute, workEndMinute, workDays, productiveTimes, seed);
    }
}