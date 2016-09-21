package io.ipoli.android.quest.ui.events;

import java.util.List;

import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.reminder.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/16/16.
 */
public class UpdateRepeatingQuestEvent {
    public final RepeatingQuest repeatingQuest;
    public final List<Reminder> reminders;
    public final EventSource source;

    public UpdateRepeatingQuestEvent(RepeatingQuest repeatingQuest, List<Reminder> reminders, EventSource source) {
        this.repeatingQuest = repeatingQuest;
        this.reminders = reminders;
        this.source = source;
    }
}