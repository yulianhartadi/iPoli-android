package io.ipoli.android.app.scheduling.distributions;

import java.util.Arrays;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */
public class FlatPeakDiscreteDistribution extends DiscreteDistribution {

    public static FlatPeakDiscreteDistribution create(int peakIndex, int peakWidth, int peakHeight, int slopeWidth, int size) {
        peakWidth = isOdd(peakWidth) ? peakWidth - 1 : peakWidth;

        double[] values = new double[size];
        Arrays.fill(values, 1.0);
        int peakStart = peakIndex - peakWidth / 2;
        int peakEnd = peakStart + peakWidth;
        for (int i = peakStart; i <= peakEnd; i++) {
            values[i] = peakHeight;
        }
        int heightDelta = peakHeight / slopeWidth;
        int slopeIndex = 0;
        for (int i = peakStart - 1; i >= peakStart - slopeWidth; i--) {
            slopeIndex++;
            values[i] = peakHeight - slopeIndex * heightDelta;
        }

        slopeIndex = 0;
        for(int i = peakEnd + 1; i <= peakEnd + slopeWidth; i++) {
            slopeIndex++;
            values[i] = peakHeight - slopeIndex * heightDelta;
        }
        return new FlatPeakDiscreteDistribution(values);
    }

    private FlatPeakDiscreteDistribution(double[] values) {
        super(values);
    }

    private static boolean isOdd(int num) {
        return ((num % 2) != 0);
    }
}
