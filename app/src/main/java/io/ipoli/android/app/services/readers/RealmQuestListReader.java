package io.ipoli.android.app.services.readers;

import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class RealmQuestListReader implements ListReader<Quest> {

    private final QuestPersistenceService questPersistenceService;

    public RealmQuestListReader(QuestPersistenceService questPersistenceService) {
        this.questPersistenceService = questPersistenceService;
    }

    @Override
    public Observable<Quest> read() {
        return questPersistenceService.findAllWhoNeedSyncWithRemote().concatMapIterable(quests -> quests);
    }
}
