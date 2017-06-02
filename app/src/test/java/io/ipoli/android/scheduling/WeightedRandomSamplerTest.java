package io.ipoli.android.scheduling;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.ipoli.android.app.scheduling.WeightedRandomSampler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/2/17.
 */
public class WeightedRandomSamplerTest {

    private static WeightedRandomSampler<Integer> sampler;

    @BeforeClass
    public static void setUp() {
        Random seed = new Random(42);
        sampler = new WeightedRandomSampler<>(seed);
    }

    @Test
    public void shouldSampleWithSmallWeights() {
        sampler.add(2, 0.0000000000000000000000000000000000000000000000002);
        sampler.add(1, 0.0000000000000000000000000000000000000000000000001);

        Map<Integer, Integer> counter = new HashMap<>();
        counter.put(2, 0);
        counter.put(1, 0);
        for (int i = 0; i < 1000; i++) {
            Integer res = sampler.sample();
            counter.put(res, counter.get(res) + 1);
        }
        assertThat(counter.get(2), is(greaterThan(600)));
    }
}
