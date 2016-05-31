package io.ipoli.android.quest.persistence;

import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.reward.data.Reward;
import rx.Observable;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/30/16.
 */
public interface RewardPersistenceService extends PersistenceService<Reward> {

    Observable<List<Reward>> findAll();

}
