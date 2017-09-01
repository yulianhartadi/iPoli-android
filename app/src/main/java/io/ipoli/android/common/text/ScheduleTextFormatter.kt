package io.ipoli.android.common.text

import android.content.Context
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.quest.overview.ui.OverviewQuestViewModel


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/21/17.
 */
class ScheduleTextFormatter(private val use24HourFormat: Boolean) {

    fun format(model: OverviewQuestViewModel, context: Context): String {
        val duration = model.duration
        val startTime = model.startTime
        if (duration > 0 && startTime != null) {
            val endTime = Time.plusMinutes(startTime, duration)
            return startTime.toString(use24HourFormat) + " - " + endTime.toString(use24HourFormat)
        } else if (duration > 0) {
            return String.format(context.getString(R.string.quest_for_time), DurationFormatter.format(context, duration))
        } else if (startTime != null) {
            return String.format(context.getString(R.string.quest_at_time), startTime.toString(use24HourFormat))
        }
        return ""
    }
}