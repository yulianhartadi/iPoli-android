package io.ipoli.android.player;

import java.util.List;
import java.util.Map;

import io.ipoli.android.store.Upgrade;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/13/17.
 */
public class ValidationStatus {
    public enum StatusType {
        TRIAL, MEMBER, NOT_MEMBER
    }

    public final List<Upgrade> expired;
    public final List<Upgrade> expiring;
    public final Map<Upgrade, Long> toBeRenewed;
    public final StatusType type;

    public ValidationStatus(List<Upgrade> expired, List<Upgrade> expiring, Map<Upgrade, Long> toBeRenewed, StatusType type) {
        this.expired = expired;
        this.expiring = expiring;
        this.toBeRenewed = toBeRenewed;
        this.type = type;
    }
}
