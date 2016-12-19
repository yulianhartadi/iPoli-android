package io.ipoli.android.app.scheduling;

import android.support.annotation.NonNull;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Random;

import io.ipoli.android.avatar.Avatar;
import io.ipoli.android.avatar.TimeOfDay;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/18/16.
 */
public class PosteriorEstimator {

    public static final int MORNING_6 = 6 * 60;
    public static final int MORNING_11 = 11 * 60;
    public static final int AFTERNOON_13 = 13 * 60;
    public static final int AFTERNOON_17 = 17 * 60;
    public static final int EVENING_19 = 19 * 60;
    public static final int EVENING_23 = 23 * 60;

    private final Avatar avatar;
    private final LocalDate currentDate;
    private final Random random;

    public PosteriorEstimator(Avatar avatar, LocalDate currentDate, Random random) {
        this.avatar = avatar;
        this.currentDate = currentDate;
        this.random = random;
    }

    @NonNull
    private DiscreteDistribution getSleepDistribution() {
        double[] values = createEmptyWholeDayValues();

        int sleepStartMinute = avatar.getSleepStartMinute();
        int sleepEndMinute = avatar.getSleepEndMinute();

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

        return new DiscreteDistribution(values, random);
    }

    @NonNull
    private DiscreteDistribution getWorkDistribution(int workStartMinute, int workEndMinute) {
        double[] values = createEmptyWholeDayValues();

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

        return new DiscreteDistribution(values, random);
    }

    private static double[] createEmptyWholeDayValues() {
        return new double[24 * 60];
    }

    public DiscreteDistribution posteriorFor(Quest quest) {

        DiscreteDistribution posterior = getSleepDistribution();

        DiscreteDistribution workDistribution = getWorkDistribution(avatar.getWorkStartMinute(), avatar.getWorkEndMinute());
        DiscreteDistribution inverseWorkDistribution = inverseUniformDistribution(workDistribution);

        Category category = Quest.getCategory(quest);

        if (category == Category.WORK && isWorkDay(currentDate, avatar)) {
            // schedule work tasks only during work days & hours
            posterior = workDistribution;
        }

        List<TimeOfDay> productiveTimesOfDay = avatar.getMostProductiveTimesOfDayList();
        if (!productiveTimesOfDay.contains(TimeOfDay.ANY_TIME) && (category == Category.WORK || category == Category.LEARNING)) {
            if (productiveTimesOfDay.contains(TimeOfDay.MORNING)) {
                posterior = posterior.joint(createMorningProductiveDistribution());
            }
            if (productiveTimesOfDay.contains(TimeOfDay.AFTERNOON)) {
                posterior = posterior.joint(createAfternoonProductiveDistribution());
            }
            if (productiveTimesOfDay.contains(TimeOfDay.EVENING)) {
                posterior = posterior.joint(createEveningProductiveDistribution());
            }
        }

        if (category == Category.FUN) {
            posterior = posterior.joint(createFunDistribution());
        }

        if ((category == Category.LEARNING || category == Category.WELLNESS || category == Category.PERSONAL)
                && isWorkDay(currentDate, avatar)) {
            posterior = posterior.joint(inverseWorkDistribution);
        }

        return posterior;
    }

    private boolean isWorkDay(LocalDate currentDate, Avatar avatar) {
        return avatar.getWorkDays().contains(currentDate.getDayOfWeek());
    }

    private DiscreteDistribution inverseUniformDistribution(DiscreteDistribution distribution) {
        double[] values = createEmptyWholeDayValues();
        for (int i = 0; i < values.length; i++) {
            if (distribution.at(i) > 0) {
                values[i] = 0;
            } else {
                values[i] = 1;
            }
        }
        return new DiscreteDistribution(values, random);
    }

    private DiscreteDistribution createFunDistribution() {
        double[] values = createEmptyWholeDayValues();
        for (int i = 0; i < values.length; i++) {
            if (i > EVENING_19 && i < EVENING_23) {
                values[i] = 1;
            } else {
                values[i] = 0;
            }
        }
        return new DiscreteDistribution(values, random);
    }

    private DiscreteDistribution createEveningProductiveDistribution() {
        double[] values = createEmptyWholeDayValues();
        for (int i = 0; i < values.length; i++) {
            if (i > EVENING_19 && i < EVENING_23) {
                values[i] = 1;
            } else {
                values[i] = 0;
            }
        }
        return new DiscreteDistribution(values, random);
    }

    private DiscreteDistribution createAfternoonProductiveDistribution() {
        double[] values = createEmptyWholeDayValues();
        for (int i = 0; i < values.length; i++) {
            if (i > AFTERNOON_13 && i < AFTERNOON_17) {
                values[i] = 1;
            } else {
                values[i] = 0;
            }
        }
        return new DiscreteDistribution(values, random);
    }

    private DiscreteDistribution createMorningProductiveDistribution() {
        double[] values = createEmptyWholeDayValues();
        for (int i = 0; i < values.length; i++) {
            if (i > MORNING_6 && i < MORNING_11) {
                values[i] = 1;
            } else {
                values[i] = 0;
            }
        }
        return new DiscreteDistribution(values, random);
    }
}
