package io.ipoli.android.quest.events;

import java.util.Date;

public class EditQuestRequestEvent {
    public final String questId;
    public final int position;
    public final Date due;
    public String name;

    public EditQuestRequestEvent(String questId, String name, int position, Date due) {
        this.questId = questId;
        this.name = name;
        this.position = position;
        this.due = due;
    }
}
