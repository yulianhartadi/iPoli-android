package io.ipoli.android.quest.persistence;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.Subquest;
import io.ipoli.android.quest.events.RepeatingQuestSavedEvent;
import io.realm.Realm;
import io.realm.RealmList;

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
                .beginGroup()
                    .isNull("recurrence.dtend")
                    .or()
                    .greaterThanOrEqualTo("recurrence.dtend", toStartOfDayUTC(LocalDate.now()))
                .endGroup()
                .findAllAsync(), listener);
    }

    @Override
    public void findNonFlexibleNonAllDayActiveRepeatingQuests(OnDatabaseChangedListener<RepeatingQuest> listener) {
        listenForChanges(where().isNotNull("name")
                .equalTo("allDay", false)
                .equalTo("recurrence.flexibleCount", 0)
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
    public void setReminders(RepeatingQuest repeatingQuest, List<Reminder> reminders) {
        getRealm().executeTransaction(realm -> {
            RealmList<Reminder> reminderRealmList = new RealmList<>();
            reminderRealmList.addAll(reminders);
            repeatingQuest.setReminders(reminderRealmList);
        });
    }

    @Override
    public void saveReminders(RepeatingQuest repeatingQuest, List<Reminder> reminders) {
        saveReminders(repeatingQuest, reminders, true);
    }

    @Override
    public void saveReminders(RepeatingQuest repeatingQuest, List<Reminder> reminders, boolean markUpdated) {
        getRealm().executeTransaction(realm -> {
            if (markUpdated) {
                for (Reminder r : reminders) {
                    r.markUpdated();
                }
            }
            if (repeatingQuest.getReminders() != null && !repeatingQuest.getReminders().isEmpty()) {
                RealmList<Reminder> realmReminders = realm.where(getRealmObjectClass()).equalTo("id", repeatingQuest.getId()).findFirst().getReminders();
                if (realmReminders != null) {
                    realmReminders.deleteAllFromRealm();
                }
            }
            RealmList<Reminder> reminderRealmList = new RealmList<>();
            reminderRealmList.addAll(reminders);
            repeatingQuest.setReminders(reminderRealmList);
        });
    }

    @Override
    public void setSubQuests(RepeatingQuest repeatingQuest, List<Subquest> subquests) {
        getRealm().executeTransaction(realm -> {
            RealmList<Subquest> subquestRealmList = new RealmList<>();
            subquestRealmList.addAll(subquests);
            repeatingQuest.setSubquests(subquestRealmList);
        });
    }

    @Override
    public void saveSubQuests(RepeatingQuest repeatingQuest, List<Subquest> subquests) {
        saveSubQuests(repeatingQuest, subquests, true);
    }

    @Override
    public void saveSubQuests(RepeatingQuest repeatingQuest, List<Subquest> subquests, boolean markUpdated) {
        getRealm().executeTransaction(realm -> {
            if (markUpdated) {
                for (Subquest sq : subquests) {
                    sq.markUpdated();
                }
            }
            if (repeatingQuest.getSubquests() != null && !repeatingQuest.getSubquests().isEmpty()) {
                RealmList<Subquest> realmSubquests = realm.where(getRealmObjectClass()).equalTo("id", repeatingQuest.getId()).findFirst().getSubquests();
                if (realmSubquests != null) {
                    realmSubquests.deleteAllFromRealm();
                }
            }
            RealmList<Subquest> subquestRealmList = new RealmList<>();
            subquestRealmList.addAll(subquests);
            repeatingQuest.setSubquests(subquestRealmList);
        });
    }
}
