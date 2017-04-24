package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.UniformDiscreteDistribution;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/24/17.
 */
public abstract class SoftConstraint implements Constraint {

    private final int startMinute;
    private final int endMinute;

    public SoftConstraint(int startMinute, int endMinute) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    @Override
    public DiscreteDistribution apply(DailySchedule schedule) {
        int scheduleStart = schedule.getStartMinute();
        int scheduleEnd = schedule.getEndMinute();

        if(scheduleStart > endMinute || scheduleEnd < startMinute) {
            return UniformDiscreteDistribution.create(schedule.getSlotCount());
        }

        int slopeWidth = schedule.getSlotCountBetween(0, 30);
        int peakWidth = schedule.getSlotCountBetween(Math.max(startMinute, scheduleStart), Math.min(endMinute, scheduleEnd));
        int peakStart = schedule.getSlotForMinute(Math.max(startMinute, scheduleStart));
        return FlatPeakDiscreteDistribution.create(peakStart, peakWidth, 100, slopeWidth, schedule.getSlotCount());
    }
}
