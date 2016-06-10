package io.ipoli.android.quest.persistence;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.reward.data.Reward;
import io.realm.Realm;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/16.
 */
public class RealmRewardPersistenceService extends BaseRealmPersistenceService<Reward> implements RewardPersistenceService {

    public RealmRewardPersistenceService(Realm realm) {
        super(realm);
    }

    @Override
    protected Class<Reward> getRealmObjectClass() {
        return Reward.class;
    }

    @Override
    public void findAll(OnDatabaseChangedListener<Reward> listener) {
        listenForResults(where()
                .findAllAsync(), listener);
    }
}