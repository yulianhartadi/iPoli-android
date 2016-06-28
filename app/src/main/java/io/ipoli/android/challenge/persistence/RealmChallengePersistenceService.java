package io.ipoli.android.challenge.persistence;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/24/16.
 */
public class RealmChallengePersistenceService extends BaseRealmPersistenceService<Challenge> implements ChallengePersistenceService {

    private final Bus eventBus;

    public RealmChallengePersistenceService(Bus eventBus, Realm realm) {
        super(realm);
        this.eventBus = eventBus;
    }

    @Override
    protected Class<Challenge> getRealmObjectClass() {
        return Challenge.class;
    }

    @Override
    public void findAllNotCompleted(OnDatabaseChangedListener<Challenge> listener) {
        listenForChanges(where()
                .isNull("completedAt")
                .findAllSortedAsync("endDate", Sort.ASCENDING), listener);
    }

    @Override
    public List<Challenge> findAllNotCompleted() {
        return findAll(where -> where
                .isNull("completedAt")
                .findAllSorted("endDate", Sort.ASCENDING));
    }
}
