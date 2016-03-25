package io.ipoli.android.player.persistence;

import io.ipoli.android.player.Player;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface PlayerPersistenceService {
    Observable<Player> save(Player player);
    Observable<Player> find();
}
