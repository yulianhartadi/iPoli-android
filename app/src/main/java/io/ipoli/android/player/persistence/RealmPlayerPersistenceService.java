package io.ipoli.android.player.persistence;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.player.Player;
import io.realm.RealmQuery;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class RealmPlayerPersistenceService extends BaseRealmPersistenceService<Player> implements PlayerPersistenceService {

    @Override
    public Observable<Player> find() {
        return find(RealmQuery::findFirstAsync);
    }

    @Override
    protected Class<Player> getRealmObjectClass() {
        return Player.class;
    }
}