package io.ipoli.android.common.text

import android.content.Context
import io.ipoli.android.R
import io.ipoli.android.quest.Quest

object QuestStartTimeFormatter {
    fun formatWithDuration(quest: Quest, context: Context, use24HourFormat : Boolean): String {
        val start = quest.startTime
                ?: return context.getString(
                    R.string.for_time,
                    DurationFormatter.formatShort(quest.actualDuration.asMinutes.intValue)
                )
        val end = start.plus(quest.actualDuration.asMinutes.intValue)
        return "${start.toString(use24HourFormat)} - ${end.toString(use24HourFormat)}"
    }

    fun format(quest: Quest, use24HourFormat : Boolean): String =
        quest.startTime?.toString(use24HourFormat) ?: ""
}