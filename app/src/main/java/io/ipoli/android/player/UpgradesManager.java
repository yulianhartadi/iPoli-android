package io.ipoli.android.player;

import org.threeten.bp.LocalDate;

import io.ipoli.android.app.utils.DateUtils;
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
        return playerPersistenceService.get().getUpgrades().containsKey(upgrade.getCode());
    }

    public boolean hasEnoughCoinsForUpgrade(Upgrade upgrade) {
        return playerPersistenceService.get().getCoins() >= upgrade.getPrice();
    }

    public void buy(Upgrade upgrade) {
        Player player = playerPersistenceService.get();
        player.removeCoins(upgrade.getPrice());
        player.getUpgrades().put(upgrade.getCode(), DateUtils.toMillis(LocalDate.now()));
        playerPersistenceService.save(player);
    }
}
