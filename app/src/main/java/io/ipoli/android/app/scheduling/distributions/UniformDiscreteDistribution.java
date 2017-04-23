package io.ipoli.android.app.scheduling.distributions;

import java.util.Random;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class UniformDiscreteDistribution extends DiscreteDistribution {

    public UniformDiscreteDistribution(double[] values, Random random) {
        super(values, random);
    }

    public static UniformDiscreteDistribution create(int valueCount) {
        return null;
    }
}
