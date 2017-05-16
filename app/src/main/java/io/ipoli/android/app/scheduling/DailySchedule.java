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
    private boolean[] isFreeSlot;
    private final Random seed;
    private final List<Constraint> constraints;
    private final int workStartMinute;
    private final int workEndMinute;

    private List<Task> tasks;

    DailySchedule(int startMinute, int endMinute, int timeSlotDuration, int workStartMinute, int workEndMinute, Set<DayOfWeek> workDays, Set<TimeOfDay> productiveTimes, Random seed) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.timeSlotDuration = timeSlotDuration;
        this.workDays = workDays;
        this.productiveTimes = productiveTimes;
        this.workStartMinute = workStartMinute;
        this.workEndMinute = workEndMinute;
        this.seed = seed;
        this.constraints = createConstraints();
        tasks = new ArrayList<>();

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
            fillSlots(freeSlots, t.getStartMinute(), t.getEndMinute());
        }
        return freeSlots;
    }

    private void fillSlots(boolean[] freeSlots, int startMinute, int endMinute) {
        int slotMinute = startMinute;
        while (slotMinute < endMinute) {
            int slotIndex = getSlotForMinute(slotMinute);
            freeSlots[slotIndex] = false;
            slotMinute += timeSlotDuration;
        }

        int index = getSlotForMinute(startMinute);
        freeSlots[index] = false;
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
        return (minute - startMinute) / timeSlotDuration;
    }

    /**
     * @param startMinute inclusive
     * @param endMinute   exclusive
     * @return number of time slots between the minutes
     */
    public int getSlotCountBetween(int startMinute, int endMinute) {
        return (int) Math.ceil((endMinute - startMinute) / (float) timeSlotDuration);
    }

    private List<Task> doScheduleTasks(List<Task> tasksToSchedule, Time currentTime) {
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

            List<TimeSlot> timeSlots = new ArrayList<>();
            int taskSlotCount = getSlotCountBetween(0, t.getDuration());
            for (int startSlot = getSlotForMinute(currentTime.toMinuteOfDay()); startSlot < isFreeSlot.length; startSlot++) {
                if (isAvailableSlot(dist, startSlot)) {
                    for (int endSlot = startSlot; endSlot < isFreeSlot.length; endSlot++) {
                        if (isNotAvailableSlot(dist, endSlot) || endSlot == isFreeSlot.length - 1) {
                            if (taskSlotCount <= endSlot - startSlot + 1) {
                                timeSlots.addAll(cutSlotToTimeBlocks(startSlot, endSlot, taskSlotCount));
                            }
                            startSlot = endSlot + 1;
                            break;
                        }
                    }
                }
            }

            List<TimeSlot> rankedSlots = rankSlots(timeSlots, dist);
            t.setRecommendedSlots(rankedSlots);
            if (!rankedSlots.isEmpty()) {
                TimeSlot bestSlot = rankedSlots.get(0);
                int slotMinute = bestSlot.getStartMinute();
                while (slotMinute < bestSlot.getEndMinute()) {
                    isFreeSlot[getSlotForMinute(slotMinute)] = false;
                    slotMinute += timeSlotDuration;
                }
            }

//            for (TimeSlot tb : rankedSlots) {
//                System.out.println(tb.getStartTime() + " end: " + tb.getEndTime());
//            }
        }
        return tasksToSchedule;
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule) {
        return scheduleTasks(tasksToSchedule, Time.now());
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule, Time currentTime) {
        isFreeSlot = createFreeSlots(new ArrayList<>());
        return doScheduleTasks(tasksToSchedule, currentTime);
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule, List<Task> scheduledTasks) {
        return scheduleTasks(tasksToSchedule, scheduledTasks, Time.now());
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule, List<Task> scheduledTasks, Time currentTime) {
        isFreeSlot = createFreeSlots(scheduledTasks);

        List<Task> newOrUpdatedTasks = new ArrayList<>();
        List<Task> sameTasks = new ArrayList<>();

        if (!tasks.isEmpty()) {
            for (Task tNew : tasksToSchedule) {
                boolean isNew = true;
                for (Task tOld : tasks) {
                    if (!tNew.getId().equals(tOld.getId())) {
                        continue;
                    }
                    isNew = false;
                    if (tNew.equals(tOld)) {
                        sameTasks.add(tOld);
                    } else {
                        newOrUpdatedTasks.add(tNew);
                    }
                }
                if (isNew) {
                    newOrUpdatedTasks.add(tNew);
                }
            }
        } else {
            newOrUpdatedTasks.addAll(tasksToSchedule);
        }

        tasks.clear();

        for (Task t : sameTasks) {
            if (isCurrentRecommendedSlotTaken(t)) {
                List<TimeSlot> recommendedSlots = new ArrayList<>();
                for (TimeSlot slot : t.getRecommendedSlots()) {
                    if (isFree(slot.getStartMinute(), slot.getEndMinute())) {
                        recommendedSlots.add(slot);
                    }
                }
                t.setRecommendedSlots(recommendedSlots);
            }

            TimeSlot timeSlot = t.getCurrentRecommendedSlot();
            if (timeSlot != null) {
                fillSlots(isFreeSlot, timeSlot.getStartMinute(), timeSlot.getEndMinute());
            }
        }
        tasks.addAll(sameTasks);
        tasks.addAll(doScheduleTasks(newOrUpdatedTasks, currentTime));
        return tasks;
    }

    private boolean isCurrentRecommendedSlotTaken(Task task) {
        if (task.getRecommendedSlots() == null || task.getRecommendedSlots().isEmpty()) {
            return false;
        }
        TimeSlot ts = task.getRecommendedSlots().get(0);
        return !isFree(ts.getStartMinute(), ts.getEndMinute());
    }

    private boolean isNotAvailableSlot(DiscreteDistribution dist, int endSlot) {
        return !isFreeSlot[endSlot] || dist.at(endSlot + getSlotCountBetween(0, startMinute)) <= 0;
    }

    private boolean isAvailableSlot(DiscreteDistribution dist, int startSlot) {
        return isFreeSlot[startSlot] && dist.at(startSlot + getSlotCountBetween(0, startMinute)) > 0;
    }

    private List<TimeSlot> rankSlots(List<TimeSlot> slotsToConsider, DiscreteDistribution distribution) {
        List<TimeSlot> result = new ArrayList<>();
        List<TimeSlot> availableSlots = new ArrayList<>();
        for (TimeSlot tb : slotsToConsider) {
            availableSlots.add(tb);
        }
        for (int i = 0; i < slotsToConsider.size(); i++) {
            WeightedRandomSampler<TimeSlot> sampler = new WeightedRandomSampler<>(seed);
            for (TimeSlot slot : availableSlots) {
                sampler.add(slot, distribution.at(getSlotForMinute(slot.getStartMinute())));
            }
            TimeSlot timeSlot = sampler.sample();
            result.add(timeSlot);
            availableSlots.remove(timeSlot);
        }
        return result;
    }

    private List<TimeSlot> cutSlotToTimeBlocks(int startSlot, int endSlot, int taskSlotCount) {
        int slotCount = (endSlot - startSlot + 1) - taskSlotCount + 1;
        List<TimeSlot> blocks = new ArrayList<>();
        int endTimeBlockSlot = startSlot + slotCount;
        for (int i = startSlot; i < endTimeBlockSlot; i++) {
            int startMinute = this.startMinute + i * timeSlotDuration;
            int endMinute = startMinute + taskSlotCount * timeSlotDuration;
            blocks.add(new TimeSlot(startMinute, endMinute));
        }
        return blocks;
    }
}