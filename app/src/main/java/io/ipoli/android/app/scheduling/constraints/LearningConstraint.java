package io.ipoli.android.app.scheduling.constraints;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */

public class LearningConstraint {

    public boolean shouldApply(Quest quest) {
        return quest.getCategoryType() == Category.LEARNING;
    }

    public DiscreteDistribution apply(DailySchedule schedule) {
        int slopeWidth = schedule.getSlotCountBetween(h2Min(1) + 30, h2Min(2));
        DiscreteDistribution d1 = createStartOfDayPeak(schedule, slopeWidth);
        DiscreteDistribution d2 = createEndOfDayPeak(schedule, slopeWidth);
        DiscreteDistribution d = d1.joint(d2);
        return d.set(0, 0.0);
    }

    private DiscreteDistribution createStartOfDayPeak(DailySchedule schedule, int slopeWidth) {
        int peakWidth = schedule.getSlotCountBetween(h2Min(2), h2Min(4));
        int peakIndex = schedule.getSlotForMinute(h2Min(3));
        return FlatPeakDiscreteDistribution.create(peakIndex, peakWidth, 100, slopeWidth, schedule.getSlotCount());
    }

    private DiscreteDistribution createEndOfDayPeak(DailySchedule schedule, int slopeWidth) {
        int peakWidth = schedule.getSlotCountBetween(h2Min(10), h2Min(12));
        int peakIndex = schedule.getSlotForMinute(h2Min(11));
        return FlatPeakDiscreteDistribution.create(peakIndex, peakWidth, 100, slopeWidth, schedule.getSlotCount());
    }

    private int h2Min(int hours) {
        return (int) TimeUnit.HOURS.toMinutes(hours);
    }
}
