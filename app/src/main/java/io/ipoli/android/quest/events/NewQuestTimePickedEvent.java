package io.ipoli.android.quest.events;

import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.TimePreference;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/17.
 */
public class NewQuestTimePickedEvent {
    public final Time time;
    public final TimePreference timePreference;

    public NewQuestTimePickedEvent(Time time) {
        this.time = time;
        this.timePreference = null;
    }

    public NewQuestTimePickedEvent(TimePreference timePreference) {
        this.time = null;
        this.timePreference = timePreference;
    }
}
