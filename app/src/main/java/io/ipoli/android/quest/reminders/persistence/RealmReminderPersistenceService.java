package io.ipoli.android.quest.reminders.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.quest.data.Reminder;
import io.realm.Realm;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/1/16.
 */
public class RealmReminderPersistenceService extends BaseRealmPersistenceService<Reminder> implements ReminderPersistenceService {

    public RealmReminderPersistenceService(Realm realm) {
        super(realm);
    }

    @Override
    protected Class<Reminder> getRealmObjectClass() {
        return Reminder.class;
    }

    @Override
    public List<Reminder> findNextReminders() {
        getRealm().beginTransaction();
        Date nextReminderStartTime = where()
                .greaterThanOrEqualTo("startTime", new Date())
                .minimumDate("startTime");
        getRealm().commitTransaction();

        if (nextReminderStartTime == null) {
            return new ArrayList<>();
        } else {
            return findAll(where -> where.equalTo("startTime", nextReminderStartTime).findAll());
        }
    }
}
