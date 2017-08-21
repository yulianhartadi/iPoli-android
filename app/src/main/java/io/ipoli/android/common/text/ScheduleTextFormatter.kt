package io.ipoli.android.common.text


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/21/17.
 */
class ScheduleTextFormatter(private val use24HourFormat: Boolean) {

//    fun format(quest: Quest): String {
//        val duration = quest.getDuration()
//        val startTime = quest.getStartTime()
//        if (duration > 0 && startTime != null) {
//            val endTime = Time.plusMinutes(startTime, duration)
//            return startTime!!.toString(use24HourFormat) + " - " + endTime.toString(use24HourFormat)
//        } else if (duration > 0) {
//            return String.format(context.getString(R.string.quest_for_time), DurationFormatter.format(context, duration))
//        } else if (startTime != null) {
//            return String.format(context.getString(R.string.quest_at_time), startTime!!.toString(use24HourFormat))
//        }
//        return ""
//    }
}