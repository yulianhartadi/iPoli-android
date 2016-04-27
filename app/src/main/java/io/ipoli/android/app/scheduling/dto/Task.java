package io.ipoli.android.app.scheduling.dto;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/27/16.
 */
public class Task {
    public Integer startMinute;
    public int duration;
    public String context;

    public Task(Integer startMinute, int duration, String context) {
        this.startMinute = startMinute;
        this.duration = duration;
        this.context = context;
    }

    public Task(int duration, String context) {
        this(null, duration, context);
    }
}
