package io.ipoli.android.app.jobs;

import java.util.Calendar;

import io.ipoli.android.app.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public abstract class RemindJob {

    private final Time time;

    public RemindJob(Time time) {
        this.time = time;
    }

    public void schedule() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, time.hour);
        calendar.set(Calendar.MINUTE, time.minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time to show has passed for today => schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        execute(calendar.getTimeInMillis());
    }

    protected abstract void execute(long timeToSchedule);
}
