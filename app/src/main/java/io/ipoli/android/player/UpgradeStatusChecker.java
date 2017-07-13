package io.ipoli.android.player;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.player.data.MembershipType;
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

    private final Player player;
    private final LocalDate currentDate;
    private final List<PurchaseState> purchases;

    public static class PurchaseState {
        public final long expiration;
        public boolean isInGrace;

        public PurchaseState(long expiration, boolean isInGrace) {
            this.expiration = expiration;
            this.isInGrace = isInGrace;
        }
    }

    public UpgradeStatusChecker(Player player) {
        this(player, LocalDate.now());
    }

    public UpgradeStatusChecker(Player player, LocalDate currentDate) {
        this(player, currentDate, new ArrayList<>());
    }

    public UpgradeStatusChecker(Player player, LocalDate currentDate, List<PurchaseState> purchases) {
        this.player = player;
        this.currentDate = currentDate;
        this.purchases = purchases;
    }

    public UpgradeStatus checkStatus() {
        List<Upgrade> inGracePeriod = new ArrayList<>();
        List<Upgrade> expired = new ArrayList<>();
        Map<Upgrade, Long> toBeRenewed = new HashMap<>();
        UpgradeStatus.StatusType statusType = UpgradeStatus.StatusType.TRIAL;

        LocalDate trialGraceStart = player.getCreatedAtDate().plusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS);
        LocalDate trialGraceEndDate = trialGraceStart.plusDays(Constants.UPGRADE_GRACE_PERIOD_DAYS - 1);

        if (currentDate.isBefore(trialGraceStart)) {
            return new UpgradeStatus(inGracePeriod, expired, toBeRenewed, statusType);
        }

        if (DateUtils.isBetween(currentDate, trialGraceStart, trialGraceEndDate)) {
            statusType = UpgradeStatus.StatusType.TRIAL_GRACE;
            inGracePeriod.addAll(Arrays.asList(Upgrade.values()));
            return new UpgradeStatus(inGracePeriod, expired, toBeRenewed, statusType);
        }

        if (player.getMembership() == MembershipType.NONE) {
            statusType = UpgradeStatus.StatusType.NOT_MEMBER;
        } else {
            statusType = UpgradeStatus.StatusType.MEMBER;
        }

        Map<Integer, Long> upgrades = player.getInventory().getUpgrades();
        for (Map.Entry<Integer, Long> entry : upgrades.entrySet()) {
            LocalDate expirationDate = DateUtils.fromMillis(entry.getValue());
            if (expirationDate.isBefore(currentDate)) {
                expired.add(Upgrade.get(entry.getKey()));
            }
            LocalDate upgradeGracePeriodStart = expirationDate.minusDays(Constants.UPGRADE_GRACE_PERIOD_DAYS - 1);
            if (DateUtils.isBetween(currentDate, upgradeGracePeriodStart, expirationDate)) {
                inGracePeriod.add(Upgrade.get(entry.getKey()));
            }
        }

        //      renewed =  purchase.state == Purchase.State.PURCHASED && purchase.autoRenewing

        if (expired.size() == Upgrade.values().length) {
            statusType = UpgradeStatus.StatusType.NOT_MEMBER;
        }

        return new UpgradeStatus(inGracePeriod, expired, toBeRenewed, statusType);
    }
}
