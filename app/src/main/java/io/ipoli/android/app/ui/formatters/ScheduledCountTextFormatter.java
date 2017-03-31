package io.ipoli.android.app.ui.formatters;

import io.ipoli.android.quest.data.Recurrence;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/29/16.
 */
public class ScheduledCountTextFormatter {
    public static String format(int scheduledCount, Recurrence recurrence) {
        String times = scheduledCount == 1 ? "time" : "times";
        if (recurrence.getRecurrenceType() == Recurrence.RepeatType.MONTHLY) {
            return scheduledCount + " " + times + " this month";
        }
        return scheduledCount + " " + times + " this week";
    }
}
