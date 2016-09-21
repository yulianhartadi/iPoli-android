package io.ipoli.android.quest.ui.events;

import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/16.
 */
public class QuestReminderPickedEvent {
    public final Reminder reminder;
    public final String reminderEditMode;
    public final String questEditMode;

    public QuestReminderPickedEvent(Reminder reminder, String reminderEditMode, String questEditMode) {
        this.reminder = reminder;
        this.reminderEditMode = reminderEditMode;
        this.questEditMode = questEditMode;
    }
}
