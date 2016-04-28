package io.ipoli.android.quest.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class NewQuestSavedEvent {
    public final String text;
    public final EventSource source;

    public NewQuestSavedEvent(String text, EventSource source) {
        this.text = text;
        this.source = source;
    }
}
