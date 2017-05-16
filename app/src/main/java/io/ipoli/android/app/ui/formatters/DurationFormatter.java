package io.ipoli.android.app.ui.formatters;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/28/16.
 */
public class DurationFormatter {

    public static String format(Context context, int duration) {
        if (duration < 0) {
            return formatEmptyDuration(context);
        }
        long hours = 0;
        long mins = 0;
        if (duration > 0) {
            hours = TimeUnit.MINUTES.toHours(duration);
            mins = duration - hours * 60;
        }
        if (hours > 0 && mins == 0) {
            return context.getString(R.string.quest_item_hours_duration, hours);
        } else if (hours > 0) {
            return context.getString(R.string.quest_item_full_duration, hours, mins);
        } else {
            return context.getString(R.string.quest_item_minutes_duration, mins);
        }
    }

    public static String formatReadable(Context context, int duration) {
        if (duration < 0) {
            return formatEmptyDuration(context);
        }
        if (duration <= Constants.QUEST_MIN_DURATION) {
            return String.format(context.getString(R.string.duration_minutes_or_less), Constants.QUEST_MIN_DURATION);
        }
        long hours = TimeUnit.MINUTES.toHours(duration);
        long mins = duration - hours * 60;
        if (hours <= 0 && mins <= 0) {
            return "";
        }
        if (hours > 0 && mins > 0) {
            return String.format(context.getString(R.string.duration_format_full), hours, mins);
        }

        if (hours > 0 && mins == 0) {
            return context.getResources().getQuantityString(R.plurals.duration_hours, (int) hours, hours);
        }

        return context.getResources().getQuantityString(R.plurals.duration_minutes, (int) mins, mins);
    }

    public static String formatReadableShort(int duration) {
        if (duration < 0) {
            return "";
        }
        if (duration <= Constants.QUEST_MIN_DURATION) {
            return Constants.QUEST_MIN_DURATION + " min or less";
        }

        String separator = "and";
        long hours = TimeUnit.MINUTES.toHours(duration);
        long mins = duration - hours * 60;
        if (hours <= 0 && mins <= 0) {
            return "";
        }
        if (hours > 0 && mins > 0) {
            return hours + "h " + separator + " " + mins + "m";
        }

        if (hours > 0 && mins == 0) {
            return hours == 1 ? "1 hour" : hours + " hours";
        }

        return mins + " min";
    }

    public static String formatShort(int duration) {
        if (duration < 0) {
            return "";
        }
        String separator = "and";
        long hours = TimeUnit.MINUTES.toHours(duration);
        long mins = duration - hours * 60;
        if (hours <= 0 && mins <= 0) {
            return "";
        }
        if (hours > 0 && mins > 0) {
            return hours + "h " + separator + " " + mins + "m";
        }

        if (hours > 0 && mins == 0) {
            return hours == 1 ? "1 hour" : hours + " hours";
        }

        return mins + " min";
    }

    public static String formatShort(Context context, int duration) {
        if (duration < 0) {
            return "";
        }

        long hours = TimeUnit.MINUTES.toHours(duration);
        long mins = duration - hours * 60;
        if (hours <= 0 && mins <= 0) {
            return "";
        }
        if (hours > 0 && mins > 0) {
            return String.format(context.getString(R.string.duration_format_short), hours, mins);
        }

        if (hours > 0 && mins == 0) {
            return context.getResources().getQuantityString(R.plurals.duration_hours, (int) hours, hours);
        }

        return context.getResources().getQuantityString(R.plurals.duration_minutes, (int) mins, mins);
    }

    private static String formatEmptyDuration(Context context) {
        return context.getString(R.string.do_not_know);
    }
}
