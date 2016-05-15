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
        return fromRealm(where()
                .isNull("completedAt")
                .isNull("habit")
                .equalTo("allDay", false)
                .lessThan("endDate", toUTCDateAtStartOfDay(localDate))
                .findAllSorted("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING, "createdAt", Sort.DESCENDING));
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
        return fromRealm(where()
                .isNull("endDate")
                .isNull("actualStart")
                .isNull("completedAt")
                .findAllSorted("createdAt", Sort.DESCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllPlannedAndStartedToday() {

        LocalDate today = LocalDate.now();

        Date startOfToday = toUTCDateAtStartOfDay(today);
        Date startOfTomorrow = toUTCDateAtStartOfDay(today.plusDays(1));

        return fromRealm(where()
                .greaterThanOrEqualTo("endDate", startOfToday)
                .lessThan("endDate", startOfTomorrow)
                .isNull("completedAt")
                .findAllSorted("startMinute", Sort.ASCENDING));
    }


    @Override
    public void deleteBySourceMappingId(String source, String sourceId) {
        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(sourceId)) {
            return;
        }
        getRealm().beginTransaction();
        Quest realmQuest = where()
                .equalTo("sourceMapping." + source, sourceId)
                .findFirst();
        if (realmQuest == null) {
            getRealm().cancelTransaction();
            return;
        }
        realmQuest.deleteFromRealm();
        getRealm().commitTransaction();
        eventBus.post(new QuestDeletedEvent(realmQuest.getId()));
    }

    @Override
    public void deleteAllFromHabit(String habitId) {
        RealmResults<Quest> questsToRemove = where().equalTo("habit.id", habitId).findAll();
        getRealm().beginTransaction();
        questsToRemove.deleteAllFromRealm();
        getRealm().commitTransaction();
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
    public Observable<List<Quest>> findAllForDate(LocalDate currentDate) {
        Date startDate = toUTCDateAtStartOfDay(currentDate);
        Date endDate = toUTCDateAtStartOfDay(currentDate.plusDays(1));

        return fromRealm(where()
                .beginGroup()
                .greaterThanOrEqualTo("endDate", startDate)
                .lessThan("endDate", endDate)
                .or()
                .greaterThanOrEqualTo("completedAt", startDate)
                .lessThan("completedAt", endDate)
                .endGroup()
                .findAllSorted("startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllCompletedForDate(LocalDate currentDate) {
        Date startDate = toUTCDateAtStartOfDay(currentDate);
        Date endDate = toUTCDateAtStartOfDay(currentDate.plusDays(1));
        return fromRealm(where()
                .greaterThanOrEqualTo("completedAt", startDate)
                .lessThan("completedAt", endDate)
                .findAllSorted("startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllIncompleteForDate(LocalDate currentDate) {
        Date startDate = toUTCDateAtStartOfDay(currentDate);
        Date endDate = toUTCDateAtStartOfDay(currentDate.plusDays(1));

        return fromRealm(where()
                .greaterThanOrEqualTo("endDate", startDate)
                .lessThan("endDate", endDate)
                .isNull("completedAt")
                .findAllSorted("startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<Quest> findPlannedQuestStartingAfter(LocalDate localDate) {

        RealmResults<Quest> quests = where()
                .greaterThanOrEqualTo("endDate", toUTCDateAtStartOfDay(localDate))
                .greaterThanOrEqualTo("startMinute", Time.now().toMinutesAfterMidnight())
                .isNull("actualStart")
                .isNull("completedAt")
                .findAllSorted("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING);
        if (quests.isEmpty()) {
            return Observable.just(null);
        }
        return fromRealm(quests.first());
    }

    @Override
    public Observable<List<Quest>> findPlannedBetween(LocalDate startDate, LocalDate endDate) {
        return fromRealm(where()
                .greaterThanOrEqualTo("endDate", toUTCDateAtStartOfDay(startDate))
                .lessThan("endDate", toUTCDateAtStartOfDay(endDate))
                .isNull("completedAt")
                .findAllSorted("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING));
    }
}