package io.ipoli.android.app.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiscreteDistribution {

    private final WeightedRandomSampler<Integer> randomSampler;
    private final List<Double> frequencies;
    private final Random random;

    public DiscreteDistribution(double[] values, Random random) {
        this.random = random;

        frequencies = new ArrayList<>();
        double total = 0;
        for (double value : values) {
            total += value;
        }
        for (double value : values) {
            frequencies.add((value / total));
        }

        randomSampler = new WeightedRandomSampler<>(random);
        for (int i = 0; i < values.length; i++) {
            randomSampler.add(i, values[i]);
        }
    }

    public DiscreteDistribution(double[] values) {
        this(values, new Random());
    }

    public int sample() {
        return randomSampler.sample();
    }

    public double at(int position) {
        if(position < 0 || position >= frequencies.size()) {
            return 0;
        }
        return frequencies.get(position);
    }

    public DiscreteDistribution joint(DiscreteDistribution distribution) {
        double[] values = new double[distribution.frequencies.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = at(i) * distribution.at(i);
        }
        return new DiscreteDistribution(values, random);
    }

    public DiscreteDistribution add(DiscreteDistribution distribution) {
        double[] values = new double[distribution.frequencies.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = at(i) + distribution.at(i);
        }
        return new DiscreteDistribution(values, random);
    }
}