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
import io.ipoli.android.store.PowerUp;

/**
 * About to expire
 * Expired
 * Should be renewed
 * <p>
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/13/17.
 */
public class PowerUpStatusValidator {

    private final Player player;
    private final LocalDate currentDate;
    private final LocalDate membershipExpirationDate;
    private final boolean isAutoRenew;

    public PowerUpStatusValidator(Player player) {
        this(player, LocalDate.now());
    }

    public PowerUpStatusValidator(Player player, LocalDate currentDate) {
        this(player, currentDate, null);
    }

    public PowerUpStatusValidator(Player player, LocalDate currentDate, LocalDate membershipExpirationDate) {
        this(player, currentDate, membershipExpirationDate, true);
    }

    public PowerUpStatusValidator(Player player, LocalDate currentDate, boolean isAutoRenew) {
        this(player, currentDate, null, isAutoRenew);
    }

    public PowerUpStatusValidator(Player player, boolean isAutoRenew) {
        this(player, LocalDate.now(), isAutoRenew);
    }

    public PowerUpStatusValidator(Player player, LocalDate currentDate, LocalDate membershipExpirationDate, boolean isAutoRenew) {
        this.player = player;
        this.currentDate = currentDate;
        this.membershipExpirationDate = membershipExpirationDate;
        this.isAutoRenew = isAutoRenew;
    }

    public ValidationStatus validate() {
        List<PowerUp> expired = new ArrayList<>();
        List<PowerUp> expiring = new ArrayList<>();
        Map<PowerUp, Long> toBeRenewed = new HashMap<>();
        ValidationStatus.StatusType statusType = ValidationStatus.StatusType.TRIAL;

        LocalDate trialEnd = player.getCreatedAtDate().plusDays(Constants.POWER_UPS_TRIAL_PERIOD_DAYS - 1);

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
                toBeRenewed.put(PowerUp.get(entry.getKey()), membershipExpirationMillis);
            }
            return new ValidationStatus(expired, expiring, toBeRenewed, statusType);
        }

        for (Map.Entry<Integer, Long> entry : upgrades.entrySet()) {
            LocalDate expirationDate = DateUtils.fromMillis(entry.getValue());
            if (expirationDate.isBefore(currentDate)) {
                expired.add(PowerUp.get(entry.getKey()));
            }
            if (currentDate.isEqual(expirationDate) && !isAutoRenew) {
                expiring.add(PowerUp.get(entry.getKey()));
            }
        }

        if (expired.size() == PowerUp.values().length) {
            statusType = ValidationStatus.StatusType.NOT_MEMBER;
        }

        return new ValidationStatus(expired, expiring, toBeRenewed, statusType);
    }
}
