package io.ipoli.android.scheduling.distributions;

import java.util.Collections;
import java.util.List;

import io.ipoli.android.app.scheduling.distributions.DiscreteDistribution;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/24/17.
 */
public class DistributionTestUtil {

    public static int getIndexCountWithMaxProbability(DiscreteDistribution dist) {
        List<Double> freq = dist.toList();
        Double max = Collections.max(freq);
        int idxCount = 0;
        for (int i = 0; i < freq.size(); i++) {
            if (Double.compare(freq.get(i), max) == 0) {
                idxCount++;
            }
        }
        return idxCount;
    }
}
