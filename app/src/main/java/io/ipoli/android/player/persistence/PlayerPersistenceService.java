package io.ipoli.android.player.persistence;

import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.player.data.Player;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface PlayerPersistenceService extends PersistenceService<Player> {

    Player get();

    void listen(OnDataChangedListener<Player> listener);

    void deleteAllPlayerData();

    void save(Player player, String playerId);
}