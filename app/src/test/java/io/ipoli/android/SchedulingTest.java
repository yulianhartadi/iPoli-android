package io.ipoli.android;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

import io.ipoli.android.app.scheduling.DiscreteDistribution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/16/16.
 */

public class SchedulingTest {

    private static DiscreteDistribution dist;
    private static Random random;

    @BeforeClass
    public static void setUp() {
        random = new Random(42);
        dist = new DiscreteDistribution(5, 10, new int[]{1, 5, 20, 44, 55, 13}, random);
    }

    @Test
    public void canSampleFromDistribution() {
        int value = dist.sample();
        assertThat(value, is(9));
    }

}
