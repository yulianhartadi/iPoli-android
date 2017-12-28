package mypoli.android.common.text

import android.content.Context
import mypoli.android.R
import mypoli.android.common.datetime.*
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/21/17.
 */
class CalendarFormatter(private val context: Context) {

    fun date(date: LocalDate): String =
        formatter(date.dayOfMonth)
            .format(date.toStartOfDay())

    private fun formatter(dayOfMonth: Int) =
        SimpleDateFormat(pattern(dayOfMonth), Locale.getDefault())

    private fun pattern(day: Int): String {
        if (day !in 11..18)
            return when (day % 10) {
                1 -> "MMM d'st' yy"
                2 -> "MMM d'nd' yy"
                3 -> "MMM d'rd' yy"
                else -> "MMM d'th' yy"
            }
        return "MMM d'th' yy"
    }

    fun day(date: LocalDate): String {
        return when {
            date.isToday -> context.getString(R.string.today)
            date.isTomorrow -> context.getString(R.string.tomorrow)
            date.isYesterday -> context.getString(R.string.yesterday)
            else -> date.dayOfWeekText
        }
    }
}