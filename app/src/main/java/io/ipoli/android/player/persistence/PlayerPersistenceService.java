package io.ipoli.android.player.persistence;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.Player;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface PlayerPersistenceService extends PersistenceService<Player> {
    Observable<Player> find();

    Observable<Player> addAuthProvider(Player player, AuthProvider authProvider);
}
