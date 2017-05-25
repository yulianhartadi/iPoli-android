package io.ipoli.android.player;

import org.threeten.bp.LocalDate;

import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.Upgrade;

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
        Player player = playerPersistenceService.get();
        if(player.getInventory() == null) {
            return false;
        }
        return player.getInventory().getUpgrades().containsKey(upgrade.code);
    }

    public boolean hasEnoughCoinsForUpgrade(Upgrade upgrade) {
        return playerPersistenceService.get().getCoins() >= upgrade.price;
    }

    public void buy(Upgrade upgrade) {
        Player player = playerPersistenceService.get();
        player.removeCoins(upgrade.price);
        if(player.getInventory() == null) {
            player.setInventory(new Inventory());
        }
        player.getInventory().addUpgrade(upgrade, LocalDate.now());
        playerPersistenceService.save(player);
    }

    public Long getBoughtDate(Upgrade upgrade) {
        Player player = playerPersistenceService.get();
        if(player.getInventory() == null) {
            return null;
        }
        if(!player.getInventory().getUpgrades().containsKey(upgrade.code)) {
            return null;
        }
        return player.getInventory().getUpgrades().get(upgrade.code);
    }
}
