package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.quest.data.Category;

import static io.ipoli.android.app.utils.Time.h2Min;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */
public class LearningConstraint extends BaseConstraint {

    private final int sleepEndMinute;

    public LearningConstraint(int sleepEndMinute, int slotDuration) {
        super(slotDuration);
        this.sleepEndMinute = sleepEndMinute;
    }

    @Override
    public boolean shouldApply(Task task) {
        return task.getCategory() == Category.LEARNING;
    }

    @Override
    public DiscreteDistribution apply() {
        int slopeWidth = getSlotCountBetween(0, 30);
        DiscreteDistribution d1 = createStartOfDayPeak(slopeWidth);
        DiscreteDistribution d2 = createEndOfDayPeak(slopeWidth);
        DiscreteDistribution d = d1.joint(d2);
        return d.set(getSlotForMinute(sleepEndMinute), 0.0);
    }

    private DiscreteDistribution createStartOfDayPeak(int slopeWidth) {
        int startMinute = sleepEndMinute + h2Min(2);
        int peakWidth = getSlotCountBetween(startMinute, startMinute + h2Min(2));
        int peakStart = getSlotForMinute(startMinute);
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, getTotalSlotCount());
    }

    private DiscreteDistribution createEndOfDayPeak(int slopeWidth) {
        int startMinute = sleepEndMinute + h2Min(10);
        int peakWidth = getSlotCountBetween(startMinute, startMinute + h2Min(2));
        int peakStart = getSlotForMinute(startMinute);
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, getTotalSlotCount());
    }
}
