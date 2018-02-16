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
        formatterWithYear(date.dayOfMonth)
            .format(date.toStartOfDay())

    fun dateWithoutYear(date: LocalDate): String =
        formatterWithoutYear(date.dayOfMonth)
            .format(date.toStartOfDay())

    private fun formatterWithYear(dayOfMonth: Int) =
        SimpleDateFormat(patternWithYear(dayOfMonth), Locale.getDefault())

    private fun formatterWithoutYear(dayOfMonth: Int) =
        SimpleDateFormat(patternWithoutYear(dayOfMonth), Locale.getDefault())

    private fun patternWithoutYear(day: Int): String {
        if (day !in 11..18)
            return when (day % 10) {
                1 -> "MMM d'st'"
                2 -> "MMM d'nd'"
                3 -> "MMM d'rd'"
                else -> "MMM d'th'"
            }
        return "MMM d'th'"
    }

    private fun patternWithYear(day: Int) =
        patternWithoutYear(day) + " yy"

    fun day(date: LocalDate): String {
        return when {
            date.isToday -> context.getString(R.string.today)
            date.isTomorrow -> context.getString(R.string.tomorrow)
            date.isYesterday -> context.getString(R.string.yesterday)
            else -> date.dayOfWeekText
        }
    }
}