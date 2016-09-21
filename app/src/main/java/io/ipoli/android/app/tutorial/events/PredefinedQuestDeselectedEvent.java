package io.ipoli.android.app.tutorial.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/29/16.
 */
public class PredefinedQuestDeselectedEvent {
    public final String name;
    public final EventSource source;

    public PredefinedQuestDeselectedEvent(String name, EventSource source) {
        this.name = name;
        this.source = source;
    }
}
