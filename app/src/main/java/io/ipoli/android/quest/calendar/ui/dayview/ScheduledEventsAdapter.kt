package io.ipoli.android.quest.calendar.ui.dayview

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import io.ipoli.android.common.datetime.Time

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
abstract class ScheduledEventsAdapter<T : CalendarEvent>(context: Context, protected val resource: Int, events: List<T>) :
    ArrayAdapter<T>(context, resource, events) {
    abstract fun onStartEdit(editView: View)
    abstract fun onStopEdit(editView: View)
    abstract fun onStartTimeChanged(editView: View, startTime: Time)
}