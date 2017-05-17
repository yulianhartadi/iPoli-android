package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.quest.data.Category;

import static io.ipoli.android.app.utils.Time.h2Min;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/24/17.
 */
public class WellnessConstraint extends BaseConstraint {

    private final int sleepEndMinute;

    public WellnessConstraint(int sleepEndMinute, int slotDuration) {
        super(slotDuration);
        this.sleepEndMinute = sleepEndMinute;
    }

    @Override
    public boolean shouldApply(Task task) {
        return task.getCategory() == Category.WELLNESS;
    }

    @Override
    public DiscreteDistribution apply() {
        int slopeWidth = getSlotCountBetween(0, 30);
        DiscreteDistribution d1 = createMorningPeak(slopeWidth);
        DiscreteDistribution d2 = createAfternoonPeak(slopeWidth);
        return d1.joint(d2);
    }

    private DiscreteDistribution createMorningPeak(int slopeWidth) {
        int startMinute = sleepEndMinute + 30;
        int peakWidth = getSlotCountBetween(startMinute, startMinute + h2Min(3));
        int peakStart = getSlotForMinute(startMinute);
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, getTotalSlotCount());
    }

    private DiscreteDistribution createAfternoonPeak(int slopeWidth) {
        int startMinute = h2Min(17);
        int peakWidth = getSlotCountBetween(startMinute, h2Min(20));
        int peakStart = getSlotForMinute(startMinute);
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, getTotalSlotCount());
    }
}
