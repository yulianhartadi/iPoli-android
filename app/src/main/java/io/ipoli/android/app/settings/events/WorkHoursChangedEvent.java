package io.ipoli.android.app.settings.events;

import io.ipoli.android.app.utils.Time;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/19/16.
 */
public class WorkHoursChangedEvent {
    public final Time startTime;
    public final Time endTime;

    public WorkHoursChangedEvent(Time startTime, Time endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
