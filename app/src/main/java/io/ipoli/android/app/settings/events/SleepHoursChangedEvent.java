package io.ipoli.android.app.settings.events;

import io.ipoli.android.app.utils.Time;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/19/16.
 */
public class SleepHoursChangedEvent {
    public final Time startTime;
    public final Time endTime;

    public SleepHoursChangedEvent(Time startTime, Time endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
