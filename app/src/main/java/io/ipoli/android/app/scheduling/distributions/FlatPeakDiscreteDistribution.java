package io.ipoli.android.app.scheduling.distributions;

import java.util.Arrays;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */
public class FlatPeakDiscreteDistribution extends DiscreteDistribution {

    public static final double DEFAULT_FILL_VALUE = 1.0;

    public static FlatPeakDiscreteDistribution create(int peakStart, int peakWidth, int peakHeight, int slopeWidth, int size) {
        peakWidth = isOdd(peakWidth) ? peakWidth - 1 : peakWidth;

        double[] values = new double[size];
        Arrays.fill(values, DEFAULT_FILL_VALUE);
        int peakEnd = peakStart + peakWidth - 1;
        for (int i = peakStart; i <= peakEnd; i++) {
            values[i] = peakHeight;
        }
        int heightDelta = peakHeight / (slopeWidth + 1);
        int slopeIndex = 0;
        for (int i = peakStart - 1; i >= peakStart - slopeWidth; i--) {
            slopeIndex++;
            if(i < 0) {
                continue;
            }
            values[i] = slopeValue(peakHeight, heightDelta, slopeIndex);
        }

        slopeIndex = 0;
        for(int i = peakEnd + 1; i <= peakEnd + slopeWidth; i++) {
            slopeIndex++;
            if(i > size - 1) {
                continue;
            }
            values[i] = slopeValue(peakHeight, heightDelta, slopeIndex);
        }
        return new FlatPeakDiscreteDistribution(values);
    }

    private static int slopeValue(int peakHeight, int heightDelta, int slopeIndex) {
        return (int) Math.max(peakHeight - slopeIndex * heightDelta, DEFAULT_FILL_VALUE);
    }

    private FlatPeakDiscreteDistribution(double[] values) {
        super(values);
    }

    private static boolean isOdd(int num) {
        return ((num % 2) != 0);
    }
}
