package io.ipoli.android.player;

import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class UpgradesManager {

    private final PlayerPersistenceService playerPersistenceService;

    public UpgradesManager(PlayerPersistenceService playerPersistenceService) {
        this.playerPersistenceService = playerPersistenceService;
    }

    public boolean has(Upgrade upgrade) {
        return playerPersistenceService.get().getUpgrades().contains(upgrade.getCode());
    }
}
