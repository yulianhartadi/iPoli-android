package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.quest.data.Category;

import static io.ipoli.android.app.utils.Time.h2Min;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/24/17.
 */

public class WellnessConstraint implements Constraint {

    @Override
    public boolean shouldApply(Task task) {
        return task.getCategory() == Category.WELLNESS;
    }

    @Override
    public DiscreteDistribution apply(DailySchedule schedule) {
        int slopeWidth = schedule.getSlotCountBetween(0, 30);
        DiscreteDistribution d1 = createMorningPeak(schedule, slopeWidth);
        DiscreteDistribution d2 = createAfternoonPeak(schedule, slopeWidth);
        return d1.joint(d2);
    }

    private DiscreteDistribution createMorningPeak(DailySchedule schedule, int slopeWidth) {
        int startMinute = schedule.getStartMinute() + 30;
        int peakWidth = schedule.getSlotCountBetween(startMinute, startMinute + h2Min(3));
        int peakStart = schedule.getSlotForMinute(startMinute);
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, schedule.getSlotCount());
    }

    private DiscreteDistribution createAfternoonPeak(DailySchedule schedule, int slopeWidth) {
        int startMinute = h2Min(17);
        int peakWidth = schedule.getSlotCountBetween(startMinute, h2Min(20));
        int peakStart = schedule.getSlotForMinute(startMinute);
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, schedule.getSlotCount());
    }
}
