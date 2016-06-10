package io.ipoli.android.quest.persistence;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.realm.Realm;
import io.realm.Sort;
import rx.Observable;

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
    public Observable<List<Quest>> findAllIncompleteToDosBefore(LocalDate localDate) {
        return findAll(where -> where
                .isNull("completedAt")
                .isNull("repeatingQuest")
                .equalTo("allDay", false)
                .lessThan("endDate", toStartOfDayUTC(localDate))
                .findAllSortedAsync(new String[]{"endDate", "startMinute", "createdAt"}, new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.DESCENDING}));
    }

    @Override
    protected void onObjectSaved(Quest object) {
        eventBus.post(new QuestSavedEvent(object));
    }

    @Override
    public void findAllUnplanned(OnDatabaseChangedListener<Quest> listener) {
        listenForResults(where()
                .isNull("endDate")
                .isNull("actualStart")
                .isNull("completedAt")
                .findAllSortedAsync("createdAt", Sort.DESCENDING), listener);
    }

    @Override
    public Observable<List<Quest>> findAllPlannedAndStartedToday() {

        LocalDate today = LocalDate.now();

        Date startOfToday = toStartOfDayUTC(today);
        Date startOfTomorrow = toStartOfDayUTC(today.plusDays(1));

        return findAll(where -> where
                .greaterThanOrEqualTo("endDate", startOfToday)
                .lessThan("endDate", startOfTomorrow)
                .isNull("completedAt")
                .findAllSortedAsync("startMinute", Sort.ASCENDING));
    }

    @Override
    public long countCompletedQuests(RepeatingQuest repeatingQuest, LocalDate fromDate, LocalDate toDate) {

        return where()
                .isNotNull("completedAt")
                .equalTo("repeatingQuest.id", repeatingQuest.getId())
                .between("endDate", toStartOfDayUTC(fromDate), toStartOfDayUTC(toDate))
                .count();
    }

    @Override
    public Observable<List<Quest>> findAllNonAllDayForDate(LocalDate currentDate) {
        Date startDate = toStartOfDayUTC(currentDate);
        Date endDate = toStartOfDayUTC(currentDate.plusDays(1));

        return findAll(where -> where.beginGroup()
                .greaterThanOrEqualTo("endDate", startDate)
                .lessThan("endDate", endDate)
                .or()
                .greaterThanOrEqualTo("completedAt", startDate)
                .lessThan("completedAt", endDate)
                .endGroup()
                .equalTo("allDay", false)
                .findAllSortedAsync("startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllNonAllDayCompletedForDate(LocalDate currentDate) {
        Date startDate = toStartOfDayUTC(currentDate);
        Date endDate = toStartOfDayUTC(currentDate.plusDays(1));
        return findAll(where -> where
                .greaterThanOrEqualTo("completedAt", startDate)
                .lessThan("completedAt", endDate)
                .equalTo("allDay", false)
                .findAllSortedAsync("startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllNonAllDayIncompleteForDate(LocalDate currentDate) {
        Date startDate = toStartOfDayUTC(currentDate);
        Date endDate = toStartOfDayUTC(currentDate.plusDays(1));
        return findAll(where -> where
                .greaterThanOrEqualTo("endDate", startDate)
                .lessThan("endDate", endDate)
                .isNull("completedAt")
                .equalTo("allDay", false)
                .findAllSortedAsync("startMinute", Sort.ASCENDING));
    }

    @Override
    public List<Quest> findAllNonAllDayIncompleteForDateSync(LocalDate currentDate) {
        Date startDate = toStartOfDayUTC(currentDate);
        Date endDate = toStartOfDayUTC(currentDate.plusDays(1));
        try (Realm realm = getRealm()) {
            return realm.copyFromRealm(realm.where(getRealmObjectClass())
                    .greaterThanOrEqualTo("endDate", startDate)
                    .lessThan("endDate", endDate)
                    .isNull("completedAt")
                    .equalTo("allDay", false)
                    .findAllSorted("startMinute", Sort.ASCENDING));
        }
    }

    @Override
    public Quest findByExternalSourceMappingIdSync(String source, String sourceId) {
        Realm realm = getRealm();
        Quest quest = realm.where(getRealmObjectClass())
                .equalTo("sourceMapping." + source, sourceId)
                .findFirst();
        if (quest == null) {
            return null;
        }
        return realm.copyFromRealm(quest);
    }

    @Override
    public Observable<Quest> findByExternalSourceMappingId(String source, String sourceId) {
        return find(where -> where
                .equalTo("sourceMapping." + source, sourceId)
                .findFirstAsync());
    }

    @Override
    public List<Quest> findAllForRepeatingQuest(RepeatingQuest repeatingQuest) {
        return getRealm().copyFromRealm(where()
                .equalTo("repeatingQuest.id", repeatingQuest.getId())
                .findAll());
    }

    @Override
    public long countAllForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate) {
        return where()
                .equalTo("repeatingQuest.id", repeatingQuest.getId())
                .between("originalStartDate", toStartOfDayUTC(startDate), toStartOfDayUTC(endDate))
                .count();
    }

    @Override
    public Observable<List<Quest>> findPlannedQuestsStartingAfter(LocalDate localDate) {
        return findAll(where -> where
                .greaterThanOrEqualTo("endDate", toStartOfDayUTC(localDate))
                .greaterThanOrEqualTo("startMinute", Time.now().toMinutesAfterMidnight())
                .isNull("actualStart")
                .isNull("completedAt")
                .findAllSortedAsync("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING));
    }

    @Override
    public void findPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDatabaseChangedListener<Quest> listener) {
        listenForResults(where()
                .greaterThanOrEqualTo("endDate", toStartOfDayUTC(startDate))
                .lessThan("endDate", toStartOfDayUTC(endDate))
                .equalTo("allDay", false)
                .isNull("completedAt")
                .findAllSortedAsync("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING), listener);
    }

    @Override
    public Observable<List<Quest>> findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate) {
        return findAll(where -> where
                .greaterThanOrEqualTo("completedAt", toStartOfDayUTC(startDate))
                .lessThan("completedAt", toStartOfDayUTC(endDate))
                .equalTo("allDay", false)
                .findAllSortedAsync("completedAt", Sort.ASCENDING));
    }
}