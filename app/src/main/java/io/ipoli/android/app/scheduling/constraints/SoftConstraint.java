package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/26/17.
 */
public abstract class SoftConstraint extends BaseConstraint {

    private final int startMinute;
    private final int endMinute;

    public SoftConstraint(int startMinute, int endMinute, int slotDuration) {
        super(slotDuration);
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    @Override
    public DiscreteDistribution apply() {
        return FlatPeakDiscreteDistribution.create(getSlotForMinute(startMinute), getSlotForMinute(endMinute - startMinute), 1000, getSlotCountBetween(0, 30), getTotalSlotCount());
    }
}
