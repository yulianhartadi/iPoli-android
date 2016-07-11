package io.ipoli.android.quest.persistence;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SubQuest;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDay;
import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class RealmQuestPersistenceService extends BaseRealmPersistenceService<Quest> implements QuestPersistenceService {

    private final Bus eventBus;

    public RealmQuestPersistenceService(Bus eventBus, Realm realm) {
        super(realm);
        this.eventBus = eventBus;
    }

    @Override
    protected Class<Quest> getRealmObjectClass() {
        return Quest.class;
    }

    @Override
    public List<Quest> findAllIncompleteToDosBefore(LocalDate localDate) {
        return findAll(where -> where
                .isNull("completedAt")
                .isNull("repeatingQuest")
                .equalTo("allDay", false)
                .lessThan("endDate", toStartOfDayUTC(localDate))
                .findAllSorted(new String[]{"endDate", "startMinute", "createdAt"}, new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.DESCENDING}));
    }

    @Override
    protected void onObjectSaved(Quest object) {
        eventBus.post(new QuestSavedEvent(object));
    }

    @Override
    public void findAllUnplanned(OnDatabaseChangedListener<Quest> listener) {
        listenForChanges(where()
                .isNull("endDate")
                .isNull("actualStart")
                .isNull("completedAt")
                .findAllSortedAsync("createdAt", Sort.DESCENDING), listener);
    }

    @Override
    public List<Quest> findAllPlannedAndStartedToday() {

        LocalDate today = LocalDate.now();

        Date startOfToday = toStartOfDayUTC(today);
        Date startOfTomorrow = toStartOfDayUTC(today.plusDays(1));

        return findAll(where -> where
                .greaterThanOrEqualTo("endDate", startOfToday)
                .lessThan("endDate", startOfTomorrow)
                .isNull("completedAt")
                .findAllSorted("startMinute", Sort.ASCENDING));
    }

    @Override
    public long countCompletedQuests(RepeatingQuest repeatingQuest, LocalDate fromDate, LocalDate toDate) {
        getRealm().beginTransaction();
        long count = where()
                .isNotNull("completedAt")
                .equalTo("repeatingQuest.id", repeatingQuest.getId())
                .between("endDate", toStartOfDayUTC(fromDate), toStartOfDayUTC(toDate))
                .count();
        getRealm().commitTransaction();
        return count;
    }

    @Override
    public Date findNextUncompletedQuestEndDate(RepeatingQuest repeatingQuest) {
        List<Quest> quests = findAll(where -> where
                .isNull("completedAt")
                .equalTo("repeatingQuest.id", repeatingQuest.getId())
                .greaterThanOrEqualTo("endDate", toStartOfDayUTC(LocalDate.now()))
                .findAllSorted("endDate"));
        if(!quests.isEmpty()) {
            return quests.get(0).getEndDate();
        }
        return null;
    }

    @Override
    public void findById(String questId, OnSingleDatabaseObjectChangedListener<Quest> listener) {
        listenForChanges(where()
                .equalTo("id", questId)
                .findFirstAsync(), listener);
    }

    @Override
    public void findAllNonAllDayForDate(LocalDate currentDate, OnDatabaseChangedListener<Quest> listener) {
        Date startDate = toStartOfDay(currentDate);
        Date endDate = toStartOfDay(currentDate.plusDays(1));
        Date startDateUTC = toStartOfDayUTC(currentDate);
        Date endDateUTC = toStartOfDayUTC(currentDate.plusDays(1));
        listenForChanges(where()
                .beginGroup()
                .greaterThanOrEqualTo("endDate", startDateUTC)
                .lessThan("endDate", endDateUTC)
                .or()
                .greaterThanOrEqualTo("completedAt", startDate)
                .lessThan("completedAt", endDate)
                .endGroup()
                .equalTo("allDay", false)
                .findAllSortedAsync("startMinute", Sort.ASCENDING), listener);
    }

    @Override
    public void findAllNonAllDayCompletedForDate(LocalDate currentDate, OnDatabaseChangedListener<Quest> listener) {
        Date startDate = toStartOfDay(currentDate);
        Date endDate = toStartOfDay(currentDate.plusDays(1));
        listenForChanges(where()
                .greaterThanOrEqualTo("completedAt", startDate)
                .lessThan("completedAt", endDate)
                .equalTo("allDay", false)
                .findAllSortedAsync("startMinute", Sort.ASCENDING), listener);
    }

    @Override
    public void findAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDatabaseChangedListener<Quest> listener) {
        Date startDate = toStartOfDayUTC(currentDate);
        Date endDate = toStartOfDayUTC(currentDate.plusDays(1));
        listenForChanges(where()
                .greaterThanOrEqualTo("endDate", startDate)
                .lessThan("endDate", endDate)
                .isNull("completedAt")
                .equalTo("allDay", false)
                .findAllSortedAsync("startMinute", Sort.ASCENDING), listener);
    }

    @Override
    public List<Quest> findAllNonAllDayIncompleteForDateSync(LocalDate currentDate) {
        Date startDate = toStartOfDayUTC(currentDate);
        Date endDate = toStartOfDayUTC(currentDate.plusDays(1));
        return findAll(where -> where
                .greaterThanOrEqualTo("endDate", startDate)
                .lessThan("endDate", endDate)
                .isNull("completedAt")
                .equalTo("allDay", false)
                .findAllSorted("startMinute", Sort.ASCENDING));
    }

    @Override
    public Quest findByExternalSourceMappingId(String source, String sourceId) {
        return findOne(where -> where.equalTo("sourceMapping." + source, sourceId)
                .findFirst());
    }

    @Override
    public List<Quest> findAllUpcomingForRepeatingQuest(LocalDate startDate, RepeatingQuest repeatingQuest) {
        Date startDateUtc = toStartOfDayUTC(startDate);
        return findAllIncludingDeleted(where -> where
                .equalTo("repeatingQuest.id", repeatingQuest.getId())
                .beginGroup()
                .isNull("endDate")
                .or()
                .greaterThanOrEqualTo("endDate", startDateUtc)
                .endGroup()
                .findAll());
    }

    @Override
    public long countAllCompletedWithPriorityForDate(int priority, LocalDate date) {
        Date dateUtc = toStartOfDayUTC(date);
        getRealm().beginTransaction();
        long count = where()
                .equalTo("priority", priority)
                .isNotNull("completedAt")
                .equalTo("endDate", dateUtc)
                .count();
        getRealm().commitTransaction();
        return count;
    }

    @Override
    public List<Quest> findAllForChallenge(Challenge challenge) {
        return findAllIncludingDeleted(where -> where
                .equalTo("challenge.id", challenge.getId())
                .findAll());
    }

    @Override
    public Quest findByReminderId(String reminderId) {
        return findOne(where -> where.equalTo("reminders.id", reminderId).findFirst());
    }

    @Override
    public void findAllIncompleteOrMostImportantForDate(LocalDate date, OnDatabaseChangedListener<Quest> listener) {
        Date startDateUTC = toStartOfDayUTC(date);
        Date endDateUTC = toStartOfDayUTC(date.plusDays(1));
        listenForChanges(where()
                .greaterThanOrEqualTo("endDate", startDateUTC)
                .lessThan("endDate", endDateUTC)
                .beginGroup()
                .isNull("completedAt")
                .or()
                .equalTo("priority", Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY)
                .endGroup()
                .equalTo("allDay", false)
                .findAllSortedAsync("startMinute", Sort.ASCENDING), listener);
    }

    @Override
    public List<Quest> findAllForRepeatingQuest(RepeatingQuest repeatingQuest) {
        return findAll(where -> where
                .equalTo("repeatingQuest.id", repeatingQuest.getId())
                .findAll());
    }

    @Override
    public long countAllForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate) {
        getRealm().beginTransaction();
        long count = where()
                .equalTo("repeatingQuest.id", repeatingQuest.getId())
                .between("originalStartDate", toStartOfDayUTC(startDate), toStartOfDayUTC(endDate))
                .count();
        getRealm().commitTransaction();
        return count;
    }

    @Override
    public long countAllScheduledForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate) {
        getRealm().beginTransaction();
        long count = where()
                .equalTo("repeatingQuest.id", repeatingQuest.getId())
                .between("endDate", toStartOfDayUTC(startDate), toStartOfDayUTC(endDate))
                .count();
        getRealm().commitTransaction();
        return count;
    }

    @Override
    public void findPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDatabaseChangedListener<Quest> listener) {
        listenForChanges(where()
                .greaterThanOrEqualTo("endDate", toStartOfDayUTC(startDate))
                .lessThan("endDate", toStartOfDayUTC(endDate))
                .equalTo("allDay", false)
                .isNull("completedAt")
                .findAllSortedAsync("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING), listener);
    }

    @Override
    public List<Quest> findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate) {
        return findAll(where -> where
                .greaterThanOrEqualTo("completedAt", toStartOfDay(startDate))
                .lessThan("completedAt", toStartOfDay(endDate))
                .equalTo("allDay", false)
                .findAllSorted("completedAt", Sort.ASCENDING));
    }

    @Override
    public void saveReminders(Quest quest, List<Reminder> reminders) {
        saveReminders(quest, reminders, true);
    }

    @Override
    public void saveReminders(Quest quest, List<Reminder> reminders, boolean markUpdated) {
        getRealm().executeTransaction(realm -> {
            if (markUpdated) {
                for (Reminder r : reminders) {
                    r.markUpdated();
                }
            }
            if (quest.getReminders() != null && !quest.getReminders().isEmpty()) {
                RealmList<Reminder> realmReminders = realm.where(getRealmObjectClass()).equalTo("id", quest.getId()).findFirst().getReminders();
                if (realmReminders != null) {
                    realmReminders.deleteAllFromRealm();
                }
            }
            RealmList<Reminder> reminderRealmList = new RealmList<>();
            reminderRealmList.addAll(reminders);
            quest.setReminders(reminderRealmList);
            quest.updateRemindersStartTime();
        });
    }

    @Override
    public void saveSubQuests(Quest quest, List<SubQuest> subQuests) {
        saveSubQuests(quest, subQuests, true);
    }

    @Override
    public void saveSubQuests(Quest quest, List<SubQuest> subQuests, boolean markUpdated) {
        getRealm().executeTransaction(realm -> {
            if (markUpdated) {
                for (SubQuest sq : subQuests) {
                    sq.markUpdated();
                }
            }
            if (quest.getSubQuests() != null && !quest.getSubQuests().isEmpty()) {
                RealmList<SubQuest> realmSubQuests = realm.where(getRealmObjectClass()).equalTo("id", quest.getId()).findFirst().getSubQuests();
                if (realmSubQuests != null) {
                    realmSubQuests.deleteAllFromRealm();
                }
            }
            RealmList<SubQuest> subQuestRealmList = new RealmList<>();
            subQuestRealmList.addAll(subQuests);
            quest.setSubQuests(subQuestRealmList);
        });
    }

    @Override
    public Observable<Void> delete(List<Quest> objects) {
        if (objects.isEmpty()) {
            return Observable.empty();
        }
        return Observable.create(subscriber -> {
            Realm realm = getRealm();
            realm.executeTransactionAsync(backgroundRealm -> {
                        RealmQuery<Quest> q = backgroundRealm.where(getRealmObjectClass());
                        for (int i = 0; i < objects.size(); i++) {
                            if (i > 0) {
                                q = q.or();
                            }
                            q = q.equalTo("id", objects.get(i).getId());
                        }
                        RealmResults<Quest> results = q.findAll();
                        for (Quest quest : results) {
                            if (quest.getReminders() != null) {
                                quest.getReminders().deleteAllFromRealm();
                            }
                            if(quest.getSubQuests() != null) {
                                quest.getSubQuests().deleteAllFromRealm();
                            }
                        }
                        results.deleteAllFromRealm();
                    },
                    () -> {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    }, subscriber::onError);
        });
    }
}