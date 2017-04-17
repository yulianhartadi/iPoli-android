package io.ipoli.android.app.scheduling;

import android.support.annotation.NonNull;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/18/16.
 */
public class PosteriorEstimator {

    public static final int MORNING_START = 6 * 60;
    public static final int MORNING_END = 11 * 60;
    public static final int AFTERNOON_START = 13 * 60;
    public static final int AFTERNOON_END = 17 * 60;
    public static final int EVENING_START = 19 * 60;
    public static final int EVENING_END = 23 * 60;

    private final PosteriorSettings posteriorSettings;
    private final LocalDate currentDate;
    private final Random random;

    public PosteriorEstimator(PosteriorSettings posteriorSettings, LocalDate currentDate, Random random) {
        this.posteriorSettings = posteriorSettings;
        this.currentDate = currentDate;
        this.random = random;
    }

    @NonNull
    private DiscreteDistribution getSleepDistribution() {
        double[] values = createEmptyWholeDayValues();

        int sleepStartMinute = posteriorSettings.getSleepStartMinute();
        int sleepEndMinute = posteriorSettings.getSleepEndMinute();

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

        DiscreteDistribution workDistribution = getWorkDistribution(posteriorSettings.getWorkStartMinute(), posteriorSettings.getWorkEndMinute());
        DiscreteDistribution inverseWorkDistribution = inverseUniformDistribution(workDistribution);

        Category category = quest.getCategoryType();

        if (category == Category.WORK && isWorkDay(currentDate, posteriorSettings.getWorkDays())) {
            // schedule work tasks only during work days & hours
            posterior = workDistribution;
        }

        List<TimeOfDay> productiveTimesOfDay = posteriorSettings.getMostProductiveTimesOfDayList();
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

        if ((category == Category.LEARNING || category == Category.WELLNESS || category == Category.PERSONAL || category == Category.CHORES)
                && isWorkDay(currentDate, posteriorSettings.getWorkDays())) {
            posterior = posterior.joint(inverseWorkDistribution);
        }

        return posterior;
    }

    private boolean isWorkDay(LocalDate currentDate, List<Integer> workDays) {
        return workDays.contains(currentDate.getDayOfWeek().getValue());
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
            if (i > EVENING_START && i < EVENING_END) {
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
            if (i > EVENING_START && i < EVENING_END) {
                values[i] = 10;
            } else {
                values[i] = 1;
            }
        }
        return new DiscreteDistribution(values, random);
    }

    private DiscreteDistribution createAfternoonProductiveDistribution() {
        double[] values = createEmptyWholeDayValues();
        for (int i = 0; i < values.length; i++) {
            if (i > AFTERNOON_START && i < AFTERNOON_END) {
                values[i] = 10;
            } else {
                values[i] = 1;
            }
        }
        return new DiscreteDistribution(values, random);
    }

    private DiscreteDistribution createMorningProductiveDistribution() {
        double[] values = createEmptyWholeDayValues();
        for (int i = 0; i < values.length; i++) {
            if (i > MORNING_START && i < MORNING_END) {
                values[i] = 10;
            } else {
                values[i] = 1;
            }
        }
        return new DiscreteDistribution(values, random);
    }

    public static class PosteriorSettings {
        private List<String> mostProductiveTimesOfDay;
        private List<Integer> workDays;
        private Integer workStartMinute;
        private Integer workEndMinute;
        private Integer sleepStartMinute;
        private Integer sleepEndMinute;

        public static PosteriorSettings create() {
            return new PosteriorSettings();
        }

        public List<String> getMostProductiveTimesOfDay() {
            return mostProductiveTimesOfDay;
        }

        public PosteriorSettings setMostProductiveTimesOfDay(List<String> mostProductiveTimesOfDay) {
            this.mostProductiveTimesOfDay = mostProductiveTimesOfDay;
            return this;
        }

        public List<Integer> getWorkDays() {
            return workDays;
        }

        public PosteriorSettings setWorkDays(List<Integer> workDays) {
            this.workDays = workDays;
            return this;
        }

        public Integer getWorkStartMinute() {
            return workStartMinute;
        }

        public PosteriorSettings setWorkStartMinute(Integer workStartMinute) {
            this.workStartMinute = workStartMinute;
            return this;
        }

        public Integer getWorkEndMinute() {
            return workEndMinute;
        }

        public PosteriorSettings setWorkEndMinute(Integer workEndMinute) {
            this.workEndMinute = workEndMinute;
            return this;
        }

        public Integer getSleepStartMinute() {
            return sleepStartMinute;
        }

        public PosteriorSettings setSleepStartMinute(Integer sleepStartMinute) {
            this.sleepStartMinute = sleepStartMinute;
            return this;
        }

        public Integer getSleepEndMinute() {
            return sleepEndMinute;
        }

        public PosteriorSettings setSleepEndMinute(Integer sleepEndMinute) {
            this.sleepEndMinute = sleepEndMinute;
            return this;
        }

        public List<TimeOfDay> getMostProductiveTimesOfDayList() {
            List<TimeOfDay> timesOfDay = new ArrayList<>();
            if(mostProductiveTimesOfDay == null) {
                mostProductiveTimesOfDay = new ArrayList<>();
                return timesOfDay;
            }
            for(String timeOfDay : mostProductiveTimesOfDay) {
                timesOfDay.add(TimeOfDay.valueOf(timeOfDay));
            }
            return timesOfDay;
        }
    }
}
