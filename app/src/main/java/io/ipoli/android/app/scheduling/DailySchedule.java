package io.ipoli.android.app.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;
import io.ipoli.android.app.scheduling.distributions.UniformDiscreteDistribution;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 04/21/17.
 */
public class DailySchedule {

    private final int startMinute;
    private final int endMinute;
    private final int timeSlotDuration;
    private final List<TimeOfDay> productiveTimes;
    private final boolean[] isFreeSlot;
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
        isFreeSlot = createFreeSlots(scheduledTasks);
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
            if (!isFreeSlot[i]) {
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
        return isFreeSlot.length;
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
        List<Task> result = new ArrayList<>();
        for (Task t : tasksToSchedule) {
            result.add(t);
        }
        Collections.sort(result, (t1, t2) -> -Integer.compare(t1.getPriority(), t2.getPriority()));
        for (Task t : result) {
            DiscreteDistribution dist = UniformDiscreteDistribution.create(getSlotCount(), seed);
            for (Constraint constraint : constraints) {
                if (constraint.shouldApply(t)) {
                    dist = dist.joint(constraint.apply(this));
                }
            }

            List<TimeBlock> timeBlocks = new ArrayList<>();
            int taskSlotCount = getSlotCountBetween(0, t.getDuration());
            int startSlot = 0;
            int endSlot = 0;
            for (int i = 0; i < isFreeSlot.length; i++) {
                if (isFreeSlot[i] && dist.at(i) > 0) {
                    endSlot = i;
                } else {
                    if (taskSlotCount <= endSlot - startSlot + 1) {
                        timeBlocks.addAll(cutSlotToTimeBlocks(startSlot, endSlot, taskSlotCount));
                    }
                    startSlot = i + 1;
                    endSlot = i + 1;
                }
            }
        }
        return tasksToSchedule;
    }

    private List<TimeBlock> cutSlotToTimeBlocks(int startSlot, int endSlot, int taskSlotCount) {
        int slotCount = (endSlot - startSlot + 1) - taskSlotCount + 1;
        List<TimeBlock> blocks = new ArrayList<>();
        int endTimeBlockSlot = startSlot + slotCount;
        for (int i = startSlot; i < endTimeBlockSlot; i++) {
            int startMinute = i * timeSlotDuration;
            int endMinute = startMinute + taskSlotCount * timeSlotDuration;
            blocks.add(new TimeBlock(startMinute, endMinute));
        }
        return blocks;
    }
}
