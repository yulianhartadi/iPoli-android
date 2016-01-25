package io.ipoli.android.quest.events;

public class EditQuestRequestEvent {
    public String questId;

    public EditQuestRequestEvent(String questId) {
        this.questId = questId;
    }
}
