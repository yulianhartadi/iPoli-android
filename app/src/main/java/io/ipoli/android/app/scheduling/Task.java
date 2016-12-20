package io.ipoli.android.app.scheduling;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class Task implements Comparable<Task> {
    private final int startMinute;
    private final int duration;

    public Task(int startMinute, int duration) {
        this.startMinute = startMinute;
        this.duration = duration;
    }

    public Task(int duration) {
        this.startMinute = -1;
        this.duration = duration;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public int compareTo(Task task) {
        return Integer.valueOf(startMinute).compareTo(task.startMinute);
    }
}
