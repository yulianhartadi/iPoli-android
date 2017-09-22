package io.ipoli.android.quest.calendar.ui.dayview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
abstract class ScheduledEventsAdapter<T : CalendarEvent>(context: Context, protected val resource: Int, val events: MutableList<T>) :
    ArrayAdapter<T>(context, resource, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(resource, parent, false)
        }
        bindView(view!!, position)
        return view
    }

    abstract fun bindView(view: View, position: Int)
//    abstract fun onStartEdit(dragView: View, startTime: Time, endTime: Time)
//    abstract fun onStopEdit(position: Int, startTime: Time, duration: Int)
//    abstract fun onScheduledTimeChanged(dragView: View, startTime: Time, endTime: Time)
//    abstract fun onEventZoomed(adapterView: View)
}