package io.ipoli.android.app.services.readers;

import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class RealmHabitListReader implements ListReader<RecurrentQuest> {

    private final RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    public RealmHabitListReader(RecurrentQuestPersistenceService recurrentQuestPersistenceService) {
        this.recurrentQuestPersistenceService = recurrentQuestPersistenceService;
    }

    public Observable<RecurrentQuest> read() {
        return recurrentQuestPersistenceService.findAllWhoNeedSyncWithRemote().concatMapIterable(recurrentQuests -> recurrentQuests);
    }
}
