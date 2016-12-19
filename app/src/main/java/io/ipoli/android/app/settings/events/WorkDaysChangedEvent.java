package io.ipoli.android.app.settings.events;

import java.util.Set;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/19/16.
 */
public class WorkDaysChangedEvent {
    public final Set<Integer> workDays;

    public WorkDaysChangedEvent(Set<Integer> workDays) {
        this.workDays = workDays;
    }
}
