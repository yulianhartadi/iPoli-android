package io.ipoli.android.quest.events;

import java.util.List;

import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/17.
 */
public class NewQuestRemindersPickedEvent {

    public final List<Reminder> reminders;

    public NewQuestRemindersPickedEvent(List<Reminder> reminders) {
        this.reminders = reminders;
    }
}
