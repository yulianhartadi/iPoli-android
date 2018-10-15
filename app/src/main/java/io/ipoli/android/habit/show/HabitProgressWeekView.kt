package io.ipoli.android.habit.show

import android.content.Context
import android.graphics.Canvas
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.WeekView

@Suppress("unused")
class HabitProgressWeekView(context: Context) : WeekView(context) {
    override fun onDrawText(
        canvas: Canvas?,
        calendar: Calendar?,
        x: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {

    }

    override fun onDrawSelected(
        canvas: Canvas?,
        calendar: Calendar?,
        x: Int,
        hasScheme: Boolean
    ): Boolean {
        return hasScheme
    }

    override fun onDrawScheme(canvas: Canvas?, calendar: Calendar?, x: Int) {
    }

}