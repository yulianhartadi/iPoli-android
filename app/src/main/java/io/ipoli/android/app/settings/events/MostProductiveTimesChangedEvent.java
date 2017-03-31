package io.ipoli.android.app.settings.events;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.TimeOfDay;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/19/16.
 */
public class MostProductiveTimesChangedEvent {
    private final List<TimeOfDay> timesOfDay;

    public MostProductiveTimesChangedEvent(List<TimeOfDay> timesOfDay) {
        this.timesOfDay = timesOfDay;
    }

    public List<String> getTimesOfDay() {
        List<String> names = new ArrayList<>();
        for(TimeOfDay timeOfDay : timesOfDay) {
            names.add(timeOfDay.name());
        }
        return names;
    }
}
