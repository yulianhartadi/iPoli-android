package io.ipoli.android.app.scheduling.distributions;

import java.util.Arrays;
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
        return create(valueCount, new Random());
    }

    public static UniformDiscreteDistribution create(int valueCount, Random random) {
        double[] values = new double[valueCount];
        Arrays.fill(values, 1);
        return new UniformDiscreteDistribution(values, random);
    }
}
