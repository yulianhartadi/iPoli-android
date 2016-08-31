package io.ipoli.android.quest.events;

import java.util.List;

import io.ipoli.android.reminders.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/7/16.
 */
public class NewRepeatingQuestEvent {
    public final RepeatingQuest repeatingQuest;
    public final List<Reminder> reminders;

    public NewRepeatingQuestEvent(RepeatingQuest repeatingQuest, List<Reminder> reminders) {
        this.repeatingQuest = repeatingQuest;
        this.reminders = reminders;
    }
}