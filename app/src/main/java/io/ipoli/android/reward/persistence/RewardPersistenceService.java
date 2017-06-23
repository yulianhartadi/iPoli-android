package io.ipoli.android.reward.persistence;

import java.util.List;

import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.reward.data.Reward;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public interface RewardPersistenceService extends PersistenceService<Reward> {

    void listenForAll(OnDataChangedListener<List<Reward>> listener);
}
