package io.ipoli.android.app.scheduling;

import java.util.BitSet;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 04/21/17.
 */
public class DailySchedule {

    private final int timeSlotDuration;
    private final BitSet isOccupied;

    public DailySchedule(int startMinute, int endMinute, int timeSlotDuration, List<Task> scheduledTasks) {
        this.timeSlotDuration = timeSlotDuration;
        isOccupied = new BitSet((endMinute - startMinute) / timeSlotDuration);

        // Task
        // start minute - inclusive
        // end minute - exclusive
        for(Task t : scheduledTasks) {
            int index = getIndex(t.getStartMinute());
            isOccupied.set(index, true);
        }
    }

    public boolean isFree(int startMinute, int endMinute) {
        BitSet subset = isOccupied.get(getIndex(startMinute), getIndex(endMinute));
        for(int i = 0 ; i < subset.length(); i++) {
            if(subset.get(i)) {
                return false;
            }
        }
        return true;
    }

    private int getIndex(int minute) {
        return minute / timeSlotDuration;
    }
}
