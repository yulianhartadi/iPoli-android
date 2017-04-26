package io.ipoli.android.app.scheduling.distributions;

import java.util.Arrays;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/23/17.
 */
public class UniformDiscreteDistribution extends DiscreteDistribution {

    private UniformDiscreteDistribution(double[] values) {
        super(values);
    }

    public static UniformDiscreteDistribution create(int valueCount) {
        double[] values = new double[valueCount];
        Arrays.fill(values, 1);
        return new UniformDiscreteDistribution(values);
    }
}
