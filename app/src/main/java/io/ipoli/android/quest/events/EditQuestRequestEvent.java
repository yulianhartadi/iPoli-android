package io.ipoli.android.quest.events;

public class EditQuestRequestEvent {
    public String questId;
    public int position;

    public EditQuestRequestEvent(String questId, int position) {
        this.questId = questId;
        this.position = position;
    }
}
