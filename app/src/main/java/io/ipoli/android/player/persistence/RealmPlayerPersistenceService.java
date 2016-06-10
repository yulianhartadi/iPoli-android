package io.ipoli.android.player.persistence;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.Player;
import io.realm.Realm;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class RealmPlayerPersistenceService extends BaseRealmPersistenceService<Player> implements PlayerPersistenceService {

    public RealmPlayerPersistenceService(Realm realm) {
        super(realm);
    }

    @Override
    public Player find() {
        Player res = where().findFirst();
        if (res == null) {
            return null;
        }
        return getRealm().copyFromRealm(res);
    }

    @Override
    public void addAuthProvider(Player player, AuthProvider authProvider) {
        getRealm().executeTransaction(backgroundRealm ->
                player.addAuthProvider(authProvider));
    }

    @Override
    protected Class<Player> getRealmObjectClass() {
        return Player.class;
    }
}