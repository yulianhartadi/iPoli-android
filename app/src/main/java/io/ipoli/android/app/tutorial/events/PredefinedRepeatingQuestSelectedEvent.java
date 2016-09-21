package io.ipoli.android.app.tutorial.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/29/16.
 */
public class PredefinedRepeatingQuestSelectedEvent {
    public final String rawText;
    public final EventSource source;

    public PredefinedRepeatingQuestSelectedEvent(String rawText, EventSource source) {
        this.rawText = rawText;
        this.source = source;
    }
}
