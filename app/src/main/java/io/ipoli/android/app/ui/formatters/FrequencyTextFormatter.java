package io.ipoli.android.app.ui.formatters;

import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.data.Recurrence;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/15/16.
 */
public class FrequencyTextFormatter {
    public static String formatReadable(Recurrence recurrence) {
        if (recurrence == null) {
            return "Does not repeat";
        }
        return StringUtils.capitalize(recurrence.getRecurrenceType().name());
    }

    public static String formatInterval(int frequency, Recurrence recurrence) {
        String times = frequency == 1 ? "time" : "times";
        if (recurrence.getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY) {
            return frequency + " " + times + " a month";
        }
        return frequency + " " + times + " a week";
    }
}
