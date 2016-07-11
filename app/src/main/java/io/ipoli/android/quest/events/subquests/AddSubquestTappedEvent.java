package io.ipoli.android.quest.events.subquests;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/11/16.
 */
public class AddSubquestTappedEvent {
    public final EventSource source;

    public AddSubquestTappedEvent(EventSource source) {
        this.source = source;
    }
}
