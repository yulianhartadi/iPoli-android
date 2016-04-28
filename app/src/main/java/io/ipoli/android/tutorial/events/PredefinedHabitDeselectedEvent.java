package io.ipoli.android.tutorial.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/29/16.
 */
public class PredefinedHabitDeselectedEvent {
    public final String rawText;
    public final EventSource source;

    public PredefinedHabitDeselectedEvent(String rawText, EventSource source) {
        this.rawText = rawText;
        this.source = source;
    }
}
