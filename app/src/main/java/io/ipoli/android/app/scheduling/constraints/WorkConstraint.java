package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.DailySchedule;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */

public class WorkConstraint {

    private final int workStartMinute;
    private final int workEndMinute;

    public WorkConstraint(int workStartMinute, int workEndMinute) {
        this.workStartMinute = workStartMinute;
        this.workEndMinute = workEndMinute - 1;
    }

    public boolean shouldApply(Quest quest) {
        return quest.getCategoryType() == Category.WORK;
    }

    public DiscreteDistribution apply(DailySchedule schedule) {
        double[] vals = new double[schedule.getSlotCount()];
        int startSlot = schedule.getSlotForMinute(workStartMinute);
        int endSlot = schedule.getSlotForMinute(workEndMinute);
        for(int i = startSlot ; i <= endSlot; i++) {
            vals[i] = 1;
        }
        return new DiscreteDistribution(vals);
    }
}
