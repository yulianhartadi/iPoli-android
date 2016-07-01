package io.ipoli.android.quest.persistence;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.RepeatingQuestSavedEvent;
import io.realm.Realm;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public class RealmRepeatingQuestPersistenceService extends BaseRealmPersistenceService<RepeatingQuest> implements RepeatingQuestPersistenceService {

    private final Bus eventBus;

    public RealmRepeatingQuestPersistenceService(Bus eventBus, Realm realm) {
        super(realm);
        this.eventBus = eventBus;
    }

    @Override
    protected Class<RepeatingQuest> getRealmObjectClass() {
        return RepeatingQuest.class;
    }

    @Override
    protected void onObjectSaved(RepeatingQuest object) {
        eventBus.post(new RepeatingQuestSavedEvent(object));
    }

    @Override
    public List<RepeatingQuest> findAllNonAllDayActiveRepeatingQuests() {
        return findAll(where -> where.isNotNull("name")
                .equalTo("allDay", false)
                .isNotNull("recurrence.rrule")
                .beginGroup()
                .isNull("recurrence.dtend")
                .or()
                .greaterThanOrEqualTo("recurrence.dtend", toStartOfDayUTC(LocalDate.now()))
                .endGroup()
                .findAll());
    }

    @Override
    public void findAllNonAllDayActiveRepeatingQuests(OnDatabaseChangedListener<RepeatingQuest> listener) {
        listenForChanges(where().isNotNull("name")
                .equalTo("allDay", false)
                .isNotNull("recurrence.rrule")
                .beginGroup()
                .isNull("recurrence.dtend")
                .or()
                .greaterThanOrEqualTo("recurrence.dtend", toStartOfDayUTC(LocalDate.now()))
                .endGroup()
                .findAllAsync(), listener);
    }

    @Override
    public RepeatingQuest findByExternalSourceMappingId(String source, String sourceId) {
        return findOne(where -> where.equalTo("sourceMapping." + source, sourceId)
                .findFirst());
    }

    @Override
    public List<RepeatingQuest> findAllForChallenge(Challenge challenge) {
        return findAllIncludingDeleted(where -> where
                .equalTo("challenge.id", challenge.getId())
                .findAll());
    }

    @Override
    public void saveReminders(RepeatingQuest repeatingQuest, List<Reminder> reminders) {
        getRealm().executeTransaction(realm -> {
            int notificationId = repeatingQuest.getReminders() == null || repeatingQuest.getReminders().isEmpty() ? new Random().nextInt() : repeatingQuest.getReminders().get(0).getNotificationId();
            List<Reminder> remindersToSave = new ArrayList<>();
            for (Reminder newReminder : reminders) {
                boolean isEdited = false;
                for (Reminder dbReminder : repeatingQuest.getReminders()) {
                    if (newReminder.getId().equals(dbReminder.getId())) {
                        dbReminder.setMessage(newReminder.getMessage());
                        dbReminder.setMinutesFromStart(newReminder.getMinutesFromStart());
                        dbReminder.markUpdated();
                        remindersToSave.add(dbReminder);
                        isEdited = true;
                        break;
                    }
                }
                if (!isEdited) {
                    if (newReminder.getNotificationId() == null) {
                        newReminder.setNotificationId(notificationId);
                    }
                    remindersToSave.add(newReminder);
                }
            }
            repeatingQuest.getReminders().clear();
            repeatingQuest.getReminders().addAll(remindersToSave);
        });
    }
}
