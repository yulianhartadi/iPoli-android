package io.ipoli.android.app.scheduling.constraints;

import io.ipoli.android.app.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/26/17.
 */
public abstract class BaseConstraint implements Constraint {

    private final int slotDuration;

    public BaseConstraint(int slotDuration) {
        this.slotDuration = slotDuration;
    }

    public int getSlotForMinute(int minute) {
        return minute / slotDuration;
    }

    public int getTotalSlotCount() {
        return (int) Math.ceil(Time.MINUTES_IN_A_DAY / (float) slotDuration);
    }

    /**
     * @param startMinute inclusive
     * @param endMinute   exclusive
     * @return number of time slots between the minutes
     */

    @Override
    public int getSlotCountBetween(int startMinute, int endMinute) {
        if (startMinute > endMinute) {
            int totalSlots = getSlotCountBetween(startMinute, Time.MINUTES_IN_A_DAY);
            totalSlots += getSlotCountBetween(0, endMinute);
            return totalSlots;
        } else {
            return (int) Math.ceil((endMinute - startMinute) / (float) slotDuration);
        }
    }
}