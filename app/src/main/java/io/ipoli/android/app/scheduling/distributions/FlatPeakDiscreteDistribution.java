package io.ipoli.android.app.scheduling.distributions;

import java.util.Arrays;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/22/17.
 */
public class FlatPeakDiscreteDistribution extends DiscreteDistribution {

    public static final double DEFAULT_FILL_VALUE = 1.0;
    private static int size;
    private static double[] values;

    public static FlatPeakDiscreteDistribution create(int peakStart, int peakWidth, int peakHeight, int slopeWidth, int size) {
        return create(peakStart, peakWidth, peakHeight, slopeWidth, size, DEFAULT_FILL_VALUE);
    }

    public static FlatPeakDiscreteDistribution create(int peakStart, int peakWidth, int peakHeight, int slopeWidth, int size, double fillValue) {
        return new FlatPeakDiscreteDistribution(peakStart, peakWidth, peakHeight, slopeWidth, size, fillValue);
    }

    private static int getDistributionIndex(int index) {
        if (index < 0) {
            return size + index;
        }
        return index % size;
    }

    private static int slopeValue(int peakHeight, int heightDelta, int slopeIndex) {
        return (int) Math.max(peakHeight - getDistributionIndex(slopeIndex) * heightDelta, DEFAULT_FILL_VALUE);
    }

    private FlatPeakDiscreteDistribution(int peakStart, int peakWidth, int peakHeight, int slopeWidth, int size, double fillValue) {
        this(createValues(peakStart, peakWidth, peakHeight, slopeWidth, size, fillValue));
    }

    private static double[] createValues(int peakStart, int peakWidth, int peakHeight, int slopeWidth, int size, double fillValue) {
        FlatPeakDiscreteDistribution.size = size;
        FlatPeakDiscreteDistribution.values = new double[size];
        Arrays.fill(values, fillValue);
        int peakEnd = peakStart + peakWidth - 1;
        for (int i = peakStart; i <= peakEnd; i++) {
            setValue(i, peakHeight);
        }
        int heightDelta = peakHeight / (slopeWidth + 1);
        int slopeIndex = 0;
        for (int i = peakStart - 1; i >= peakStart - slopeWidth; i--) {
            slopeIndex++;
            int slopeValue = slopeValue(peakHeight, heightDelta, slopeIndex);

            setValue(i, slopeValue);
        }
        slopeIndex = 0;
        for (int i = peakEnd + 1; i <= peakEnd + slopeWidth; i++) {
            slopeIndex++;
            setValue(i, slopeValue(peakHeight, heightDelta, slopeIndex));
        }
        return values;
    }

    private static void setValue(int index, int value) {
        values[getDistributionIndex(index)] = value;
    }

    private FlatPeakDiscreteDistribution(double[] values) {
        super(values);
    }
}
