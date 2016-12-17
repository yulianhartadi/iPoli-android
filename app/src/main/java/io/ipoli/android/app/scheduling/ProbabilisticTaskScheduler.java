package io.ipoli.android.app.scheduling;

import java.util.List;
import java.util.Random;

public class ProbabilisticTaskScheduler extends TaskScheduler {

    private final WeightedRandomSampler<TimeBlock> sampler;

    public ProbabilisticTaskScheduler(int startHour, int endHour, List<Task> tasks, Random random) {
        super(startHour, endHour, tasks);
        this.sampler = new WeightedRandomSampler<>(random);
    }

    public TimeBlock chooseSlotFor(Task task, int minTimeInterval, DiscreteDistribution posterior) {
        List<TimeBlock> slots = getAvailableSlotsFor(task, minTimeInterval);
        for (TimeBlock slot : slots) {
            double slotProb = 0.0;
            for (int i = slot.getStartMinute(); i <= slot.getEndMinute(); i++) {
                slotProb += posterior.at(i);
            }
            sampler.add(slot, slotProb);
        }
        return sampler.sample();
    }
}
