package io.ipoli.android.challenge.persistence;

import com.squareup.otto.Bus;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.realm.Realm;

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
}
