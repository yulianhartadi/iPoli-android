package io.ipoli.android.app.settings.events;

import java.util.HashSet;
import java.util.Set;

import io.ipoli.android.app.TimeOfDay;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/19/16.
 */
public class MostProductiveTimesChangedEvent {
    private final Set<TimeOfDay> timesOfDay;

    public MostProductiveTimesChangedEvent(Set<TimeOfDay> timesOfDay) {
        this.timesOfDay = timesOfDay;
    }

    public Set<String> getTimesOfDay() {
        Set<String> names = new HashSet<>();
        for (TimeOfDay timeOfDay : timesOfDay) {
            names.add(timeOfDay.name());
        }
        return names;
    }
}
