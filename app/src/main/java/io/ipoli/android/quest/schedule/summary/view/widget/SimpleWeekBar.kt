package io.ipoli.android.quest.schedule.summary.view.widget

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.WeekBar
import io.ipoli.android.R

@Suppress("unused")
class SimpleWeekBar(context: Context) : WeekBar(context) {

    private var preselectedIndex = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.view_month_preview_week_bar, this, true)
    }

    override fun onDateSelected(calendar: Calendar, weekStart: Int, isClick: Boolean) {
        getChildAt(preselectedIndex).isSelected = false
        val viewIndex = getViewIndexByCalendar(calendar, weekStart)
        getChildAt(viewIndex).isSelected = true
        preselectedIndex = viewIndex
    }

    override fun onWeekStartChange(weekStart: Int) {
        for (i in 0 until childCount) {
            (getChildAt(i) as TextView).text = getWeekString(i, weekStart)
        }
    }

    private fun getWeekString(index: Int, weekStart: Int): String {
        val weeks = context.resources.getStringArray(R.array.week_bar_days_of_week)

        if (weekStart == 1) {
            return weeks[index]
        }
        return if (weekStart == 2) {
            weeks[if (index == 6) 0 else index + 1]
        } else weeks[if (index == 0) 6 else index - 1]
    }

}