package io.ipoli.android.quest.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class ShowQuestEvent {
    public final Quest quest;
    public final EventSource source;

    public ShowQuestEvent(Quest quest, EventSource source) {
        this.quest = quest;
        this.source = source;
    }
}
