package io.ipoli.android.app.scheduling.constraints;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.UniformDiscreteDistribution;
import io.ipoli.android.quest.data.Category;

import static io.ipoli.android.app.scheduling.constraints.AfternoonConstraint.AFTERNOON_END;
import static io.ipoli.android.app.scheduling.constraints.AfternoonConstraint.AFTERNOON_START;
import static io.ipoli.android.app.scheduling.constraints.EveningConstraint.EVENING_END;
import static io.ipoli.android.app.scheduling.constraints.EveningConstraint.EVENING_START;
import static io.ipoli.android.app.scheduling.constraints.MorningConstraint.MORNING_END;
import static io.ipoli.android.app.scheduling.constraints.MorningConstraint.MORNING_START;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class ProductiveTimeConstraint extends BaseConstraint {

    private final Set<TimeOfDay> productiveTimes;

    public ProductiveTimeConstraint(Collection<TimeOfDay> productiveTimes, int slotDuration) {
        super(slotDuration);
        this.productiveTimes = new HashSet<>(productiveTimes);
    }

    @Override
    public boolean shouldApply(Task task) {
        Category category = task.getCategory();
        return category == Category.WORK || category == Category.LEARNING;
    }

    @Override
    public DiscreteDistribution apply() {
        DiscreteDistribution result = UniformDiscreteDistribution.create(getTotalSlotCount());
        if (productiveTimes.contains(TimeOfDay.ANY_TIME)) {
            return result;
        }

        int slopeWidth = getSlotCountBetween(0, 30);

        if (productiveTimes.contains(TimeOfDay.MORNING)) {
            result = result.joint(createMorningDistribution(slopeWidth));
        }

        if (productiveTimes.contains(TimeOfDay.AFTERNOON)) {
            result = result.joint(createAfternoonDistribution(slopeWidth));
        }

        if (productiveTimes.contains(TimeOfDay.EVENING)) {
            result = result.joint(createEveningDistribution(slopeWidth));
        }

        return result;
    }

    private DiscreteDistribution createMorningDistribution(int slopeWidth) {
        return createDistribution(slopeWidth, MORNING_START, MORNING_END);
    }

    private DiscreteDistribution createAfternoonDistribution(int slopeWidth) {
        return createDistribution(slopeWidth, AFTERNOON_START, AFTERNOON_END);
    }

    private DiscreteDistribution createEveningDistribution(int slopeWidth) {
        return createDistribution(slopeWidth, EVENING_START, EVENING_END);
    }

    private DiscreteDistribution createDistribution(int slopeWidth, int startMinute, int endMinute) {
        int peakWidth = getSlotCountBetween(startMinute, endMinute);
        int peakStart = getSlotForMinute(startMinute);
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, getTotalSlotCount());
    }
}