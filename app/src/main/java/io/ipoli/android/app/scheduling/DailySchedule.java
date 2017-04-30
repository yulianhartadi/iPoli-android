package io.ipoli.android.app.scheduling;

import org.threeten.bp.DayOfWeek;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
import io.ipoli.android.app.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 04/21/17.
 */
public class DailySchedule {

    public static final int DEFAULT_TIME_SLOT_DURATION = 15;

    private final int startMinute;
    private final int endMinute;
    private final int timeSlotDuration;
    private final Set<DayOfWeek> workDays;
    private final Set<TimeOfDay> productiveTimes;
    private final boolean[] isFreeSlot;
    private final Random seed;
    private final List<Constraint> constraints;
    private final int workStartMinute;
    private final int workEndMinute;

    DailySchedule(int startMinute, int endMinute, int timeSlotDuration, int workStartMinute, int workEndMinute, Set<DayOfWeek> workDays, Set<TimeOfDay> productiveTimes, List<Task> scheduledTasks, Random seed) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.timeSlotDuration = timeSlotDuration;
        this.workDays = workDays;
        this.productiveTimes = productiveTimes;
        this.workStartMinute = workStartMinute;
        this.workEndMinute = workEndMinute;
        this.isFreeSlot = createFreeSlots(scheduledTasks);
        this.seed = seed;
        this.constraints = createConstraints();

    }

    private List<Constraint> createConstraints() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new MorningConstraint(DEFAULT_TIME_SLOT_DURATION));
        constraints.add(new AfternoonConstraint(DEFAULT_TIME_SLOT_DURATION));
        constraints.add(new EveningConstraint(DEFAULT_TIME_SLOT_DURATION));
        constraints.add(new WorkConstraint(workStartMinute, workEndMinute, workDays, DEFAULT_TIME_SLOT_DURATION));
        constraints.add(new NotWorkConstraint(workStartMinute, workEndMinute, workDays, DEFAULT_TIME_SLOT_DURATION));
        constraints.add(new WellnessConstraint(startMinute, DEFAULT_TIME_SLOT_DURATION));
        constraints.add(new LearningConstraint(startMinute, DEFAULT_TIME_SLOT_DURATION));
        constraints.add(new ProductiveTimeConstraint(productiveTimes, DEFAULT_TIME_SLOT_DURATION));
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

            int slotMinute = t.getStartMinute();
            while (slotMinute < t.getEndMinute()) {
                int slotIndex = getSlotForMinute(slotMinute - startMinute);
                freeSlots[slotIndex] = false;
                slotMinute += timeSlotDuration;
            }

            int index = getSlotForMinute(t.getStartMinute());
            freeSlots[index] = false;
        }
        return freeSlots;
    }

    public boolean isFree(int startMinute, int endMinute) {
        for (int i = getSlotForMinute(startMinute); i < getSlotForMinute(endMinute); i++) {
            if (!isFreeSlot[i]) {
                return false;
            }
        }
        return true;
    }

    public int getSlotForMinute(int minute) {
        return minute / timeSlotDuration;
    }

    /**
     * @param startMinute inclusive
     * @param endMinute   exclusive
     * @return number of time slots between the minutes
     */
    public int getSlotCountBetween(int startMinute, int endMinute) {
        return (int) Math.ceil((endMinute - startMinute) / (float) timeSlotDuration);
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule) {
        List<Task> result = new ArrayList<>();
        for (Task t : tasksToSchedule) {
            result.add(t);
        }
        Collections.sort(result, (t1, t2) -> -Integer.compare(t1.getPriority(), t2.getPriority()));
        for (Task t : result) {
            DiscreteDistribution dist = UniformDiscreteDistribution.create((int) Math.ceil(Time.MINUTES_IN_A_DAY / (float) timeSlotDuration));
            for (Constraint constraint : constraints) {
                if (constraint.shouldApply(t)) {
                    dist = dist.joint(constraint.apply());
                }
            }

            List<TimeBlock> timeBlocks = new ArrayList<>();
            int taskSlotCount = getSlotCountBetween(0, t.getDuration());
            for (int startSlot = 0; startSlot < isFreeSlot.length; startSlot++) {
                if (isAvailableSlot(dist, startSlot)) {
                    for (int endSlot = startSlot; endSlot < isFreeSlot.length; endSlot++) {
                        if (isNotAvailableSlot(dist, endSlot) || endSlot == isFreeSlot.length - 1) {
                            if (taskSlotCount <= endSlot - startSlot + 1) {
                                timeBlocks.addAll(cutSlotToTimeBlocks(startSlot, endSlot, taskSlotCount));
                            }
                            startSlot = endSlot + 1;
                            break;
                        }
                    }
                }
            }

            List<TimeBlock> rankedSlots = rankSlots(timeBlocks, dist);
            t.setRecommendedSlots(rankedSlots);
            if (!rankedSlots.isEmpty()) {
                TimeBlock bestSlot = rankedSlots.get(0);
                int slotMinute = bestSlot.getStartMinute();
                while (slotMinute < bestSlot.getEndMinute()) {
                    isFreeSlot[getSlotForMinute(slotMinute - startMinute)] = false;
                    slotMinute += timeSlotDuration;
                }
            }

//            for (TimeBlock tb : rankedSlots) {
//                System.out.println(tb.getStartTime() + " end: " + tb.getEndTime());
//            }
        }
        return tasksToSchedule;
    }

    private boolean isNotAvailableSlot(DiscreteDistribution dist, int endSlot) {
        return !isFreeSlot[endSlot] || dist.at(endSlot + getSlotCountBetween(0, startMinute)) <= 0;
    }

    private boolean isAvailableSlot(DiscreteDistribution dist, int startSlot) {
        return isFreeSlot[startSlot] && dist.at(startSlot + getSlotCountBetween(0, startMinute)) > 0;
    }

    private List<TimeBlock> rankSlots(List<TimeBlock> slotsToConsider, DiscreteDistribution distribution) {
        List<TimeBlock> result = new ArrayList<>();
        List<TimeBlock> availableSlots = new ArrayList<>();
        for (TimeBlock tb : slotsToConsider) {
            availableSlots.add(tb);
        }
        for (int i = 0; i < slotsToConsider.size(); i++) {
            WeightedRandomSampler<TimeBlock> sampler = new WeightedRandomSampler<>(seed);
            for (TimeBlock slot : availableSlots) {
                sampler.add(slot, distribution.at(getSlotForMinute(slot.getStartMinute())));
            }
            TimeBlock timeBlock = sampler.sample();
            result.add(timeBlock);
            availableSlots.remove(timeBlock);
        }
        return result;
    }

    private List<TimeBlock> cutSlotToTimeBlocks(int startSlot, int endSlot, int taskSlotCount) {
        int slotCount = (endSlot - startSlot + 1) - taskSlotCount + 1;
        List<TimeBlock> blocks = new ArrayList<>();
        int endTimeBlockSlot = startSlot + slotCount;
        for (int i = startSlot; i < endTimeBlockSlot; i++) {
            int startMinute = this.startMinute + i * timeSlotDuration;
            int endMinute = startMinute + taskSlotCount * timeSlotDuration;
            blocks.add(new TimeBlock(startMinute, endMinute));
        }
        return blocks;
    }
}
