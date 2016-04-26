package io.ipoli.android.quest.events;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.Quest;

public class EditQuestRequestEvent {

    public final Quest quest;
    public final EventSource source;

    public EditQuestRequestEvent(Quest quest, EventSource source) {
        this.quest = quest;
        this.source = source;
    }
}
