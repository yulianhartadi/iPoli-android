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

    public static class UpgradeStatus {
        public enum StatusType {
            TRIAL, TRIAL_GRACE, MEMBER, MEMBER_GRACE, NOT_MEMBER
        }

        public final List<Upgrade> inGracePeriod;
        public final List<Upgrade> expired;
        public final Map<Upgrade, Long> toBeRenewed;
        public final StatusType type;

        public UpgradeStatus(List<Upgrade> inGracePeriod, List<Upgrade> expired, Map<Upgrade, Long> toBeRenewed, StatusType type) {
            this.inGracePeriod = inGracePeriod;
            this.expired = expired;
            this.toBeRenewed = toBeRenewed;
            this.type = type;
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
        List<Upgrade> inGracePeriod = new ArrayList<>();
        List<Upgrade> expired = new ArrayList<>();
        Map<Upgrade, Long> toBeRenewed = new HashMap<>();
        UpgradeStatus.StatusType statusType = UpgradeStatus.StatusType.TRIAL;

        LocalDate trialGraceStart = player.getCreatedAtDate().plusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS);
        LocalDate trialGraceEndDate = trialGraceStart.plusDays(Constants.UPGRADE_TRIAL_GRACE_PERIOD_DAYS - 1);

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
        }

        Map<Integer, Long> upgrades = player.getInventory().getUpgrades();
        for (Map.Entry<Integer, Long> entry : upgrades.entrySet()) {
            LocalDate expirationDate = DateUtils.fromMillis(entry.getValue());
            if (expirationDate.isBefore(currentDate)) {
                expired.add(Upgrade.get(entry.getKey()));
            }
            LocalDate upgradeGracePeriodStart = expirationDate.minusDays(Constants.UPGRADE_EXPIRATION_GRACE_PERIOD_DAYS - 1);
            if (DateUtils.isBetween(currentDate, upgradeGracePeriodStart, expirationDate)) {
                inGracePeriod.add(Upgrade.get(entry.getKey()));
            }
        }

        if (expired.size() == Upgrade.values().length) {
            statusType = UpgradeStatus.StatusType.NOT_MEMBER;
        }

        return new UpgradeStatus(inGracePeriod, expired, toBeRenewed, statusType);
    }
}
