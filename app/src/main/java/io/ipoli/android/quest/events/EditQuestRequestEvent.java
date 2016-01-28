package io.ipoli.android.quest.events;

import java.util.Date;

public class EditQuestRequestEvent {
    public final String questId;
    public final int position;
    public final Date due;

    public EditQuestRequestEvent(String questId, int position, Date due) {
        this.questId = questId;
        this.position = position;
        this.due = due;
    }
}
