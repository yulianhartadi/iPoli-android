package io.ipoli.android.quest.calendar

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.TextViewCompat
import android.support.v4.widget.TintableCompoundButton
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import com.bluelinelabs.conductor.Controller
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.di.Module
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.calendar.ui.dayview.*
import io.ipoli.android.quest.data.Category
import kotlinx.android.synthetic.main.controller_day_view.view.*
import kotlinx.android.synthetic.main.item_calendar_drag.view.*
import kotlinx.android.synthetic.main.item_calendar_quest.view.*
import kotlinx.android.synthetic.main.unscheduled_quest_item.view.*
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/2/17.
 */
class DayViewController : Controller(), Injects<Module> {

    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.controller_day_view, container, false)
        view.calendar.setScheduledEventsAdapter(QuestScheduledEventsAdapter(activity!!,
            mutableListOf(
                QuestViewModel("Play COD", 15, Time.atHours(1).toMinuteOfDay(), "1:00", "1:15",
                    Category.FUN.color500, Category.FUN.color800, true),
                QuestViewModel("Study Bayesian Stats", 45, Time.atHours(3).toMinuteOfDay(), "3:00", "3:45",
                    Category.LEARNING.color500, Category.LEARNING.color700, false),
                QuestViewModel("Workout in the Gym with Vihar and his baba", 60, Time.atHours(7).toMinuteOfDay(), "7:00", "8:00",
                    Category.WELLNESS.color500, Category.WELLNESS.color700, false),
                QuestViewModel("Workout in the Gym with Vihar and his baba", 60, Time.atHours(22).toMinuteOfDay(), "7:00", "8:00",
                    Category.WELLNESS.color500, Category.WELLNESS.color700, false)
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
                              val startTime: String,
                              val endTime: String,
                              @ColorRes val backgroundColor: Int,
                              @ColorRes val textColor: Int,
                              var isCompleted: Boolean) : CalendarEvent

    inner class QuestScheduledEventsAdapter(context: Context, events: MutableList<QuestViewModel>, private val calendarDayView: CalendarDayView) :
        ScheduledEventsAdapter<QuestViewModel>(context, R.layout.item_calendar_quest, events) {

        override fun bindView(view: View, position: Int) {
            val vm = getItem(position)

            view.setOnLongClickListener { v ->

                //                v.visibility = View.GONE
                calendarDayView.scheduleEvent(v, position)
//                calendarDayView.startEditMode(v, position)
                false
            }

            view.startTime.text = vm.startTime
            view.endTime.text = vm.endTime

            if (!vm.isCompleted) {
                view.questName.text = vm.name
                view.questName.setTextColor(ContextCompat.getColor(context, vm.textColor))
                view.questBackground.setBackgroundResource(vm.backgroundColor)
                view.questCategoryIndicator.setBackgroundResource(vm.backgroundColor)
            } else {
                val span = SpannableString(vm.name)
                span.setSpan(StrikethroughSpan(), 0, vm.name.length, 0)
                view.questName.text = span
                view.questBackground.setBackgroundResource(R.color.md_grey_500)
                view.questCategoryIndicator.setBackgroundResource(R.color.md_grey_500)
                view.checkBox.isChecked = true
            }

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(view.questName, 8, 16, 1, TypedValue.COMPLEX_UNIT_SP)

            (view.checkBox as TintableCompoundButton).supportButtonTintList = tintList(vm.backgroundColor)

            view.post {
                adaptViewForHeight(view, ViewUtils.pxToDp(view.height, context))
            }
        }

        override fun onEventZoomed(adapterView: View) {
            adaptViewForHeight(adapterView, ViewUtils.pxToDp(adapterView.height, context))
        }

        private fun adaptViewForHeight(adapterView: View, height: Float) {
            with(adapterView) {
                when {
                    height < 28 -> ViewUtils.hideViews(checkBox, indicatorContainer, startTime, endTime)
                    height < 80 -> {
                        ViewUtils.showViews(checkBox, indicatorContainer)
                        ViewUtils.hideViews(startTime, endTime)
                        if (indicatorContainer.orientation == LinearLayout.VERTICAL) {
                            indicatorContainer.orientation = LinearLayout.HORIZONTAL
                            reverseIndicators(indicatorContainer)
                        }
                    }
                    else -> {
                        ViewUtils.showViews(checkBox, indicatorContainer, startTime, endTime)
                        if (indicatorContainer.orientation == LinearLayout.HORIZONTAL) {
                            indicatorContainer.orientation = LinearLayout.VERTICAL
                            reverseIndicators(indicatorContainer)
                        }
                    }
                }
            }
        }

        private fun reverseIndicators(indicatorContainer: ViewGroup) {
            val indicators = (0 until indicatorContainer.childCount)
                .map { indicatorContainer.getChildAt(it) }.reversed()

            indicatorContainer.removeAllViews()

            indicators.forEach {
                indicatorContainer.addView(it)
            }
        }

        private fun tintList(@ColorRes color: Int) = ContextCompat.getColorStateList(context, color)

        override fun onStartEdit(dragView: View, startTime: Time, endTime: Time) {
            startActionMode()
            dragView.dragStartTime.text = startTime.toString()
            dragView.dragEndTime.text = endTime.toString()
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(dragView.dragStartTime, 8, 14, 1, TypedValue.COMPLEX_UNIT_SP)
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(dragView.dragEndTime, 8, 14, 1, TypedValue.COMPLEX_UNIT_SP)
        }

        override fun onStopEdit(position: Int, startTime: Time, duration: Int) {
            stopActionMode()
//            val vm = getItem(position)
//            events[position] = vm.copy(
//                startMinute = startTime.toMinuteOfDay(),
//                startTime = startTime.toString(),
//                duration = duration,
//                endTime = Time.plusMinutes(startTime, duration).toString()
//            )
//            calendarDayView.updateEvent(position, startTime, duration)
            
            events.add(QuestViewModel("Hi", duration, startTime.toMinuteOfDay(), "", "", Category.WELLNESS.color500, Category.WELLNESS.color700, false))
            notifyDataSetChanged()
        }

        override fun onScheduledTimeChanged(dragView: View, startTime: Time, endTime: Time) {
            dragView.dragStartTime.text = startTime.toString()
            dragView.dragEndTime.text = endTime.toString()
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
                calendarDayView.scheduleEvent(itemView, adapterPosition)
                true
            }
        }
    }
}