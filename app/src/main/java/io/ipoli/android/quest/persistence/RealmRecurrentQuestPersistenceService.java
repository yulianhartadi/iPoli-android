package io.ipoli.android.quest.persistence;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.quest.data.RecurrentQuest;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public class RealmRecurrentQuestPersistenceService extends BaseRealmPersistenceService<RecurrentQuest> implements RecurrentQuestPersistenceService {

    @Override
    protected Class<RecurrentQuest> getRealmObjectClass() {
        return RecurrentQuest.class;
    }

    @Override
    public Observable<List<RecurrentQuest>> findAll() {
        return fromRealm(where().findAll());
    }
}
