package io.ipoli.android.quest.reminders.persistence;

import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.quest.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/1/16.
 */
public interface ReminderPersistenceService extends PersistenceService<Reminder> {
    List<Reminder> findNextReminders();
}
