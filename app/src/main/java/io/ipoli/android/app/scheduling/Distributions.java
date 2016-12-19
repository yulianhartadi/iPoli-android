package io.ipoli.android.app.scheduling;

import android.support.annotation.NonNull;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/18/16.
 */
public class Distributions {

    @NonNull
    public static DiscreteDistribution getSleepDistribution(int sleepStartMinute, int sleepEndMinute) {
        double[] values = new double[24 * 60];

        if (sleepEndMinute < sleepStartMinute) {
            for (int i = 0; i < values.length; i++) {
                if (i > sleepEndMinute && i < sleepStartMinute) {
                    values[i] = 1;
                } else {
                    values[i] = 0;
                }
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                if (i > sleepEndMinute || i < sleepStartMinute) {
                    values[i] = 1;
                } else {
                    values[i] = 0;
                }
            }
        }

        return new DiscreteDistribution(values);
    }

    public static DiscreteDistribution getWorkDistribution(int workStartMinute, int workEndMinute) {
        double[] values = new double[24 * 60];

        if (workStartMinute < workEndMinute) {
            for (int i = 0; i < values.length; i++) {
                if (i > workStartMinute && i < workEndMinute) {
                    values[i] = 1;
                } else {
                    values[i] = 0;
                }
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                if (i > workEndMinute || i < workStartMinute) {
                    values[i] = 1;
                } else {
                    values[i] = 0;
                }
            }
        }

        return new DiscreteDistribution(values);
    }
}
