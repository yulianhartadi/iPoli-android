package io.ipoli.android.player.persistence;

import io.ipoli.android.player.Player;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface PlayerPersistenceService {

    void save(Player player);

    void find(OnDatabaseChangedListener<Player> listener);

    void listenForChanges(OnDatabaseChangedListener<Player> listener);

    void removeAllListeners();
}