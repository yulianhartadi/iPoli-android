package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.UniformDiscreteDistribution;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class ProductiveTimeConstraint implements Constraint {

    private final int productiveTimeStartMinute;
    private final int productiveTimeEndMinute;

    public ProductiveTimeConstraint(int productiveTimeStartMinute, int productiveTimeEndMinute) {
        this.productiveTimeStartMinute = productiveTimeStartMinute;
        this.productiveTimeEndMinute = productiveTimeEndMinute;
    }

    @Override
    public boolean shouldApply(Task task) {
        Category category = task.getCategory();
        return category == Category.WORK || category == Category.LEARNING;
    }

    @Override
    public DiscreteDistribution apply(DailySchedule schedule) {
        int scheduleStart = schedule.getStartMinute();
        int scheduleEnd = schedule.getEndMinute();

        if(scheduleStart > productiveTimeStartMinute || scheduleEnd < productiveTimeEndMinute) {
            return UniformDiscreteDistribution.create(schedule.getSlotCount());
        }

        int slopeWidth = schedule.getSlotCountBetween(0, 30);
        int peakWidth = schedule.getSlotCountBetween(Math.max(productiveTimeStartMinute, scheduleStart), Math.min(productiveTimeEndMinute, scheduleEnd));
        int peakStart = schedule.getSlotForMinute(Math.max(productiveTimeStartMinute, scheduleStart));
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, schedule.getSlotCount());

    }
}
