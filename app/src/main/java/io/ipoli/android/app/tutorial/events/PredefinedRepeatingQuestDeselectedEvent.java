package io.ipoli.android.app.tutorial.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/29/16.
 */
public class PredefinedRepeatingQuestDeselectedEvent {
    public final String rawText;
    public final EventSource source;

    public PredefinedRepeatingQuestDeselectedEvent(String rawText, EventSource source) {
        this.rawText = rawText;
        this.source = source;
    }
}
