package io.ipoli.android.app.scheduling.constraints;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.scheduling.DailySchedule;
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
public class ProductiveTimeConstraint implements Constraint {

    private final Set<TimeOfDay> productiveTimes;
    private final Random random;

    public ProductiveTimeConstraint(Collection<TimeOfDay> productiveTimes) {
        this(productiveTimes, new Random());
    }

    public ProductiveTimeConstraint(Collection<TimeOfDay> productiveTimes, Random random) {
        this.productiveTimes = new HashSet<>(productiveTimes);
        this.random = random;
    }

    @Override
    public boolean shouldApply(Task task) {
        Category category = task.getCategory();
        return category == Category.WORK || category == Category.LEARNING;
    }

    @Override
    public DiscreteDistribution apply(DailySchedule schedule) {
        DiscreteDistribution result = UniformDiscreteDistribution.create(schedule.getSlotCount(), random);
        if (productiveTimes.contains(TimeOfDay.ANY_TIME)) {
            return result;
        }

        int slopeWidth = schedule.getSlotCountBetween(0, 30);

        if (productiveTimes.contains(TimeOfDay.MORNING) && canAddToSchedule(schedule, MORNING_START, MORNING_END)) {
            result = result.joint(createMorningDistribution(schedule, slopeWidth));
        }

        if (productiveTimes.contains(TimeOfDay.AFTERNOON) && canAddToSchedule(schedule, AFTERNOON_START, AFTERNOON_END)) {
            result = result.joint(createAfternoonDistribution(schedule, slopeWidth));
        }

        if (productiveTimes.contains(TimeOfDay.EVENING) && canAddToSchedule(schedule, EVENING_START, EVENING_END)) {
            result = result.joint(createEveningDistribution(schedule, slopeWidth));
        }

        return result;
    }

    private boolean canAddToSchedule(DailySchedule schedule, int startMinute, int endMinute) {
        return schedule.getStartMinute() <= endMinute && schedule.getEndMinute() >= startMinute;
    }

    private DiscreteDistribution createMorningDistribution(DailySchedule schedule, int slopeWidth) {
        return createDistribution(schedule, slopeWidth, MORNING_START, MORNING_END);
    }

    private DiscreteDistribution createAfternoonDistribution(DailySchedule schedule, int slopeWidth) {
        return createDistribution(schedule, slopeWidth, AFTERNOON_START, AFTERNOON_END);
    }

    private DiscreteDistribution createEveningDistribution(DailySchedule schedule, int slopeWidth) {
        return createDistribution(schedule, slopeWidth, EVENING_START, EVENING_END);
    }

    private DiscreteDistribution createDistribution(DailySchedule schedule, int slopeWidth, int startMinute, int endMinute) {
        int peakWidth = schedule.getSlotCountBetween(Math.max(startMinute, schedule.getStartMinute()),
                Math.min(endMinute, schedule.getEndMinute()));
        int peakStart = schedule.getSlotForMinute(Math.max(startMinute, schedule.getStartMinute()));
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, schedule.getSlotCount());
    }
}
