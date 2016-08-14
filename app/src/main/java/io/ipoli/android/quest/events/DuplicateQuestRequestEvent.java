package io.ipoli.android.quest.events;

import java.util.Date;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/12/16.
 */
public class DuplicateQuestRequestEvent {
    public final Quest quest;
    public final Date date;
    public final EventSource source;

    public DuplicateQuestRequestEvent(Quest quest, EventSource source) {
        this.quest = quest;
        this.date = null;
        this.source = source;
    }

    public DuplicateQuestRequestEvent(Quest quest, Date date, EventSource source) {
        this.quest = quest;
        this.date = date;
        this.source = source;
    }
}
