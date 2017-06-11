package io.ipoli.android.app.scheduling;

import android.support.annotation.NonNull;

import com.google.firebase.crash.FirebaseCrash;

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

import static io.ipoli.android.app.utils.Time.*;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 04/21/17.
 */
public class DailyScheduler {

    public static final int DEFAULT_TIME_SLOT_DURATION = 15;

    private final int startMinute;
    private final int endMinute;
    private final int timeSlotDuration;
    private final Set<DayOfWeek> workDays;
    private final Set<TimeOfDay> productiveTimes;
    private boolean[] freeSlots;
    private final Random seed;
    private final List<Constraint> constraints;
    private final int workStartMinute;
    private final int workEndMinute;

    private List<Task> tasks;

    DailyScheduler(int startMinute, int endMinute, int timeSlotDuration, int workStartMinute, int workEndMinute, Set<DayOfWeek> workDays, Set<TimeOfDay> productiveTimes, Random seed) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.timeSlotDuration = timeSlotDuration;
        this.workDays = workDays;
        this.productiveTimes = productiveTimes;
        this.workStartMinute = workStartMinute;
        this.workEndMinute = workEndMinute;
        this.seed = seed;
        this.constraints = createConstraints();
        this.tasks = new ArrayList<>();
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
        boolean[] freeSlots = new boolean[getSlotCount()];
        Arrays.fill(freeSlots, true);
        for (Task t : scheduledTasks) {
            int startMinute = t.getStartMinute();
            int endMinute = t.getEndMinute();

            if (!isInSchedule(startMinute) && !isInSchedule(endMinute)) {
                continue;
            }

            if (isInSchedule(startMinute) && !isInSchedule(endMinute)) {
                endMinute = this.endMinute;
            }

            if (!isInSchedule(startMinute) && isInSchedule(endMinute)) {
                startMinute = this.startMinute;
            }
            occupySlots(freeSlots, startMinute, endMinute);
        }
        return freeSlots;
    }

    private boolean isInSchedule(int minute) {
        if (startMinute <= endMinute && (minute < startMinute || minute >= endMinute)) {
            return false;
        }

        if (startMinute >= endMinute && (minute >= endMinute && minute < startMinute)) {
            return false;
        }
        return true;
    }

    private int getSlotCount() {
        int slotCount = (int) Math.ceil((endMinute - startMinute) / (float) timeSlotDuration);
        if (startMinute > endMinute) {
            int slotsInADay = MINUTES_IN_A_DAY / timeSlotDuration;
            slotCount = slotsInADay + slotCount;
        }
        return slotCount;
    }

    private void occupySlots(boolean[] freeSlots, int startMinute, int endMinute) {
        populateSlots(freeSlots, false, startMinute, endMinute);
    }

    private void freeUpSlots(boolean[] freeSlots, int startMinute, int endMinute) {
        populateSlots(freeSlots, true, startMinute, endMinute);
    }

    /**
     * @param startMinute inclusive
     * @param endMinute   exclusive
     */
    private void populateSlots(boolean[] slots, boolean value, int startMinute, int endMinute) {
        if(endMinute < startMinute) {
            endMinute = MINUTES_IN_A_DAY + endMinute;
        }
        int slotMinute = startMinute;
        while (slotMinute < endMinute) {
            int slotIndex = getSlotForMinute(slotMinute % MINUTES_IN_A_DAY);
            if(slotIndex >= slots.length) {
                StringBuilder builder = new StringBuilder();
                builder.append("Slot index:").append(slotIndex)
                        .append(" slot minute:").append(slotMinute)
                        .append(" slot count:").append(slots.length)
                        .append(" startMinute:").append(startMinute)
                        .append(" endMinute:").append(endMinute)
                        .append(" schedule startMinute:").append(this.startMinute)
                        .append(" schedule endMinute:").append(this.endMinute)
                        .append(" workStartMinute:").append(workStartMinute)
                        .append(" workEndMinute:").append(workEndMinute);
                FirebaseCrash.report(new ArrayIndexOutOfBoundsException(builder.toString()));
                break;
            }
            slots[slotIndex] = value;
            slotMinute += timeSlotDuration;
        }
    }

    public boolean isFree(int startMinute, int endMinute) {
        if (startMinute < 0 && startMinute > MINUTES_IN_A_DAY) {
            throw new IllegalArgumentException("Start minute out of bounds: " + startMinute);
        }
        if (endMinute < 0 && endMinute > MINUTES_IN_A_DAY) {
            throw new IllegalArgumentException("End minute out of bounds: " + endMinute);
        }

        if (this.startMinute <= this.endMinute && startMinute <= endMinute) {
            if (startMinute < this.startMinute || endMinute > this.endMinute) {
                return false;
            }
        }

        if (this.startMinute <= this.endMinute && startMinute > endMinute) {
            return false;
        }

        if (this.startMinute >= this.endMinute) {
            if ((startMinute >= this.endMinute && startMinute < this.startMinute) || (endMinute > this.endMinute && endMinute <= this.startMinute)) {
                return false;
            }
        }

        int startIndex = getSlotForMinute(startMinute);
        int endIndex = startIndex + getSlotCountBetween(startMinute, endMinute);
        for (int slotIndex = startIndex; slotIndex < endIndex; slotIndex++) {
            if (!freeSlots[slotIndex]) {
                return false;
            }
        }
        return true;
    }

    public int getSlotForMinute(int minute) {

        if (minute < 0 || minute > MINUTES_IN_A_DAY) {
            throw new IllegalArgumentException("Minute out of bounds: " + minute);
        }

        if (startMinute > minute) {
            int mins = MINUTES_IN_A_DAY - startMinute;
            return (minute + mins) / timeSlotDuration;
        } else {
            return (minute - startMinute) / timeSlotDuration;
        }
    }

    /**
     * @param startMinute inclusive
     * @param endMinute   exclusive
     * @return number of time slots between the minutes
     */
    private int getSlotCountBetween(int startMinute, int endMinute) {
        if (startMinute > endMinute) {
            int totalSlots = getSlotCountBetween(startMinute, MINUTES_IN_A_DAY);
            totalSlots += getSlotCountBetween(0, endMinute);
            return totalSlots;
        } else {
            return (int) Math.ceil((endMinute - startMinute) / (float) timeSlotDuration);
        }
    }

    private List<Task> doScheduleTasks(List<Task> tasksToSchedule, Time currentTime) {
        List<Task> result = new ArrayList<>();
        for (Task t : tasksToSchedule) {
            result.add(t);
        }
        Collections.sort(result, (t1, t2) -> -Integer.compare(t1.getPriority(), t2.getPriority()));
        for (Task t : result) {
            DiscreteDistribution dist = UniformDiscreteDistribution.create((int) Math.ceil(MINUTES_IN_A_DAY / (float) timeSlotDuration));
            for (Constraint constraint : constraints) {
                if (constraint.shouldApply(t)) {
                    dist = dist.joint(constraint.apply());
                }
            }

            List<TimeSlot> timeSlots = createTimeSlotsForTask(t, dist, currentTime);

            List<TimeSlot> rankedSlots = rankSlots(timeSlots, dist);
            t.setRecommendedSlots(rankedSlots);
            updateTaskCurrentSlot(t);

            TimeSlot currentSlot = t.getCurrentTimeSlot();
            if (currentSlot != null) {
                occupySlots(freeSlots, currentSlot.getStartMinute(), currentSlot.getEndMinute());
            }
        }
        return tasksToSchedule;
    }

    private void updateTaskCurrentSlot(Task task) {
        for (int i = 0; i < task.getRecommendedSlots().size(); i++) {
            TimeSlot ts = task.getRecommendedSlots().get(i);
            if (isFree(ts.getStartMinute(), ts.getEndMinute())) {
                task.setCurrentTimeSlotIndex(i);
                return;
            }
        }
        task.setCurrentTimeSlotIndex(-1);
    }

    @NonNull
    private List<TimeSlot> createTimeSlotsForTask(Task task, DiscreteDistribution dist, Time currentTime) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        int taskSlotCount = getSlotCountBetween(0, task.getDuration());
        int startMinute = Math.max(currentTime.toMinuteOfDay(), this.startMinute);
        int startSlot = getSlotForMinute(startMinute);
        if (isNotAtSlotMinute(of(startMinute))) {
            startSlot = Math.min(startSlot + 1, freeSlots.length - 1);
        }
        for (int curSlot = startSlot; curSlot < freeSlots.length; curSlot++) {
            if (isAvailableSlot(dist, curSlot)) {
                for (int endSlot = curSlot; endSlot < freeSlots.length; endSlot++) {
                    if (isNotAvailableSlot(dist, endSlot) || endSlot == freeSlots.length - 1) {
                        if (taskSlotCount <= endSlot - curSlot + 1) {
                            timeSlots.addAll(cutSlotToTimeBlocks(curSlot, endSlot, taskSlotCount, task.getDuration()));
                        }
                        curSlot = endSlot + 1;
                        break;
                    }
                }
            }
        }
        return timeSlots;
    }

    private boolean isNotAtSlotMinute(Time currentTime) {
        return currentTime.toMinuteOfDay() % timeSlotDuration != 0;
    }

    private boolean isNotAvailableSlot(DiscreteDistribution dist, int slot) {
        int slotIdx = (slot + getSlotCountBetween(0, startMinute)) % dist.size();
        return dist.at(slotIdx) <= 0;
    }

    private boolean isAvailableSlot(DiscreteDistribution dist, int slot) {
        int slotIdx = (slot + getSlotCountBetween(0, startMinute)) % dist.size();
        return dist.at(slotIdx) > 0;
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule) {
        return scheduleTasks(tasksToSchedule, now());
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule, Time currentTime) {
        return scheduleTasks(tasksToSchedule, new ArrayList<>(), currentTime);
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule, List<Task> scheduledTasks) {
        return scheduleTasks(tasksToSchedule, scheduledTasks, now());
    }

    public List<Task> scheduleTasks(List<Task> tasksToSchedule, List<Task> scheduledTasks, Time currentTime) {
        freeSlots = createFreeSlots(scheduledTasks);

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
            updateTaskCurrentSlot(t);

            TimeSlot timeSlot = t.getCurrentTimeSlot();
            if (timeSlot != null) {
                occupySlots(freeSlots, timeSlot.getStartMinute(), timeSlot.getEndMinute());
            }
        }
        tasks.addAll(sameTasks);
        tasks.addAll(doScheduleTasks(newOrUpdatedTasks, currentTime));
        return tasks;
    }


    private List<TimeSlot> rankSlots(List<TimeSlot> slotsToConsider, DiscreteDistribution distribution) {
        List<TimeSlot> result = new ArrayList<>();
        List<TimeSlot> availableSlots = new ArrayList<>();
        for (TimeSlot ts : slotsToConsider) {
            availableSlots.add(ts);
        }
        for (int i = 0; i < slotsToConsider.size(); i++) {
            WeightedRandomSampler<TimeSlot> sampler = new WeightedRandomSampler<>(seed);
            for (TimeSlot slot : availableSlots) {
                sampler.add(slot, distribution.at(slot.getStartMinute() / timeSlotDuration));
            }
            TimeSlot timeSlot = sampler.sample();
            result.add(timeSlot);
            availableSlots.remove(timeSlot);
        }
        return result;
    }

    private List<TimeSlot> cutSlotToTimeBlocks(int startSlot, int endSlot, int taskSlotCount, int taskDuration) {
        int slotCount = (endSlot - startSlot + 1) - taskSlotCount + 1;
        List<TimeSlot> blocks = new ArrayList<>();
        int endTimeBlockSlot = startSlot + slotCount;
        for (int i = startSlot; i < endTimeBlockSlot; i++) {
            int startMinute = (this.startMinute + i * timeSlotDuration) % MINUTES_IN_A_DAY;
            int endMinute = (startMinute + taskSlotCount * timeSlotDuration) % MINUTES_IN_A_DAY;
            if(i == endTimeBlockSlot - 1) {
                endMinute = Math.min(endMinute, startMinute + taskDuration);
            }
            blocks.add(new TimeSlot(startMinute, endMinute));
        }
        return blocks;
    }

    public Task chooseNewTimeSlot(String taskId, Time currentTime) {
        for (Task t : tasks) {
            if (t.getId().equals(taskId)) {
                freeUpSlots(freeSlots, t.getCurrentTimeSlot().getStartMinute(), t.getCurrentTimeSlot().getEndMinute());

                int size = t.getRecommendedSlots().size();
                int end = size + t.getCurrentTimeSlotIndex();
                for (int i = t.getCurrentTimeSlotIndex(); i < end; i++) {
                    int idx = (i + 1) % size;
                    TimeSlot slot = t.getRecommendedSlots().get(idx);
                    if (!isFree(slot.getStartMinute(), slot.getEndMinute()) ||
                            currentTime.toMinuteOfDay() > slot.getStartMinute()) {
                        continue;
                    }

                    t.setCurrentTimeSlotIndex(idx);
                    occupySlots(freeSlots, t.getCurrentTimeSlot().getStartMinute(), t.getCurrentTimeSlot().getEndMinute());
                    return t;
                }
                t.setRecommendedSlots(new ArrayList<>());
                t.setCurrentTimeSlotIndex(-1);
                return t;
            }
        }
        return null;
    }
}