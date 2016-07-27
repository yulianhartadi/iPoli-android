package io.ipoli.android.player.persistence;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface PlayerPersistenceService extends PersistenceService<Player> {
    void find(OnDatabaseChangedListener<Player> listener);
}