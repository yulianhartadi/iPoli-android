package io.ipoli.android.quest.events;

import io.ipoli.android.quest.data.Quest;

public class EditQuestRequestEvent {

    public final Quest quest;

    public EditQuestRequestEvent(Quest quest) {
        this.quest = quest;
    }
}
