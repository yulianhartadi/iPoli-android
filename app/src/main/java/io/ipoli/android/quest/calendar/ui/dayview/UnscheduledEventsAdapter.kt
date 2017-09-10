package io.ipoli.android.quest.calendar.ui.dayview

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/10/17.
 */

interface UnscheduledEvent {

}

abstract class UnscheduledEventsAdapter<E : UnscheduledEvent>(@LayoutRes private val unscheduledEventLayout: Int,
                                                              private val items: List<E>,
                                                              private val calendarDayView: CalendarDayView)
    : RecyclerView.Adapter<UnscheduledEventsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(unscheduledEventLayout, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], calendarDayView)
    }

    val events: List<E>
        get() = items

    abstract fun ViewHolder.bind(event: E, calendarDayView: CalendarDayView)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}