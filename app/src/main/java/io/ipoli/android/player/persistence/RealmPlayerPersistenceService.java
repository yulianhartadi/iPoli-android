package io.ipoli.android.player.persistence;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.player.Player;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class RealmPlayerPersistenceService extends BaseRealmPersistenceService<Player> implements PlayerPersistenceService {

    @Override
    public Observable<Player> find() {
        Player p = where(Player.class).findFirst();
        if (p == null) {
            return Observable.just(null);
        }
        return Observable.just(p);
    }
}