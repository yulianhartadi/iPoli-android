package io.ipoli.android.quest.persistence;

import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.quest.data.RecurrentQuest;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public interface RecurrentQuestPersistenceService extends PersistenceService<RecurrentQuest> {

    Observable<List<RecurrentQuest>> findAll();
    Observable<List<RecurrentQuest>> findAllWhoNeedSyncWithRemote();
    Observable<RecurrentQuest> findByRemoteId(String remoteId);
    Observable<List<RecurrentQuest>>findAllHabits();
}
