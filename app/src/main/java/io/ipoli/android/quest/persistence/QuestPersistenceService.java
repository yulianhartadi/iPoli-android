package io.ipoli.android.quest.persistence;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RecurrentQuest;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService extends PersistenceService<Quest> {
    Observable<Quest> findById(String id);

    Observable<Quest> findByRemoteId(String remoteId);

    Observable<List<Quest>> findAllCompleted();

    Observable<List<Quest>> findAllUnplanned();

    Observable<List<Quest>> findAllPlanned();

    Observable<List<Quest>> findAllPlannedAndStartedToday();

    Observable<List<Quest>> findAllUncompleted();

    Observable<List<Quest>> findAllCompletedToday();

    Observable<Quest> findPlannedQuestStartingAfter(Date date);

    Observable<List<Quest>> findAllPlannedForToday();

    void delete(Quest quest);

    Observable<List<Quest>> findAllWhoNeedSyncWithRemote();

    long countCompletedQuests(RecurrentQuest recurrentQuest, Date fromDate, Date toDate);
}
