package io.ipoli.android.app.scheduling.distributions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DiscreteDistribution implements Iterable<Double> {

    private final List<Double> frequencies;
    private final double[] values;

    public DiscreteDistribution(double[] values) {
        this.values = values;

        frequencies = new ArrayList<>();
        double total = 0;
        for (double value : values) {
            total += value;
        }
        for (double value : values) {
            frequencies.add((value / total));
        }
    }


    public double at(int position) {
        if (position < 0 || position >= frequencies.size()) {
            return 0;
        }
        return frequencies.get(position);
    }

    public DiscreteDistribution joint(DiscreteDistribution distribution) {
        double[] values = new double[distribution.frequencies.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = at(i) * distribution.at(i);
        }
        return new DiscreteDistribution(values);
    }

    public DiscreteDistribution add(DiscreteDistribution distribution) {
        double[] values = new double[distribution.frequencies.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = at(i) + distribution.at(i);
        }
        return new DiscreteDistribution(values);
    }

    public int size() {
        return frequencies.size();
    }

    public DiscreteDistribution set(int index, double value) {
        double[] values = Arrays.copyOf(this.values, this.values.length);
        values[index] = value;
        return new DiscreteDistribution(values);
    }

    @Override
    public Iterator<Double> iterator() {
        return frequencies.iterator();
    }

    public List<Double> toList() {
        List<Double> values = new ArrayList<>();
        for (double v : frequencies) {
            values.add(v);
        }
        return values;
    }

    public DiscreteDistribution inverse() {
        double[] inverseValues = new double[values.length];
        double maxElement = -1;
        for (int i = 0; i < values.length; i++) {
            double val = values[i];
            if (val > maxElement) {
                maxElement = val;
            }
            inverseValues[i] = val;
        }

        for (int i = 0; i < inverseValues.length; i++) {
            inverseValues[i] = maxElement - inverseValues[i];
        }

        return new DiscreteDistribution(inverseValues);
    }
}