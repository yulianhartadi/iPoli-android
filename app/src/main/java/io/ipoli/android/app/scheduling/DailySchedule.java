package io.ipoli.android.app.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 04/21/17.
 */
public class DailySchedule {

    private final int startMinute;
    private final int endMinute;
    private final int timeSlotDuration;
    private final boolean[] isFree;

    public DailySchedule(int startMinute, int endMinute, int timeSlotDuration) {
        this(startMinute, endMinute, timeSlotDuration, new ArrayList<>());
    }

    public DailySchedule(int startMinute, int endMinute, int timeSlotDuration, List<Task> scheduledTasks) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.timeSlotDuration = timeSlotDuration;
        isFree = createFreeSlots(scheduledTasks);
    }

    private boolean[] createFreeSlots(List<Task> scheduledTasks) {
        int slotCount = (endMinute - startMinute) / timeSlotDuration;
        boolean[] freeSlots = new boolean[slotCount];
        Arrays.fill(freeSlots, true);

        // Task
        // start minute - inclusive
        // end minute - exclusive
        for(Task t : scheduledTasks) {
            int index = getIndex(t.getStartMinute());
            freeSlots[index] = false;
        }
        return freeSlots;
    }

    public boolean isFree(int startMinute, int endMinute) {
        for(int i = getIndex(startMinute) ; i < getIndex(endMinute); i++) {
            if(!isFree[i]) {
                return false;
            }
        }
        return true;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndMinute() {
        return endMinute;
    }

    private int getIndex(int minute) {
        return (minute - startMinute) / timeSlotDuration;
    }

    public int getSlotCount() {
        return isFree.length;
    }

    public int getSlotForMinute(int minute) {
        return getIndex(minute);
    }

    /**
     * @param startMinute inclusive
     * @param endMinute exclusive
     * @return number of time slots between the minutes
     */
    public int getSlotCountBetween(int startMinute, int endMinute) {
        return (endMinute - startMinute) / timeSlotDuration;
    }
}
