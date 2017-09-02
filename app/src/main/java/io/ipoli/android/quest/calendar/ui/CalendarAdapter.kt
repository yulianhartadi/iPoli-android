package io.ipoli.android.quest.calendar.ui

import android.content.Context
import android.widget.ArrayAdapter

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
abstract class CalendarAdapter<T : CalendarEvent>(context: Context, resource: Int, events: List<T>) :
    ArrayAdapter<T>(context, resource, events) {
    protected val resource = resource
    abstract fun onStartEdit(position: Int)
    abstract fun onStopEdit(position: Int)
}