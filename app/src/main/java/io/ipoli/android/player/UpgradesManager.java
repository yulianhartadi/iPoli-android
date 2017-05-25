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
        return playerPersistenceService.get().getUpgrades().containsKey(upgrade.code);
    }

    public boolean hasEnoughCoinsForUpgrade(Upgrade upgrade) {
        return playerPersistenceService.get().getCoins() >= upgrade.price;
    }

    public void buy(Upgrade upgrade) {
        Player player = playerPersistenceService.get();
        player.removeCoins(upgrade.price);
        player.getUpgrades().put(upgrade.code, DateUtils.toMillis(LocalDate.now()));
        playerPersistenceService.save(player);
    }

    public Long getBoughtDate(Upgrade upgrade) {
        Player player = playerPersistenceService.get();
        if(!player.getUpgrades().containsKey(upgrade.code)) {
            return null;
        }
        return player.getUpgrades().get(upgrade.code);
    }
}
