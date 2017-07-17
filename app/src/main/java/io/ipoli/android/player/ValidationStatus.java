package io.ipoli.android.player;

import java.util.List;
import java.util.Map;

import io.ipoli.android.store.PowerUp;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/13/17.
 */
public class ValidationStatus {
    public enum StatusType {
        TRIAL, MEMBER, NOT_MEMBER
    }

    public final List<PowerUp> expired;
    public final List<PowerUp> expiring;
    public final Map<PowerUp, Long> toBeRenewed;
    public final StatusType type;

    public ValidationStatus(List<PowerUp> expired, List<PowerUp> expiring, Map<PowerUp, Long> toBeRenewed, StatusType type) {
        this.expired = expired;
        this.expiring = expiring;
        this.toBeRenewed = toBeRenewed;
        this.type = type;
    }
}
