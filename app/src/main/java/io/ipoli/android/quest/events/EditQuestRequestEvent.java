package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

public class EditQuestRequestEvent {

    public final Quest quest;
    public final String source;

    public EditQuestRequestEvent(Quest quest, String source) {
        this.quest = quest;
        this.source = source;
    }
}
