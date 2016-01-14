package io.ipoli.android.player.persistence;

import android.content.Context;

import io.ipoli.android.Constants;
import io.ipoli.android.player.Player;
import io.realm.Realm;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class RealmPlayerPersistenceService implements PlayerPersistenceService {

    private Realm realm;

    public RealmPlayerPersistenceService(Context context) {
        realm = Realm.getInstance(context);
    }

    @Override
    public Player save(Player player) {
        realm.beginTransaction();
        Player realmPlayer = realm.copyToRealmOrUpdate(player);
        realm.commitTransaction();
        return realmPlayer;
    }

    @Override
    public Player find() {
        Player player = realm.where(Player.class).findFirst();
        if (player == null) {
            return new Player(Constants.DEFAULT_PLAYER_EXPERIENCE, Constants.DEFAULT_PLAYER_LEVEL, "avatar_02");
        }
        return realm.copyFromRealm(player);
    }
}
