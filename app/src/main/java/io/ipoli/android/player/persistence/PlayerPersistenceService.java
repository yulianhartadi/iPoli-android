package io.ipoli.android.player.persistence;

import io.ipoli.android.player.Player;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public interface PlayerPersistenceService {
    Player save(Player player);
    Player find();
}
