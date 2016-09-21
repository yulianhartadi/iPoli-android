package io.ipoli.android.app.settings.events;

import io.ipoli.android.app.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class DailyChallengeStartTimeChangedEvent {
    public final Time time;

    public DailyChallengeStartTimeChangedEvent(Time time) {
        this.time = time;
    }
}
