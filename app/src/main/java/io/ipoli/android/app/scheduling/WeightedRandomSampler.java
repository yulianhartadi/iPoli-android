package io.ipoli.android.app.scheduling;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/15/16.
 */

public class WeightedRandomSampler<E> {

    private final NavigableMap<Double, E> weightToValue = new TreeMap<>();
    private final Random random;
    private double totalWeight = 0;

    public WeightedRandomSampler(Random random) {
        this.random = random;
    }

    public void add(E value, double weight) {
        totalWeight += weight;
        weightToValue.put(totalWeight, value);
    }

    public E sample() {
        double value = random.nextDouble() * totalWeight;
        return weightToValue.ceilingEntry(value).getValue();
    }
}
