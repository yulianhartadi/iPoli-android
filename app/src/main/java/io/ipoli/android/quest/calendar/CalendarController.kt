package io.ipoli.android.quest.calendar

import android.content.Context
import android.view.*
import com.bluelinelabs.conductor.Controller
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.quest.calendar.ui.CalendarAdapter
import io.ipoli.android.quest.calendar.ui.CalendarDayView
import io.ipoli.android.quest.calendar.ui.CalendarEvent
import kotlinx.android.synthetic.main.controller_calendar.view.*


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */
class CalendarController : Controller() {

    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.controller_calendar, container, false)
        view.calendarContainer.setAdapter(QuestCalendarAdapter(activity!!,
            listOf(
                QuestViewModel(60, Time.atHours(1).toMinuteOfDay()),
                QuestViewModel(30, Time.atHours(3).toMinuteOfDay())
            )
        ))
        return view
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


    data class QuestViewModel(override var duration: Int, override var startMinute: Int) : CalendarEvent {

    }

    inner class QuestCalendarAdapter(context: Context, events: List<QuestViewModel>) :
        CalendarAdapter<QuestViewModel>(context, R.layout.item_calendar_quest, events) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(context).inflate(resource, parent, false)!!
            }
            val questModel = getItem(position)

            view.setOnLongClickListener { v ->
                (parent as CalendarDayView).startEditMode(position)
                true
            }
            return view
        }

        override fun onStartEdit(position: Int) {
            startActionMode()
        }

        override fun onStopEdit(position: Int) {
            stopActionMode()
        }
    }
}