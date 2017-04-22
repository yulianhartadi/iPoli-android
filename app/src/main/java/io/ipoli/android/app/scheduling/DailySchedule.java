package io.ipoli.android.app.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 04/21/17.
 */
public class DailySchedule {

    private final int timeSlotDuration;
    private final boolean[] isFree;

    public DailySchedule(int startMinute, int endMinute, int timeSlotDuration) {
        this(startMinute, endMinute, timeSlotDuration, new ArrayList<>());
    }

    public DailySchedule(int startMinute, int endMinute, int timeSlotDuration, List<Task> scheduledTasks) {
        this.timeSlotDuration = timeSlotDuration;
        int slotCount = (endMinute - startMinute) / timeSlotDuration;
        isFree = new boolean[slotCount];
        Arrays.fill(isFree, true);

        // Task
        // start minute - inclusive
        // end minute - exclusive
        for(Task t : scheduledTasks) {
            int index = getIndex(t.getStartMinute());
            isFree[index] = false;
        }
    }

    public boolean isFree(int startMinute, int endMinute) {
        for(int i = getIndex(startMinute) ; i < getIndex(endMinute); i++) {
            if(!isFree[i]) {
                return false;
            }
        }
        return true;
    }

    private int getIndex(int minute) {
        return minute / timeSlotDuration;
    }

    public int getSlotCount() {
        return isFree.length;
    }

    public int getSlotForMinute(int minute) {
        return getIndex(minute);
    }
}
