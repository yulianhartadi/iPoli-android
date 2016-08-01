package io.ipoli.android.quest.events;

import java.util.List;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.reminders.data.Reminder;
import io.ipoli.android.quest.data.SubQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/16.
 */
public class UpdateQuestEvent {
    public final Quest quest;
    public final List<SubQuest> subQuests;
    public final List<Reminder> reminders;
    public final EventSource source;

    public UpdateQuestEvent(Quest quest, List<Reminder> reminders, EventSource source) {
        this.quest = quest;
        this.reminders = reminders;
        this.source = source;
        this.subQuests = null;
    }

    public UpdateQuestEvent(Quest quest, List<SubQuest> subQuests, List<Reminder> reminders, EventSource source) {
        this.quest = quest;
        this.subQuests = subQuests;
        this.reminders = reminders;
        this.source = source;
    }
}
