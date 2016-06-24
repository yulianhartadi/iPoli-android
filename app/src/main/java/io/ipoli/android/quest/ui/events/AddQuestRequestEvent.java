package io.ipoli.android.quest.ui.events;

import io.ipoli.android.app.events.EventSource;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/24/16.
 */
public class AddQuestRequestEvent {
    public final EventSource source;

    public AddQuestRequestEvent(EventSource source) {
        this.source = source;
    }
}
