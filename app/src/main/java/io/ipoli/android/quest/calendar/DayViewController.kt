package io.ipoli.android.quest.calendar

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.TintableCompoundButton
import android.view.*
import com.bluelinelabs.conductor.Controller
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.Module
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.calendar.ui.dayview.*
import io.ipoli.android.quest.data.Category
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

    private val layoutInflater by required { layoutInflater }

    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.controller_day_view, container, false)
        view.calendar.setScheduledEventsAdapter(QuestScheduledEventsAdapter(activity!!,
            listOf(
                QuestViewModel("Play COD", 45, Time.atHours(1).toMinuteOfDay(), Category.FUN.color500, Category.FUN.color800),
                QuestViewModel("Study Bayesian Stats", 3 * 60, Time.atHours(3).toMinuteOfDay(), Category.LEARNING.color500, Category.LEARNING.color800)
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

    data class QuestViewModel(val name: String,
                              override var duration: Int,
                              override var startMinute: Int,
                              @ColorRes val backgroundColor: Int,
                              @ColorRes val textColor: Int) : CalendarEvent

    inner class QuestScheduledEventsAdapter(context: Context, events: List<QuestViewModel>, private val calendarDayView: CalendarDayView) :
        ScheduledEventsAdapter<QuestViewModel>(context, R.layout.item_calendar_quest, events) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(context).inflate(resource, parent, false)
            }

            val vm = getItem(position)

            view!!.setOnLongClickListener { v ->
                v.visibility = View.GONE
                calendarDayView.scheduleEvent(v)
//                calendarDayView.startEditMode(v, position)
                true
            }

            view.questName.text = vm.name
            view.questName.setTextColor(vm.textColor)

            view.questBackground.setBackgroundResource(vm.backgroundColor)
            view.questCategoryIndicator.setBackgroundResource(vm.backgroundColor)

            (view.checkBox as TintableCompoundButton).supportButtonTintList = getTintList(vm.backgroundColor)

            return view
        }

        private fun getTintList(@ColorRes color: Int) = ContextCompat.getColorStateList(context, color)

        override fun onStartEdit(editView: View) {
            startActionMode()
        }

        override fun onStopEdit(editView: View) {
            stopActionMode()
        }

        override fun onStartTimeChanged(editView: View, startTime: Time) {
//            editView.startTime.text = startTime.toString()
//            editView.startTime.text = startTime.toString()
//            editView.endTime.text = Time.plusMinutes(startTime, getItem(position).duration).toString()
        }
    }

    data class UnscheduledQuestViewModel(val name: String, val duration: Int) : UnscheduledEvent

    inner class UnscheduledQuestsAdapter(items: List<UnscheduledQuestViewModel>, calendarDayView: CalendarDayView) :
        UnscheduledEventsAdapter<UnscheduledQuestViewModel>
        (R.layout.unscheduled_quest_item, items, calendarDayView) {

        override fun ViewHolder.bind(event: UnscheduledQuestViewModel, calendarDayView: CalendarDayView) {
            itemView.name.text = event.name

//            calendarDayView.scheduleEvent(itemView)
            itemView.setOnLongClickListener {
                calendarDayView.scheduleEvent(itemView)
                true
            }
        }
    }
}