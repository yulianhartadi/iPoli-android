package io.ipoli.android.player;

import org.threeten.bp.LocalDate;

import java.util.Map;
import java.util.Set;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.data.Inventory;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.Upgrade;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class UpgradeManager implements OnDataChangedListener<Player> {
    private static final int DEFAULT_EXPIRATION_MONTHS = 1;

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
        Map<Integer, Long> upgrades = player.getInventory().getUpgrades();
        if(!upgrades.containsKey(upgrade.code)) {
            return false;
        }

        LocalDate expirationDate = DateUtils.fromMillis(upgrades.get(upgrade.code));
        return !expirationDate.isBefore(LocalDate.now(DateUtils.ZONE_UTC));
    }

    public boolean hasEnoughCoinsForUpgrade(Upgrade upgrade) {
        return playerPersistenceService.get().getCoins() >= upgrade.price;
    }

    public void unlock(Upgrade upgrade) {
        unlock(upgrade, LocalDate.now().plusMonths(DEFAULT_EXPIRATION_MONTHS).plusDays(Constants.UPGRADE_EXPIRATION_GRACE_DAYS));
    }

    public void unlock(Upgrade upgrade, LocalDate expirationDate) {
        Player player = getPlayer();
        player.removeCoins(upgrade.price);
        if (player.getInventory() == null) {
            player.setInventory(new Inventory());
        }
        player.getInventory().addUpgrade(upgrade, expirationDate);
        playerPersistenceService.save(player);
    }

    public Long getExpirationDate(Upgrade upgrade) {
        Player player = getPlayer();
        if (player.getInventory() == null) {
            return null;
        }
        if (!player.getInventory().getUpgrades().containsKey(upgrade.code)) {
            return null;
        }
        return player.getInventory().getUpgrades().get(upgrade.code);
    }

    public Set<Integer> getUnlockedCodes() {
        Player player = getPlayer();
        return player.getInventory().getUpgrades().keySet();
    }

    @Override
    public void onDataChanged(Player player) {
        this.player = player;
    }
}
