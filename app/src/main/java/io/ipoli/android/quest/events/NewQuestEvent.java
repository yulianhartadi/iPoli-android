package io.ipoli.android.quest.events;

import java.util.List;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class NewQuestEvent {
    public final Quest quest;
    public final List<Reminder> reminders;
    public final EventSource source;

    public NewQuestEvent(Quest quest, List<Reminder> reminders, EventSource source) {
        this.quest = quest;
        this.reminders = reminders;
        this.source = source;
    }
}
