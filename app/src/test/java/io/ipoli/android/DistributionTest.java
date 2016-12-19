package io.ipoli.android;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.ipoli.android.app.scheduling.DiscreteDistribution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class DistributionTest {

    private static final long SEED = 42;

    private static DiscreteDistribution dist;
    private static Random random;

    @BeforeClass
    public static void setUp() {
        random = new Random(SEED);
        dist = new DiscreteDistribution(new double[]{20, 20, 20, 20, 20}, random);
    }

    @Test
    public void shouldSampleFromDiscrete() {
        int value = dist.sample();
        assertThat(value, is(2));
    }

    @Test
    public void shouldBeUniformlyDistributed() {
        Map<Integer, Integer> samples = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            int value = dist.sample();
            samples.put(value, samples.getOrDefault(value, 0) + 1);
        }
        for (int i = 0; i < 5; i++) {
            assertThat(samples.get(i), greaterThan(150));
        }
    }

    @Test
    public void shouldNormalizeProbability() {
        double value = dist.at(0);
        assertThat(value, closeTo(0.2, 0.0001));
    }

    @Test
    public void shouldNormalizeJointDistribution() {
        DiscreteDistribution dist1 = new DiscreteDistribution(new double[]{10, 20, 30, 10, 10}, random);
        DiscreteDistribution joint = dist.joint(dist1);
        assertThat(joint.at(0), closeTo(0.125, 0.000001));
    }
}
