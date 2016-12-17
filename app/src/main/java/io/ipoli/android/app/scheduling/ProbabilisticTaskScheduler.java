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

    public List<TimeBlock> chooseSlotsFor(Task task, int minTimeInterval, DiscreteDistribution posterior) {
        List<TimeBlock> slots = getAvailableSlotsFor(task, minTimeInterval);
        for (TimeBlock slot : slots) {
            double slotProb = 0.0;
            for (int i = slot.getStartMinute(); i <= slot.getEndMinute(); i++) {
                slotProb += posterior.at(i);
            }
            slot.setProbability(slotProb);
        }
        List<TimeBlock> result = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            WeightedRandomSampler<TimeBlock> sampler = new WeightedRandomSampler<>(random);
            for (TimeBlock slot : slots) {
                sampler.add(slot, slot.getProbability());
            }
            result.add(sampler.sample());
        }
        return result;
    }
}
