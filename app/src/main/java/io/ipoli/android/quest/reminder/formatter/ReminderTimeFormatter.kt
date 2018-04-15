package io.ipoli.android.quest.reminder.formatter

import android.content.Context
import io.ipoli.android.Constants
import io.ipoli.android.R
import java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/5/17.
 */
object ReminderTimeFormatter {

    fun format(minutes: Int, context: Context): String {
        if (minutes < 0) {
            return ""
        }

        if (minutes == 0) {
            return context.getString(R.string.reminder_at_start)
        }

        val hours = TimeUnit.MINUTES.toHours(minutes.toLong())
        val mins = minutes - hours * 60

        if (hours > 0 && mins > 0) {
            return String.format(context.getString(R.string.reminder_time_format_full), hours, mins)
        }

        if (hours > 0 && mins == 0L) {
            return if (hours == 1L)
                context.getString(R.string.reminder_time_format_1_hour)
            else
                String.format(context.getString(R.string.reminder_time_format_hours), hours)
        }

        return if (mins == 1L)
            context.getString(R.string.reminder_time_format_1_minute)
        else
            String.format(context.getString(R.string.reminder_time_format_minutes), mins)
    }

    fun predefinedTimes(context: Context): List<String> {
        val predefinedTimes = Constants.REMINDER_PREDEFINED_MINUTES.map {
            format(it, context)
        }
        return predefinedTimes + context.getString(R.string.custom)
    }
}