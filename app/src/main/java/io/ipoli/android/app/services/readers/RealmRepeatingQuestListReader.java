package io.ipoli.android.app.services.readers;

import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class RealmRepeatingQuestListReader implements ListReader<RepeatingQuest> {

    private final RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    public RealmRepeatingQuestListReader(RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        this.repeatingQuestPersistenceService = repeatingQuestPersistenceService;
    }

    public Observable<RepeatingQuest> read() {
        return repeatingQuestPersistenceService.findAllWhoNeedSyncWithRemote().concatMapIterable(repeatingQuest -> repeatingQuest);
    }
}