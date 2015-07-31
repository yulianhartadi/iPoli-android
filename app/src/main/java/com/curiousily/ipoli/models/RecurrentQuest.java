package com.curiousily.ipoli.models;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/31/15.
 */
public class RecurrentQuest extends Quest {

    public final WeeklyRecurrence weeklyRecurrence;

    public RecurrentQuest(String name, String time, int duration, Context context) {
        super(name, time, duration, context);
        this.weeklyRecurrence = new WeeklyRecurrence(2, 3);
    }

    public static class WeeklyRecurrence {
        public final int timesCompleted;
        public final int totalTimes;

        public WeeklyRecurrence(int timesCompleted, int totalTimes) {
            this.timesCompleted = timesCompleted;
            this.totalTimes = totalTimes;
        }
    }
}
