package io.ipoli.android.quest.events.subquests;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/11/16.
 */
public class AddSubQuestTappedEvent {
    public final EventSource source;

    public AddSubQuestTappedEvent(EventSource source) {
        this.source = source;
    }
}
