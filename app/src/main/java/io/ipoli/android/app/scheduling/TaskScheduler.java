package io.ipoli.android.app.scheduling;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class TaskScheduler {
    private final int startHour;
    private final int endHour;
    private final TreeSet<Task> tasks;

    public TaskScheduler(int startHour, int endHour, List<Task> tasks) {
        this.startHour = startHour;
        this.endHour = endHour;
        this.tasks = new TreeSet<>(tasks);
    }

    public TaskScheduler(int startHour, int endHour) {
        this(startHour, endHour, new ArrayList<>());
    }

    public List<TimeBlock> getFreeBlocksFor(Task task) {
        List<TimeBlock> freeBlocks = new ArrayList<>();
        int endMinute = endHour > 1 ? (endHour - 1) * 60 : 60;
        freeBlocks.add(new TimeBlock(startHour * 60, endMinute));
        int newTaskDuration = task != null ? task.getDuration() : 0;
        for (Task t : tasks) {
            freeBlocks = insertTaskInSchedule(freeBlocks, newTaskDuration, t);
        }
        return freeBlocks;
    }

    @NonNull
    private List<TimeBlock> insertTaskInSchedule(List<TimeBlock> freeBlocks, int newTaskDuration, Task t) {
        int startMinute = t.getStartMinute();
        int endMinute = startMinute + t.getDuration();

        List<TimeBlock> newBlocks = cloneTimeBlocks(freeBlocks);
        for (TimeBlock tb : freeBlocks) {
            newBlocks.remove(tb);
            if (startMinute >= tb.getStartMinute() && endMinute <= tb.getEndMinute()) {
                TimeBlock b1 = new TimeBlock(tb.getStartMinute(), startMinute);
                TimeBlock b2 = new TimeBlock(endMinute, tb.getEndMinute());
                if (startMinute > tb.getStartMinute() && endMinute < tb.getEndMinute()) {
                    addBlockIfHasEnoughDuration(newTaskDuration, newBlocks, b1);
                    addBlockIfHasEnoughDuration(newTaskDuration, newBlocks, b2);
                } else if (startMinute == tb.getStartMinute()) {
                    addBlockIfHasEnoughDuration(newTaskDuration, newBlocks, b2);
                } else if (endMinute == tb.getEndMinute()) {
                    addBlockIfHasEnoughDuration(newTaskDuration, newBlocks, b1);
                }
                return newBlocks;
            }
        }
        return freeBlocks;
    }

    private List<TimeBlock> cloneTimeBlocks(List<TimeBlock> timeBlocks) {
        List<TimeBlock> result = new ArrayList<>();
        for (TimeBlock tb : timeBlocks) {
            result.add(tb);
        }
        return result;
    }

    public List<TimeBlock> getAvailableSlotsFor(Task task, int minTimeInterval) {
        List<TimeBlock> freeBlocks = getFreeBlocksFor(task);
        List<TimeBlock> slots = new ArrayList<>();
        for (TimeBlock b : freeBlocks) {
            int start = b.getStartMinute();
            while (true) {
                slots.add(new TimeBlock(start, start + task.getDuration()));
                start += minTimeInterval;
                if (start + task.getDuration() > b.getEndMinute()) {
                    break;
                }
            }
        }
        return slots;
    }

    private void addBlockIfHasEnoughDuration(int taskDuration, List<TimeBlock> newBlocks, TimeBlock b1) {
        if (b1.getDuration() >= taskDuration) {
            newBlocks.add(b1);
        }
    }

    public List<TimeBlock> getFreeBlocks() {
        return getFreeBlocksFor(null);
    }

    public void addTask(Task task) {
        tasks.add(task);
    }
}
