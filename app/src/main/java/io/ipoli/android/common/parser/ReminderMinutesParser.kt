package io.ipoli.android.common.parser

import io.ipoli.android.quest.reminder.picker.TimeUnit

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/4/17.
 */
object ReminderMinutesParser {

    fun parseCustomMinutes(minutes: Long): Pair<Long, TimeUnit> {
        val timeUnit = TimeUnit.values().reversed()
            .firstOrNull { minutes % it.minutes == 0L }

        return if (timeUnit == null) {
            Pair(minutes, TimeUnit.MINUTES)
        } else {
            Pair(minutes / timeUnit.minutes, timeUnit)
        }
    }
}