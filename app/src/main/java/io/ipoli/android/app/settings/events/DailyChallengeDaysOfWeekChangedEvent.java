package io.ipoli.android.app.settings.events;

import org.threeten.bp.DayOfWeek;

import java.util.Set;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class DailyChallengeDaysOfWeekChangedEvent {

    public final Set<DayOfWeek> selectedDaysOfWeek;

    public DailyChallengeDaysOfWeekChangedEvent(Set<DayOfWeek> selectedDaysOfWeek) {
        this.selectedDaysOfWeek = selectedDaysOfWeek;
    }
}
