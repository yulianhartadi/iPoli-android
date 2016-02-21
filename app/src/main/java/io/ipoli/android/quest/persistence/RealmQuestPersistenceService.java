package io.ipoli.android.quest.persistence;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.persistence.events.QuestsSavedEvent;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class RealmQuestPersistenceService implements QuestPersistenceService {

    private final Bus eventBus;
    private Realm realm;

    public RealmQuestPersistenceService(Context context, Bus eventBus) {
        this.eventBus = eventBus;
        realm = Realm.getInstance(context);
    }

    @Override
    public Quest save(Quest quest) {
        realm.beginTransaction();
        Quest resultQuest = realm.copyFromRealm(realm.copyToRealmOrUpdate(quest));
        realm.commitTransaction();
        eventBus.post(new QuestSavedEvent(resultQuest));
        return resultQuest;
    }

    @Override
    public List<Quest> saveAll(List<Quest> quests) {
        realm.beginTransaction();
        List<Quest> resultQuests = realm.copyFromRealm(realm.copyToRealmOrUpdate(quests));
        realm.commitTransaction();
        eventBus.post(new QuestsSavedEvent(resultQuests));
        return resultQuests;
    }

    @Override
    public List<Quest> findAllUncompleted() {
        return realm.copyFromRealm(realm.where(Quest.class)
                .isNull("completedAtDateTime")
                .findAllSorted("due", Sort.ASCENDING, "startTime", Sort.ASCENDING, "createdAt", Sort.DESCENDING));
    }

    @Override
    public List<Quest> findAllUnplanned() {
        return realm.copyFromRealm(realm.where(Quest.class)
                .isNull("due")
                .isNull("actualStartDateTime")
                .isNull("completedAtDateTime")
                .findAllSorted("createdAt", Sort.DESCENDING));
    }

    @Override
    public List<Quest> findAllPlannedAndStartedToday() {
        Calendar yesterday = DateUtils.getTodayAtMidnight();
        yesterday.add(Calendar.SECOND, -1);

        Calendar tomorrow = DateUtils.getTodayAtMidnight();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        return realm.copyFromRealm(realm.where(Quest.class)
                .greaterThan("due", yesterday.getTime())
                .lessThan("due", tomorrow.getTime())
                .isNull("completedAtDateTime")
                .findAllSorted("startTime", Sort.ASCENDING));
    }

    @Override
    public List<Quest> findAllForToday() {
        Calendar yesterday = DateUtils.getTodayAtMidnight();
        yesterday.add(Calendar.SECOND, -1);

        Calendar tomorrow = DateUtils.getTodayAtMidnight();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        return realm.copyFromRealm(realm.where(Quest.class)
                .greaterThan("due", yesterday.getTime())
                .lessThan("due", tomorrow.getTime())
                .findAll());
    }

    @Override
    public long countAllUncompleted() {
        return realm.where(Quest.class)
                .isNull("completedAtDateTime")
                .count();
    }

    @Override
    public long countAllPlannedForToday() {
        Calendar yesterday = DateUtils.getTodayAtMidnight();
        yesterday.add(Calendar.SECOND, -1);

        Calendar tomorrow = DateUtils.getTodayAtMidnight();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        return realm.where(Quest.class)
                .greaterThan("due", yesterday.getTime())
                .lessThan("due", tomorrow.getTime())
                .isNull("completedAtDateTime")
                .count();
    }

    @Override
    public void delete(Quest quest) {
        if (quest == null) {
            return;
        }
        realm.beginTransaction();
        Quest realmQuest = realm.where(Quest.class)
                .equalTo("id", quest.getId())
                .findFirst();
        if (realmQuest == null) {
            realm.cancelTransaction();
            return;
        }
        realmQuest.removeFromRealm();
        realm.commitTransaction();
        eventBus.post(new QuestDeletedEvent());
    }

    @Override
    public void deleteByNames(String... names) {
        realm.beginTransaction();
        for (String name : names) {
            Quest realmQuest = realm.where(Quest.class)
                    .equalTo("name", name)
                    .findFirst();
            if (realmQuest != null) {
                realmQuest.removeFromRealm();
            }
        }
        realm.commitTransaction();
    }

    @Override
    public Quest findPlannedQuestStartingAfter(Date date) {
        Calendar yesterday = DateUtils.getTodayAtMidnight();
        yesterday.add(Calendar.SECOND, -1);

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);

        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(0);
        startTime.set(Calendar.HOUR_OF_DAY, dateCalendar.get(Calendar.HOUR_OF_DAY));
        startTime.set(Calendar.MINUTE, dateCalendar.get(Calendar.MINUTE));

        RealmResults<Quest> quests = realm.where(Quest.class)
                .greaterThan("due", yesterday.getTime())
                .greaterThanOrEqualTo("startTime", startTime.getTime())
                .isNull("actualStartDateTime")
                .isNull("completedAtDateTime")
                .findAllSorted("due", Sort.ASCENDING, "startTime", Sort.ASCENDING);
        if (quests.isEmpty()) {
            return null;
        }
        return realm.copyFromRealm(quests.first());
    }

    @Override
    public Quest findById(String id) {
        return realm.copyFromRealm(realm.where(Quest.class)
                .equalTo("id", id).findFirst());
    }

    @Override
    public List<Quest> findAllPlanned() {
        return realm.copyFromRealm(realm.where(Quest.class)
                .isNotNull("due")
                .isNull("completedAtDateTime")
                .findAllSorted("due", Sort.ASCENDING, "startTime", Sort.ASCENDING));
    }

}
