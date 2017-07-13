package io.ipoli.android.player;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
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
public class UpgradeStatusValidator {

    private final Player player;
    private final LocalDate currentDate;
    private final LocalDate membershipExpirationDate;
    private final boolean isAutoRenew;

    public UpgradeStatusValidator(Player player) {
        this(player, LocalDate.now());
    }

    public UpgradeStatusValidator(Player player, LocalDate currentDate) {
        this(player, currentDate, null);
    }

    public UpgradeStatusValidator(Player player, LocalDate currentDate, LocalDate membershipExpirationDate) {
        this(player, currentDate, membershipExpirationDate, true);
    }

    public UpgradeStatusValidator(Player player, LocalDate currentDate, boolean isAutoRenew) {
        this(player, currentDate, null, isAutoRenew);
    }

    public UpgradeStatusValidator(Player player, LocalDate currentDate, LocalDate membershipExpirationDate, boolean isAutoRenew) {
        this.player = player;
        this.currentDate = currentDate;
        this.membershipExpirationDate = membershipExpirationDate;
        this.isAutoRenew = isAutoRenew;
    }


    public ValidationStatus validate() {
        List<Upgrade> expired = new ArrayList<>();
        List<Upgrade> expiring = new ArrayList<>();
        Map<Upgrade, Long> toBeRenewed = new HashMap<>();
        ValidationStatus.StatusType statusType = ValidationStatus.StatusType.TRIAL;

        LocalDate trialEnd = player.getCreatedAtDate().plusDays(Constants.UPGRADE_TRIAL_PERIOD_DAYS - 1);

        if (currentDate.isBefore(trialEnd)) {
            return new ValidationStatus(expired, expiring, toBeRenewed, statusType);
        }

        if (player.getMembership() == MembershipType.NONE) {
            statusType = ValidationStatus.StatusType.NOT_MEMBER;
        } else {
            statusType = ValidationStatus.StatusType.MEMBER;
        }

        Map<Integer, Long> upgrades = player.getInventory().getUpgrades();
        if (membershipExpirationDate != null) {
            long membershipExpirationMillis = DateUtils.toMillis(membershipExpirationDate);
            for (Map.Entry<Integer, Long> entry : upgrades.entrySet()) {
                toBeRenewed.put(Upgrade.get(entry.getKey()), membershipExpirationMillis);
            }
            return new ValidationStatus(expired, expiring, toBeRenewed, statusType);
        }

        for (Map.Entry<Integer, Long> entry : upgrades.entrySet()) {
            LocalDate expirationDate = DateUtils.fromMillis(entry.getValue());
            if (expirationDate.isBefore(currentDate)) {
                expired.add(Upgrade.get(entry.getKey()));
            }
            if (currentDate.isEqual(expirationDate) && !isAutoRenew) {
                expiring.add(Upgrade.get(entry.getKey()));
            }
        }

        if (expired.size() == Upgrade.values().length) {
            statusType = ValidationStatus.StatusType.NOT_MEMBER;
        }

        return new ValidationStatus(expired, expiring, toBeRenewed, statusType);
    }
}
