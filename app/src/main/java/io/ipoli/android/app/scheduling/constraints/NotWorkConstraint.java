package io.ipoli.android.app.scheduling.constraints;

import java.util.Arrays;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class NotWorkConstraint implements Constraint {

    private final int workStartMinute;
    private final int workEndMinute;

    public NotWorkConstraint(int workStartMinute, int workEndMinute) {
        this.workStartMinute = workStartMinute;
        this.workEndMinute = workEndMinute - 1;
    }

    @Override
    public boolean shouldApply(Task task) {
        return task.getCategory() != Category.WORK;
    }

    @Override
    public DiscreteDistribution apply(DailySchedule schedule) {
        double[] vals = new double[schedule.getSlotCount()];
        Arrays.fill(vals, 1.0);
        int startSlot = schedule.getSlotForMinute(workStartMinute);
        int endSlot = schedule.getSlotForMinute(workEndMinute);
        for(int i = startSlot ; i <= endSlot; i++) {
            vals[i] = 0;
        }
        return new DiscreteDistribution(vals);
    }
}
