package io.ipoli.android.quest.persistence;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.persistence.events.QuestsSavedEvent;
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
    protected void onObjectSaved(Quest obj) {
        eventBus.post(new QuestSavedEvent(obj));
    }

    @Override
    protected void onObjectsSaved(List<Quest> objs) {
        eventBus.post(new QuestsSavedEvent(objs));
    }

    @Override
    protected Class<Quest> getRealmObjectClass() {
        return Quest.class;
    }

    @Override
    public Observable<List<Quest>> findAllIncompleteTodosBefore(LocalDate localDate) {
        return fromRealm(where()
                .isNull("completedAt")
                .lessThan("endDate", toUTCDateAtStartOfDay(localDate))
                .isNull("recurrentQuest")
                .findAllSorted("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING, "createdAt", Sort.DESCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllUnplanned() {
        return fromRealm(where()
                .isNull("endDate")
                .isNull("actualStartDateTime")
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
    public void delete(Quest quest) {
        if (quest == null) {
            return;
        }
        getRealm().beginTransaction();
        Quest realmQuest = where()
                .equalTo("id", quest.getId())
                .findFirst();
        if (realmQuest == null) {
            getRealm().cancelTransaction();
            return;
        }
        realmQuest.removeFromRealm();
        getRealm().commitTransaction();
        eventBus.post(new QuestDeletedEvent());
    }

    @Override
    public long countCompletedQuests(RecurrentQuest recurrentQuest, LocalDate fromDate, LocalDate toDate) {

        return where()
                .isNotNull("completedAt")
                .equalTo("recurrentQuest.id", recurrentQuest.getId())
                .between("endDate", toUTCDateAtStartOfDay(fromDate), toUTCDateAtStartOfDay(toDate))
                .count();
    }

    @Override
    public Observable<Quest> findPlannedQuestStartingAfter(LocalDate localDate) {

        RealmResults<Quest> quests = where()
                .greaterThanOrEqualTo("endDate", toUTCDateAtStartOfDay(localDate))
                .greaterThanOrEqualTo("startMinute", Time.now().toMinutesAfterMidnight())
                .isNull("actualStartDateTime")
                .isNull("completedAt")
                .findAllSorted("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING);
        if (quests.isEmpty()) {
            return Observable.just(null);
        }
        return fromRealm(quests.first());
    }

    @Override
    public Observable<List<Quest>> findAllForToday() {

        LocalDate today = new LocalDate();

        Date startOfToday = toUTCDateAtStartOfDay(today);
        Date startOfTomorrow = toUTCDateAtStartOfDay(today.plusDays(1));

        return fromRealm(where()
                .beginGroup()
                .greaterThanOrEqualTo("endDate", startOfToday)
                .lessThan("endDate", startOfTomorrow)
                .or()
                .greaterThan("completedAt", startOfToday)
                .lessThan("completedAt", startOfTomorrow)
                .endGroup()
                .findAllSorted("startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllCompleted() {
        return fromRealm(where()
                .isNotNull("completedAt")
                .findAllSorted("completedAt", Sort.DESCENDING));
    }

    @Override
    public Observable<List<Quest>> findPlannedBetween(LocalDate startDate, LocalDate endDate) {
        return fromRealm(where()
                .greaterThanOrEqualTo("endDate", toUTCDateAtStartOfDay(startDate))
                .lessThan("endDate", toUTCDateAtStartOfDay(endDate))
                .isNull("completedAt")
                .findAllSorted("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllCompletedToday() {

        LocalDate today = LocalDate.now();

        Date startOfToday = toUTCDateAtStartOfDay(today);
        Date startOfTomorrow = toUTCDateAtStartOfDay(today.plusDays(1));

        return fromRealm(where()
                .greaterThanOrEqualTo("completedAt", startOfToday)
                .lessThan("completedAt", startOfTomorrow)
                .findAll());
    }
}