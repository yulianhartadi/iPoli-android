package mypoli.android.quest.schedule.calendar.dayview.view.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import mypoli.android.common.datetime.Time

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/2/17.
 */
abstract class ScheduledEventsAdapter<T : CalendarEvent>(
    context: Context,
    protected val resource: Int,
    val events: MutableList<T>
) :
    ArrayAdapter<T>(context, resource, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(resource, parent, false)
        }
        bindView(view!!, position)
        return view
    }

    fun addEvent(event: T) {
        events.add(event)
        notifyDataSetChanged()
    }

    fun removeEvent(position: Int): T {
        val event = events.removeAt(position)
        notifyDataSetChanged()
        return event
    }

    abstract fun bindView(view: View, position: Int)
    abstract fun adaptViewForHeight(adapterView: View, height: Float)
    abstract fun rescheduleEvent(position: Int, startTime: Time, duration: Int)
}