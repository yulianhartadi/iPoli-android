package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */

public class WorkConstraint extends BaseConstraint {

    private final int workStartMinute;
    private final int workEndMinute;

    public WorkConstraint(int workStartMinute, int workEndMinute, int slotDuration) {
        super(slotDuration);
        this.workStartMinute = workStartMinute;
        this.workEndMinute = workEndMinute;
    }

    @Override
    public boolean shouldApply(Task task) {
        return task.getCategory() == Category.WORK;
    }

    @Override
    public DiscreteDistribution apply() {
        return FlatPeakDiscreteDistribution.create(getSlotForMinute(workStartMinute),
                getSlotForMinute(workEndMinute - workStartMinute),
                1,
                0,
                getTotalSlotCount(),
                0);
    }
}
