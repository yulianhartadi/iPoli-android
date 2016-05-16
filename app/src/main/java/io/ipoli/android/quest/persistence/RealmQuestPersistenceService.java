package io.ipoli.android.quest.persistence;

import android.text.TextUtils;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Habit;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class RealmQuestPersistenceService extends BaseRealmPersistenceService<Quest> implements QuestPersistenceService {

    private final Bus eventBus;

    public RealmQuestPersistenceService(Bus eventBus) {
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
                .isNull("habit")
                .equalTo("allDay", false)
                .lessThan("endDate", toUTCDateAtStartOfDay(localDate))
                .findAllSortedAsync(new String[]{"endDate", "startMinute", "createdAt"}, new Sort[]{Sort.ASCENDING, Sort.ASCENDING, Sort.DESCENDING}));
    }

    @Override
    protected void onObjectSaved(Quest object) {
        eventBus.post(new QuestSavedEvent(object));
    }

    @Override
    protected void onObjectDeleted(String id) {
        eventBus.post(new QuestDeletedEvent(id));
    }

    @Override
    public Observable<List<Quest>> findAllUnplanned() {
        return findAll(where -> where
                .isNull("endDate")
                .isNull("actualStart")
                .isNull("completedAt")
                .findAllSortedAsync("createdAt", Sort.DESCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllPlannedAndStartedToday() {

        LocalDate today = LocalDate.now();

        Date startOfToday = toUTCDateAtStartOfDay(today);
        Date startOfTomorrow = toUTCDateAtStartOfDay(today.plusDays(1));

        return findAll(where -> where
                .greaterThanOrEqualTo("endDate", startOfToday)
                .lessThan("endDate", startOfTomorrow)
                .isNull("completedAt")
                .findAllSortedAsync("startMinute", Sort.ASCENDING));
    }


    @Override
    public Observable<String> deleteBySourceMappingId(String source, String sourceId) {
        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(sourceId)) {
            return Observable.empty();
        }

        Realm realm = getRealm();

        Quest realmQuest = realm.where(getRealmObjectClass())
                .equalTo("sourceMapping." + source, sourceId)
                .findFirst();

        if (realmQuest == null) {
            realm.close();
            return Observable.empty();
        }

        final String questId = realmQuest.getId();

        return Observable.create(subscriber -> {
            realm.executeTransactionAsync(backgroundRealm -> {
                        Quest questToDelete = backgroundRealm.where(getRealmObjectClass())
                                .equalTo("sourceMapping." + source, sourceId)
                                .findFirst();
                        questToDelete.deleteFromRealm();
                    },
                    () -> {
                        subscriber.onNext(questId);
                        subscriber.onCompleted();
                        onObjectDeleted(questId);
                        realm.close();
                    }, error -> {
                        subscriber.onError(error);
                        realm.close();
                    });
        });

    }

    @Override
    public Observable<Void> deleteAllFromHabit(String habitId) {
        return Observable.create(subscriber -> {
            Realm realm = getRealm();
            realm.executeTransactionAsync(backgroundRealm -> {
                RealmResults<Quest> questsToRemove = where().equalTo("habit.id", habitId).findAll();
                questsToRemove.deleteAllFromRealm();
            }, () -> {
                subscriber.onNext(null);
                subscriber.onCompleted();
                realm.close();
            }, error -> {
                subscriber.onError(error);
                realm.close();
            });
        });
    }

    @Override
    public long countCompletedQuests(Habit habit, LocalDate fromDate, LocalDate toDate) {

        return where()
                .isNotNull("completedAt")
                .equalTo("habit.id", habit.getId())
                .between("endDate", toUTCDateAtStartOfDay(fromDate), toUTCDateAtStartOfDay(toDate))
                .count();
    }

    @Override
    public Observable<List<Quest>> findAllNonAllDayForDate(LocalDate currentDate) {
        Date startDate = toUTCDateAtStartOfDay(currentDate);
        Date endDate = toUTCDateAtStartOfDay(currentDate.plusDays(1));

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
        Date startDate = toUTCDateAtStartOfDay(currentDate);
        Date endDate = toUTCDateAtStartOfDay(currentDate.plusDays(1));
        return findAll(where -> where
                .greaterThanOrEqualTo("completedAt", startDate)
                .lessThan("completedAt", endDate)
                .equalTo("allDay", false)
                .findAllSortedAsync("startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllNonAllDayIncompleteForDate(LocalDate currentDate) {
        Date startDate = toUTCDateAtStartOfDay(currentDate);
        Date endDate = toUTCDateAtStartOfDay(currentDate.plusDays(1));

        return findAll(where -> where
                .greaterThanOrEqualTo("endDate", startDate)
                .lessThan("endDate", endDate)
                .isNull("completedAt")
                .equalTo("allDay", false)
                .findAllSortedAsync("startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findPlannedQuestsStartingAfter(LocalDate localDate) {

        return findAll(where -> where
                .greaterThanOrEqualTo("endDate", toUTCDateAtStartOfDay(localDate))
                .greaterThanOrEqualTo("startMinute", Time.now().toMinutesAfterMidnight())
                .isNull("actualStart")
                .isNull("completedAt")
                .findAllSortedAsync("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate) {
        return findAll(where -> where
                .greaterThanOrEqualTo("endDate", toUTCDateAtStartOfDay(startDate))
                .lessThan("endDate", toUTCDateAtStartOfDay(endDate))
                .equalTo("allDay", false)
                .isNull("completedAt")
                .findAllSortedAsync("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING));
    }
}