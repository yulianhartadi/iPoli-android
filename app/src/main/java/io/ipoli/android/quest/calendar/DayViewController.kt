package io.ipoli.android.quest.calendar

import android.content.Context
import android.view.*
import com.bluelinelabs.conductor.Controller
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.Module
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.calendar.ui.dayview.*
import kotlinx.android.synthetic.main.controller_day_view.view.*
import kotlinx.android.synthetic.main.item_calendar_quest.view.*
import kotlinx.android.synthetic.main.unscheduled_quest_item.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */
class DayViewController : Controller(), Injects<Module> {

    private val sharedPreferences by required { sharedPreferences }

    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.controller_day_view, container, false)
        view.calendar.setCalendarAdapter(QuestCalendarAdapter(activity!!,
            listOf(
                QuestViewModel(60, Time.atHours(0).toMinuteOfDay()),
                QuestViewModel(30, Time.atHours(3).toMinuteOfDay())
            ),
            view.calendar
        ))
        view.calendar.setUnscheduledQuestsAdapter(UnscheduledQuestsAdapter(listOf(
            UnscheduledQuestViewModel("name 1", 45),
            UnscheduledQuestViewModel("name 2", 90)
        ), view.calendar))
        return view
    }

    override fun onContextAvailable(context: Context) {
        inject(iPoliApp.module(context))
    }

    private fun startActionMode() {
        parentController?.view?.startActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                actionMode = mode
                mode.menuInflater.inflate(R.menu.calendar_quest_edit_menu, menu)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(p0: ActionMode?) {
                actionMode = null
            }
        })
    }

    private fun stopActionMode() {
        actionMode?.finish()
    }

    data class QuestViewModel(override var duration: Int, override var startMinute: Int) : CalendarEvent

    inner class QuestCalendarAdapter(context: Context, events: List<QuestViewModel>, private val calendarDayView: CalendarDayView) :
        CalendarAdapter<QuestViewModel>(context, R.layout.item_calendar_quest, events) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(context).inflate(resource, parent, false)!!
            }

            view.setOnLongClickListener { v ->
                calendarDayView.startEditMode(v, position)
                true
            }

            return view
        }

        override fun onStartEdit(editView: View, position: Int) {
            startActionMode()
        }

        override fun onStopEdit(editView: View, position: Int) {
            stopActionMode()
        }

        override fun onStartTimeChanged(editView: View, position: Int, startTime: Time) {
            editView.startTime.text = startTime.toString()
            editView.endTime.text = Time.plusMinutes(startTime, getItem(position).duration).toString()
        }
    }

    data class UnscheduledQuestViewModel(val name: String, val duration: Int) : UnscheduledEvent

    inner class UnscheduledQuestsAdapter(items: List<UnscheduledQuestViewModel>, calendarDayView: CalendarDayView) : UnscheduledEventsAdapter<UnscheduledQuestViewModel>
    (R.layout.unscheduled_quest_item, items, calendarDayView) {
        override fun ViewHolder.bind(event: UnscheduledQuestViewModel, calendarDayView: CalendarDayView) {
            itemView.name.text = event.name

            itemView.setOnLongClickListener {
                calendarDayView.scheduleEvent(event)
                true
            }
        }
    }
}