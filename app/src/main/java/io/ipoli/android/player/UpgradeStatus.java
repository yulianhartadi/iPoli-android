package io.ipoli.android.player;

import java.util.List;
import java.util.Map;

import io.ipoli.android.store.Upgrade;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/13/17.
 */
public class UpgradeStatus {
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
