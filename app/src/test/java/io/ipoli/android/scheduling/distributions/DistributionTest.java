package io.ipoli.android.scheduling.distributions;

import org.junit.BeforeClass;
import org.junit.Test;

import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class DistributionTest {

    private static DiscreteDistribution dist;

    @BeforeClass
    public static void setUp() {
        dist = new DiscreteDistribution(new double[]{20, 20, 20, 20, 20});
    }

    @Test
    public void shouldNormalizeProbability() {
        double value = dist.at(0);
        assertThat(value, closeTo(0.2, 0.0001));
    }

    @Test
    public void shouldNormalizeJointDistribution() {
        DiscreteDistribution dist1 = new DiscreteDistribution(new double[]{10, 20, 30, 10, 10});
        DiscreteDistribution joint = dist.joint(dist1);
        assertThat(joint.at(0), closeTo(0.125, 0.000001));
    }
}
