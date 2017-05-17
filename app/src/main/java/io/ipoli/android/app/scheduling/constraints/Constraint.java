package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public interface Constraint {

    boolean shouldApply(Task task);

    DiscreteDistribution apply();

    int getSlotForMinute(int minute);

    int getTotalSlotCount();

    /**
     * @param startMinute inclusive
     * @param endMinute   exclusive
     * @return number of time slots between the minutes
     */
    int getSlotCountBetween(int startMinute, int endMinute);
}
