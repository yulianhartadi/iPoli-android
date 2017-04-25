package io.ipoli.android.app.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.scheduling.constraints.AfternoonConstraint;
import io.ipoli.android.app.scheduling.constraints.Constraint;
import io.ipoli.android.app.scheduling.constraints.EveningConstraint;
import io.ipoli.android.app.scheduling.constraints.LearningConstraint;
import io.ipoli.android.app.scheduling.constraints.MorningConstraint;
import io.ipoli.android.app.scheduling.constraints.NotWorkConstraint;
import io.ipoli.android.app.scheduling.constraints.ProductiveTimeConstraint;
import io.ipoli.android.app.scheduling.constraints.WellnessConstraint;
import io.ipoli.android.app.scheduling.constraints.WorkConstraint;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 04/21/17.
 */
public class DailySchedule {

    private final int startMinute;
    private final int endMinute;
    private final int timeSlotDuration;
    private final List<TimeOfDay> productiveTimes;
    private final boolean[] isFree;
    private final Random seed;
    private final List<Constraint> constraints;
    private final int workStartMinute;
    private final int workEndMinute;

    public DailySchedule(int startMinute, int endMinute, int timeSlotDuration, int workStartMinute, int workEndMinute, List<TimeOfDay> productiveTimes, List<Task> scheduledTasks, Random seed) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.timeSlotDuration = timeSlotDuration;
        this.productiveTimes = productiveTimes;
        this.workStartMinute = workStartMinute;
        this.workEndMinute = workEndMinute;
        isFree = createFreeSlots(scheduledTasks);
        this.seed = seed;

        this.constraints = createConstraints();

    }

    private List<Constraint> createConstraints() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new MorningConstraint());
        constraints.add(new AfternoonConstraint());
        constraints.add(new EveningConstraint());
        constraints.add(new WorkConstraint(workStartMinute, workEndMinute));
        constraints.add(new NotWorkConstraint(workStartMinute, workEndMinute));
        constraints.add(new WellnessConstraint());
        constraints.add(new LearningConstraint());
        constraints.add(new ProductiveTimeConstraint(productiveTimes));
        return constraints;
    }

    private boolean[] createFreeSlots(List<Task> scheduledTasks) {
        int slotCount = (endMinute - startMinute) / timeSlotDuration;
        boolean[] freeSlots = new boolean[slotCount];
        Arrays.fill(freeSlots, true);

        // Task
        // start minute - inclusive
        // end minute - exclusive
        for (Task t : scheduledTasks) {
            int index = getIndex(t.getStartMinute());
            freeSlots[index] = false;
        }
        return freeSlots;
    }

    public boolean isFree(int startMinute, int endMinute) {
        for (int i = getIndex(startMinute); i < getIndex(endMinute); i++) {
            if (!isFree[i]) {
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
     * @param endMinute   exclusive
     * @return number of time slots between the minutes
     */
    public int getSlotCountBetween(int startMinute, int endMinute) {
        return (endMinute - startMinute) / timeSlotDuration;
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule) {
        return tasksToSchedule;
    }
}
