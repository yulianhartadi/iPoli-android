package io.ipoli.android.player;

import org.threeten.bp.LocalDate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.data.Inventory;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.store.PowerUp;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class PowerUpManager implements OnDataChangedListener<Player> {
    private static final int DEFAULT_EXPIRATION_MONTHS = 1;

    private Player player;

    private final PlayerPersistenceService playerPersistenceService;

    public PowerUpManager(PlayerPersistenceService playerPersistenceService) {
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

    public boolean isDisabled(PowerUp powerUp) {
        return !isEnabled(powerUp);
    }

    public boolean isEnabled(PowerUp powerUp) {
        Player player = getPlayer();
        if (player.getInventory() == null) {
            return false;
        }
        Map<Integer, Long> powerUps = player.getInventory().getPowerUps();
        if (!powerUps.containsKey(powerUp.code)) {
            return false;
        }

        LocalDate expirationDate = DateUtils.fromMillis(powerUps.get(powerUp.code));
        return isEnabledToday(expirationDate);
    }

    private boolean isEnabledToday(LocalDate expirationDate) {
        return !expirationDate.isBefore(LocalDate.now(DateUtils.ZONE_UTC));
    }

    public boolean hasEnoughCoinsForPowerUp(PowerUp powerUp) {
        return playerPersistenceService.get().getCoins() >= powerUp.price;
    }

    public void enable(PowerUp powerUp) {
        enable(powerUp, LocalDate.now().plusMonths(DEFAULT_EXPIRATION_MONTHS).minusDays(1));
    }

    public void enable(PowerUp powerUp, LocalDate expirationDate) {
        Player player = getPlayer();
        player.removeCoins(powerUp.price);
        if (player.getInventory() == null) {
            player.setInventory(new Inventory());
        }
        player.getInventory().addPowerUp(powerUp, expirationDate);
        playerPersistenceService.save(player);
    }

    public Long getExpirationDate(PowerUp powerUp) {
        Player player = getPlayer();
        if (player.getInventory() == null) {
            return null;
        }
        if (!player.getInventory().getPowerUps().containsKey(powerUp.code)) {
            return null;
        }
        return player.getInventory().getPowerUps().get(powerUp.code);
    }

    public Set<Integer> getEnabledCodes() {
        Player player = getPlayer();
        Set<Integer> enabledCodes = new HashSet<>();
        for (Map.Entry<PowerUp, LocalDate> entry : player.getPowerUps().entrySet()) {
            if (isEnabledToday(entry.getValue())) {
                enabledCodes.add(entry.getKey().code);
            }
        }
        return enabledCodes;
    }

    @Override
    public void onDataChanged(Player player) {
        this.player = player;
    }
}
