package io.ipoli.android.app.scheduling;

import java.util.Random;

public class TimeDiscreteDistribution extends DiscreteDistribution {

    private final int intervalLength;

    public TimeDiscreteDistribution(int intervalLength, int[] values, Random random) {
        super(values, random);
        this.intervalLength = intervalLength;
    }

    @Override
    public int sample() {
        return super.sample() * intervalLength;
    }
}
