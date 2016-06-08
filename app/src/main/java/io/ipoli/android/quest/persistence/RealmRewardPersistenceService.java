package io.ipoli.android.quest.persistence;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.reward.data.Reward;
import io.realm.RealmQuery;
import rx.Observable;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/16.
 */
public class RealmRewardPersistenceService extends BaseRealmPersistenceService<Reward> implements RewardPersistenceService {
    @Override
    protected Class<Reward> getRealmObjectClass() {
        return Reward.class;
    }

    @Override
    public Observable<List<Reward>> findAll() {
        return findAll(RealmQuery::findAllAsync);
    }
}
