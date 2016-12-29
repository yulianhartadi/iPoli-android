package io.ipoli.android.quest.events;

import io.ipoli.android.app.events.EventSource;

public class EditQuestRequestEvent {

    public final EventSource source;
    public final String questId;

    public EditQuestRequestEvent(String questId, EventSource source) {
        this.questId = questId;
        this.source = source;
    }
}
