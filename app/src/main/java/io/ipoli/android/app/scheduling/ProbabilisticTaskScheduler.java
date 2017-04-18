package io.ipoli.android.app.scheduling;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.ipoli.android.app.utils.Time;

public class ProbabilisticTaskScheduler extends TaskScheduler {

    private final Random random;

    public ProbabilisticTaskScheduler(int startHour, int endHour, List<Task> tasks, Random random) {
        super(startHour, endHour, tasks);
        this.random = random;
    }

    public List<TimeBlock> chooseSlotsFor(Task task, int minTimeInterval, Time currentTime, DiscreteDistribution posterior) {
        List<TimeBlock> availableSlots = getAvailableSlotsFor(task, minTimeInterval);
        List<TimeBlock> slotsToConsider = filterPossibleSlots(posterior, availableSlots, currentTime);
        return rankSlots(slotsToConsider);
    }

    @NonNull
    private List<TimeBlock> rankSlots(List<TimeBlock> slotsToConsider) {
        List<TimeBlock> result = new ArrayList<>();
        List<TimeBlock> availableSlots = new ArrayList<>();
        for (TimeBlock tb : slotsToConsider) {
            availableSlots.add(tb);
        }
        for (int i = 0; i < slotsToConsider.size(); i++) {
            WeightedRandomSampler<TimeBlock> sampler = new WeightedRandomSampler<>(random);
            for (TimeBlock slot : availableSlots) {
                sampler.add(slot, slot.getProbability());
            }
            TimeBlock timeBlock = sampler.sample();
            result.add(timeBlock);
            availableSlots.remove(timeBlock);
        }
        return result;
    }

    @NonNull
    private List<TimeBlock> filterPossibleSlots(DiscreteDistribution posterior, List<TimeBlock> slots, Time currentTime) {
        List<TimeBlock> slotsToConsider = new ArrayList<>();
        for (TimeBlock slot : slots) {
            if (currentTime.toMinuteOfDay() > slot.getStartMinute()) {
                continue;
            }
            double slotProb = findSlotProbabilityDistribution(posterior, slot);
            if (slotProb <= 0) {
                continue;
            }
            slot.setProbability(slotProb);
            slotsToConsider.add(slot);
        }
        return slotsToConsider;
    }

    /**
     * If slot probability is not defined at any value - the total probability of the slot is 0
     */
    private double findSlotProbabilityDistribution(DiscreteDistribution posterior, TimeBlock slot) {
        double slotProb = 0.0;
        for (int i = slot.getStartMinute(); i <= slot.getEndMinute(); i++) {
            double prob = posterior.at(i);
            if (!(prob > 0)) {
                return 0;
            }
            slotProb += prob;
        }
        return slotProb;
    }
}
