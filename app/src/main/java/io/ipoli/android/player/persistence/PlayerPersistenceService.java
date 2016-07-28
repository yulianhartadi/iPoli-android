package io.ipoli.android.player.persistence;

import io.ipoli.android.player.Player;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface PlayerPersistenceService {

    void save(Player player);

    void find(OnDataChangedListener<Player> listener);

    void listenForChanges(OnDataChangedListener<Player> listener);

    void removeAllListeners();
}