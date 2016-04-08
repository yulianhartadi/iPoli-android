package io.ipoli.android.quest.persistence;

import com.squareup.otto.Bus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.app.utils.DateUtils;
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
    public Observable<List<Quest>> findAllUncompleted() {
        return fromRealm(where()
                .isNull("completedAtDateTime")
                .findAllSorted("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING, "createdAt", Sort.DESCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllUnplanned() {
        return fromRealm(where()
                .isNull("endDate")
                .isNull("actualStartDateTime")
                .isNull("completedAtDateTime")
                .findAllSorted("createdAt", Sort.DESCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllPlannedAndStartedToday() {
        Calendar yesterday = DateUtils.getTodayAtMidnight();
        yesterday.add(Calendar.SECOND, -1);

        Calendar tomorrow = DateUtils.getTodayAtMidnight();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        return fromRealm(where()
                .greaterThan("endDate", yesterday.getTime())
                .lessThan("endDate", tomorrow.getTime())
                .isNull("completedAtDateTime")
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
    public long countCompletedQuests(RecurrentQuest recurrentQuest, Date fromDate, Date toDate) {
        return where()
                .isNotNull("completedAtDateTime")
                .equalTo("recurrentQuest.id", recurrentQuest.getId())
                .between("endDate", fromDate, toDate)
                .count();
    }

    @Override
    public Observable<Quest> findPlannedQuestStartingAfter(Date date) {
        Calendar yesterday = DateUtils.getTodayAtMidnight();
        yesterday.add(Calendar.SECOND, -1);

        RealmResults<Quest> quests = where()
                .greaterThan("endDate", yesterday.getTime())
                .greaterThanOrEqualTo("startMinute", Time.now().toMinutesAfterMidnight())
                .isNull("actualStartDateTime")
                .isNull("completedAtDateTime")
                .findAllSorted("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING);
        if (quests.isEmpty()) {
            return Observable.just(null);
        }
        return fromRealm(quests.first());
    }

    @Override
    public Observable<List<Quest>> findAllPlannedForToday() {
        Calendar yesterday = DateUtils.getTodayAtMidnight();
        yesterday.add(Calendar.SECOND, -1);

        Calendar tomorrow = DateUtils.getTodayAtMidnight();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        return fromRealm(where()
                .beginGroup()
                .greaterThan("endDate", yesterday.getTime())
                .lessThan("endDate", tomorrow.getTime())
                .or()
                .greaterThan("completedAtDateTime", yesterday.getTime())
                .lessThan("completedAtDateTime", tomorrow.getTime())
                .endGroup()
                .findAllSorted("startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllCompleted() {
        return fromRealm(where()
                .isNotNull("completedAtDateTime")
                .findAllSorted("completedAtDateTime", Sort.DESCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllPlanned() {
        return fromRealm(where()
                .isNotNull("endDate")
                .isNull("completedAtDateTime")
                .findAllSorted("endDate", Sort.ASCENDING, "startMinute", Sort.ASCENDING));
    }

    @Override
    public Observable<List<Quest>> findAllCompletedToday() {
        Calendar yesterday = DateUtils.getTodayAtMidnight();
        yesterday.add(Calendar.SECOND, -1);

        Calendar tomorrow = DateUtils.getTodayAtMidnight();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        return fromRealm(where()
                .greaterThan("completedAtDateTime", yesterday.getTime())
                .lessThan("completedAtDateTime", tomorrow.getTime())
                .findAll());
    }
}