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

    public boolean hasEnoughCoinsForUpgrade(Upgrade upgrade) {
        return playerPersistenceService.get().getCoins() >= upgrade.getPrice();
    }

    public void buy(Upgrade upgrade) {
        Player player = playerPersistenceService.get();
//        player.removeCoins(upgrade.getPrice());
//        player.getUpgrades().add(upgrade.getCode());
        playerPersistenceService.save(player);
    }
}
