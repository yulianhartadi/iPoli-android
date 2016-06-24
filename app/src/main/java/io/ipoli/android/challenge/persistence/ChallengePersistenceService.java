package io.ipoli.android.challenge.persistence;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/24/16.
 */
public interface ChallengePersistenceService extends PersistenceService<Challenge> {

    void findAllNotCompleted(OnDatabaseChangedListener<Challenge> listener);
}
