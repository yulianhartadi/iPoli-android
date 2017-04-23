package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.UniformDiscreteDistribution;
import io.ipoli.android.app.utils.TimePreference;

import static io.ipoli.android.app.utils.Time.h2Min;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class MorningConstraint implements Constraint {

    public static final int MORNING_START = h2Min(6);
    public static final int MORNING_END = h2Min(11);

    @Override
    public boolean shouldApply(Task task) {
        return task.getStartTimePreference() == TimePreference.MORNING;
    }

    @Override
    public DiscreteDistribution apply(DailySchedule schedule) {
        int startMinute = schedule.getStartMinute();
        int endMinute = schedule.getEndMinute();

        if(startMinute > MORNING_END || endMinute < MORNING_START) {
            return UniformDiscreteDistribution.create(schedule.getSlotCount());
        }

        int slopeWidth = schedule.getSlotCountBetween(0, 30);
        int peakWidth = schedule.getSlotCountBetween(h2Min(2), h2Min(4));
        int peakIndex = schedule.getSlotForMinute(h2Min(3));
        return FlatPeakDiscreteDistribution.create(peakIndex, peakWidth, 100, slopeWidth, schedule.getSlotCount());
    }


}
