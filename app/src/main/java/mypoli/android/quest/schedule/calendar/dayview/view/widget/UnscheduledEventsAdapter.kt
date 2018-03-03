package mypoli.android.quest.schedule.calendar.dayview.view.widget

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/10/17.
 */

interface UnscheduledEvent {

    val id: String

    val duration: Int
}

abstract class UnscheduledEventsAdapter<E : UnscheduledEvent>(
    @LayoutRes private val unscheduledEventLayout: Int,
    private val items: MutableList<E>,
    private val calendarDayView: CalendarDayView
) : RecyclerView.Adapter<UnscheduledEventsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                unscheduledEventLayout,
                parent,
                false
            )
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], calendarDayView)
    }

    val events: List<E>
        get() = items

    abstract fun ViewHolder.bind(viewModel: E, calendarDayView: CalendarDayView)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    fun addEvent(event: E) {
        items.add(event)
        notifyItemInserted(items.size - 1)
    }

    fun removeEvent(position: Int): E {
        val event = items.removeAt(position)
        notifyItemRemoved(position)
        return event
    }
}