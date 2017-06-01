package io.ipoli.android.player;

import org.threeten.bp.LocalDate;

import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.Upgrade;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class UpgradeManager implements OnDataChangedListener<Player> {

    private Player player;

    private final PlayerPersistenceService playerPersistenceService;

    public UpgradeManager(PlayerPersistenceService playerPersistenceService) {
        this.playerPersistenceService = playerPersistenceService;
        if (playerPersistenceService.get() != null) {
            playerPersistenceService.listen(this);
        }
    }

    private Player getPlayer() {
        if (player == null) {
            player = playerPersistenceService.get();
            if (player != null) {
                playerPersistenceService.listen(this);
            }
        }
        return player;
    }

    public boolean isLocked(Upgrade upgrade) {
        return !isUnlocked(upgrade);
    }

    public boolean isUnlocked(Upgrade upgrade) {
        Player player = getPlayer();
        if (player.getInventory() == null) {
            return false;
        }
        return player.getInventory().getUpgrades().containsKey(upgrade.code);
    }

    public boolean hasEnoughCoinsForUpgrade(Upgrade upgrade) {
        return playerPersistenceService.get().getCoins() >= upgrade.price;
    }

    public void unlock(Upgrade upgrade) {
        Player player = getPlayer();
        player.removeCoins(upgrade.price);
        if (player.getInventory() == null) {
            player.setInventory(new Inventory());
        }
        player.getInventory().addUpgrade(upgrade, LocalDate.now());
        playerPersistenceService.save(player);
    }

    public Long getUnlockDate(Upgrade upgrade) {
        Player player = getPlayer();
        if (player.getInventory() == null) {
            return null;
        }
        if (!player.getInventory().getUpgrades().containsKey(upgrade.code)) {
            return null;
        }
        return player.getInventory().getUpgrades().get(upgrade.code);
    }

    @Override
    public void onDataChanged(Player player) {
        this.player = player;
    }
}
