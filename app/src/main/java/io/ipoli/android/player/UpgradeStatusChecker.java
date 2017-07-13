package io.ipoli.android.player;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.store.Upgrade;

/**
 * About to expire
 * Expired
 * Should be renewed
 * <p>
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/13/17.
 */
public class UpgradeStatusChecker {

    public static class UpgradeStatus {

        public List<Upgrade> aboutToExpire;

        public List<Upgrade> expired;
        public Map<Upgrade, Long> toBeRenewed;

        public UpgradeStatus(List<Upgrade> aboutToExpire, List<Upgrade> expired, Map<Upgrade, Long> toBeRenewed) {
            this.aboutToExpire = aboutToExpire;
            this.expired = expired;
            this.toBeRenewed = toBeRenewed;
        }
    }

    private final Player player;
    private final LocalDate currentDate;

    public UpgradeStatusChecker(Player player) {
        this(player, LocalDate.now());
    }

    public UpgradeStatusChecker(Player player, LocalDate currentDate) {
        this.player = player;
        this.currentDate = currentDate;
    }

    public UpgradeStatus checkStatus() {
        List<Upgrade> aboutToExpire = new ArrayList<>();
        List<Upgrade> expired = new ArrayList<>();
        Map<Upgrade, Long> toBeRenewed = new HashMap<>();

        Map<Integer, Long> upgrades = player.getInventory().getUpgrades();
        for (Map.Entry<Integer, Long> entry : upgrades.entrySet()) {
            LocalDate expirationDate = DateUtils.fromMillis(entry.getValue());
            if (expirationDate.isBefore(currentDate)) {
                expired.add(Upgrade.get(entry.getKey()));
            }
        }

        return new UpgradeStatus(aboutToExpire, expired, toBeRenewed);
    }
}
