package io.ipoli.android.quest.calendar.ui.dayview

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import io.ipoli.android.common.datetime.Time

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
abstract class CalendarAdapter<T : CalendarEvent>(context: Context, protected val resource: Int, events: List<T>) :
    ArrayAdapter<T>(context, resource, events) {
    abstract fun onStartEdit(editView: View, position: Int)
    abstract fun onStopEdit(editView: View, position: Int)
    abstract fun onStartTimeChanged(editView: View, position: Int, startTime: Time)
}