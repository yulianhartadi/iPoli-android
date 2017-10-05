package io.ipoli.android.common.parser

import io.ipoli.android.reminder.ui.picker.TimeUnit

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/4/17.
 */
object ReminderMinutesParser {

    fun parseCustomMinutes(minutes: Long): Pair<Long, TimeUnit>? {
        if (minutes == 0L) {
            return Pair(minutes, TimeUnit.MINUTES)
        }

        return TimeUnit.values().reversed()
            .firstOrNull { minutes % it.minutes == 0L }
            ?.let { Pair(minutes / it.minutes, it) }
    }
}