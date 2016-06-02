package io.ipoli.android.quest.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/2/16.
 */
public class AddQuestButtonTappedEvent {
    public final EventSource source;

    public AddQuestButtonTappedEvent(EventSource source) {
        this.source = source;
    }
}
