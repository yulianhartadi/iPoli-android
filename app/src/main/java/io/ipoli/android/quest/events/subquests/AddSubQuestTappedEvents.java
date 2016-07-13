package io.ipoli.android.quest.events.subquests;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/11/16.
 */
public class AddSubQuestTappedEvents {
    public final EventSource source;

    public AddSubQuestTappedEvents(EventSource source) {
        this.source = source;
    }
}
