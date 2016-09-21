package io.ipoli.android.app.settings.events;

import java.util.Set;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class DailyChallengeDaysOfWeekChangedEvent {

    public final Set<Integer> selectedDaysOfWeek;

    public DailyChallengeDaysOfWeekChangedEvent(Set<Integer> selectedDaysOfWeek) {
        this.selectedDaysOfWeek = selectedDaysOfWeek;
    }
}
