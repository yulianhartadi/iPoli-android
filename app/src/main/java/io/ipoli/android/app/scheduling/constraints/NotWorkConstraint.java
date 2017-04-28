package io.ipoli.android.app.scheduling.constraints;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.Set;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.FlatPeakDiscreteDistribution;
import io.ipoli.android.app.utils.TimePreference;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class NotWorkConstraint extends BaseConstraint {

    private final int workStartMinute;
    private final int workEndMinute;
    private final Set<DayOfWeek> workDays;
    private final LocalDate today;

    public NotWorkConstraint(int workStartMinute, int workEndMinute, Set<DayOfWeek> workDays, int slotDuration) {
        super(slotDuration);
        this.workStartMinute = workStartMinute;
        this.workEndMinute = workEndMinute;
        this.workDays = workDays;
        this.today = LocalDate.now();
    }

    @Override
    public boolean shouldApply(Task task) {
        return task.getCategory() != Category.WORK &&
                task.getStartTimePreference() != TimePreference.ANY &&
                workDays.contains(today.getDayOfWeek());
    }

    @Override
    public DiscreteDistribution apply() {
        return FlatPeakDiscreteDistribution.create(getSlotForMinute(workStartMinute),
                getSlotForMinute(workEndMinute - workStartMinute),
                1,
                0,
                getTotalSlotCount(),
                0).inverse();
    }
}
