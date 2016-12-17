package io.ipoli.android.app.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiscreteDistribution {

    private final WeightedRandomSampler<Integer> randomSampler;
    private final List<Double> frequencies;
    private final Random random;

    public DiscreteDistribution(int[] values, Random random) {
        this.random = random;

        frequencies = new ArrayList<>();
        double total = 0;
        for (int value : values) {
            total += value;
        }
        for (int value : values) {
            frequencies.add((value / total));
        }

        randomSampler = new WeightedRandomSampler<>(random);
        for (int i = 0; i < values.length; i++) {
            randomSampler.add(i, values[i]);
        }
    }

    public int sample() {
        return randomSampler.sample();
    }

    public double at(int position) {
        return frequencies.get(position);
    }

    public DiscreteDistribution joint(DiscreteDistribution distribution) {
        int[] values = new int[distribution.frequencies.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (int) ((at(i) * distribution.at(i)) * 100);
        }
        return new DiscreteDistribution(values, random);
    }
}