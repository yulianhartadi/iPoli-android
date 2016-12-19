package io.ipoli.android.app.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProbabilisticTaskScheduler extends TaskScheduler {

    private final Random random;

    public ProbabilisticTaskScheduler(int startHour, int endHour, List<Task> tasks, Random random) {
        super(startHour, endHour, tasks);
        this.random = random;
    }

    public ProbabilisticTaskScheduler(int startHour, int endHour, List<Task> tasks) {
        this(startHour, endHour, tasks, new Random());
    }

    public List<TimeBlock> chooseSlotsFor(Task task, int minTimeInterval, DiscreteDistribution posterior) {
        List<TimeBlock> slots = getAvailableSlotsFor(task, minTimeInterval);
        List<TimeBlock> slotsToConsider = new ArrayList<>();
        for (TimeBlock slot : slots) {
            double slotProb = 0.0;
            for (int i = slot.getStartMinute(); i <= slot.getEndMinute(); i++) {
                double prob = posterior.at(i);
                if (!(prob > 0)) {
                    slotProb = 0.0;
                    break;
                }
                slotProb += prob;
            }
            slot.setProbability(slotProb);
            if (slotProb > 0) {
                slotsToConsider.add(slot);
            }
        }
        List<TimeBlock> result = new ArrayList<>();
        for (int i = 0; i < slotsToConsider.size(); i++) {
            WeightedRandomSampler<TimeBlock> sampler = new WeightedRandomSampler<>(random);
            for (TimeBlock slot : slotsToConsider) {
                sampler.add(slot, slot.getProbability());
            }
            TimeBlock timeBlock = sampler.sample();
            result.add(timeBlock);
            slotsToConsider.remove(timeBlock);
        }
        return result;
    }
}
