package io.ipoli.android.player.persistence;

import io.ipoli.android.app.persistence.BaseRealmPersistenceService;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.Player;
import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class RealmPlayerPersistenceService extends BaseRealmPersistenceService<Player> implements PlayerPersistenceService {

    public RealmPlayerPersistenceService(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<Player> find() {
        return find(RealmQuery::findFirstAsync);
    }

    @Override
    public Player findSync() {
        return getRealm().copyFromRealm(getRealm().where(getRealmObjectClass()).findFirst());
    }

    @Override
    public Observable<Player> addAuthProvider(Player player, AuthProvider authProvider) {
        return Observable.create(subscriber -> {
            getRealm().executeTransactionAsync(backgroundRealm ->
                            player.addAuthProvider(authProvider),
                    () -> {
                        subscriber.onNext(player);
                        subscriber.onCompleted();
                    }, subscriber::onError);
        });
    }

    @Override
    protected Class<Player> getRealmObjectClass() {
        return Player.class;
    }
}