package io.ipoli.android.quest.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/29/16.
 */
public class ShareQuestEvent {

    public final Quest quest;
    public final EventSource source;

    public ShareQuestEvent(Quest quest, EventSource source) {
        this.quest = quest;
        this.source = source;
    }
}
