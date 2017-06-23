package io.ipoli.android.app.ui.formatters;

import android.content.Context;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/15/16.
 */
public class TimesADayFormatter {

    public static String formatReadableShort(Context context, int value) {
        if (value <= 0) {
            value = 1;
        }
        if (value == 1) {
            return context.getString(R.string.once);
        } else if (value == 2) {
            return context.getString(R.string.twice);
        }

        return String.format(context.getString(R.string.times_per_period_multiple), value);
    }

    public static String formatReadable(Context context, int value) {
        if (value <= 0) {
            value = 1;
        }
        if (value == 1) {
            return context.getString(R.string.once_a_day);
        } else if (value == 2) {
            return context.getString(R.string.twice_a_day);
        }

        return String.format(context.getString(R.string.times_a_day), value);
    }

}
