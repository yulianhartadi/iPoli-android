package io.ipoli.android.quest.calendar.dayview.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.widget.TextViewCompat
import android.support.v4.widget.TintableCompoundButton
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.isNotEqual
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.*
import io.ipoli.android.quest.calendar.CalendarViewController
import io.ipoli.android.quest.calendar.dayview.DayViewPresenter
import io.ipoli.android.quest.calendar.dayview.view.DayViewState.StateType.*
import io.ipoli.android.quest.calendar.dayview.view.widget.*
import io.ipoli.android.reminder.view.picker.ReminderPickerDialogController
import io.ipoli.android.reminder.view.picker.ReminderViewModel
import kotlinx.android.synthetic.main.calendar_hour_cell.view.*
import kotlinx.android.synthetic.main.controller_day_view.view.*
import kotlinx.android.synthetic.main.item_calendar_drag.view.*
import kotlinx.android.synthetic.main.item_calendar_quest.view.*
import kotlinx.android.synthetic.main.unscheduled_quest_item.view.*
import kotlinx.android.synthetic.main.view_calendar_day.view.*
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required
import timber.log.Timber

class DayViewController :
    MviViewController<DayViewState, DayViewController, DayViewPresenter, DayViewIntent>,
    Injects<ControllerModule>,
    CalendarDayView.CalendarChangeListener,
    ViewStateRenderer<DayViewState> {

    private lateinit var currentDate: LocalDate

    private var actionMode: ActionMode? = null

    private lateinit var eventsAdapter: QuestScheduledEventsAdapter

    private lateinit var unscheduledEventsAdapter: UnscheduledQuestsAdapter

    constructor(currentDate: LocalDate) : this() {
        this.currentDate = currentDate
    }

    constructor(args: Bundle? = null) : super(args)

    private val presenter by required { dayViewPresenter }

    private lateinit var calendarDayView: CalendarDayView

    override fun onSaveViewState(view: View, outState: Bundle) {
        outState.putLong("current_date", currentDate.startOfDayUTC())
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        currentDate = DateUtils.fromMillis(savedViewState.getLong("current_date"))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {

        val view = inflater.inflate(R.layout.controller_day_view, container, false)

        calendarDayView = view.calendar
        calendarDayView.setCalendarChangeListener(this)
        calendarDayView.setHourAdapter(object : CalendarDayView.HourCellAdapter {
            override fun bind(view: View, hour: Int) {
                if (hour > 0) {
                    view.timeLabel.text = hour.toString() + ":00"
                }
            }
        })

        calendarDayView.scrollToNow()

        repeatTruc = Runnable {
            calendarDayView.setScheduledEventsAdapter(eventsAdapter)
            calendarDayView.setUnscheduledEventsAdapter(unscheduledEventsAdapter)
            view?.postDelayed(repeatTruc, 3000)
        }

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadDataIntent(currentDate))
        if (currentDate.isNotEqual(LocalDate.now())) {
            calendarDayView.hideTimeline()
        }
    }

    override fun handleBack(): Boolean {
        cancelEdit()
        return super.handleBack()
    }

    private fun cancelEdit() {
        ViewUtils.hideKeyboard(calendarDayView)
        calendarDayView.cancelEdit()
    }

    override fun createPresenter() = presenter

    var repeatTruc: Runnable? = null


    override fun render(state: DayViewState, view: View) {

        when (state.type) {
            SCHEDULE_LOADED -> {
                eventsAdapter = QuestScheduledEventsAdapter(activity!!, state.scheduledQuests, calendarDayView)
                calendarDayView.setScheduledEventsAdapter(eventsAdapter)
                unscheduledEventsAdapter = UnscheduledQuestsAdapter(state.unscheduledQuests, calendarDayView)
                calendarDayView.setUnscheduledEventsAdapter(unscheduledEventsAdapter)
                updateUnscheduledQuestsHeight(view)

                view.postDelayed(repeatTruc, 3000)
            }

            ADD_NEW_SCHEDULED_QUEST -> {
                startActionMode(null)
                (parentController as CalendarViewController).onStartEdit()
                val dragView = view.dragContainer
                dragView.dragStartTime.visibility = View.VISIBLE
                dragView.dragEndTime.visibility = View.VISIBLE
                startEditScheduledEvent(dragView, state.startTime!!, state.endTime!!)
                setupDragViewNameAndColor(dragView, state.name, state.color!!)
            }

            START_EDIT_SCHEDULED_QUEST -> {
                startActionMode(state.icon)
                (parentController as CalendarViewController).onStartEdit()
                val dragView = view.dragContainer
                dragView.dragStartTime.visibility = View.VISIBLE
                dragView.dragEndTime.visibility = View.VISIBLE
                startEditScheduledEvent(dragView, state.startTime!!, state.endTime!!)
                setupDragViewNameAndColor(dragView, state.name, state.color!!, state.reminder)
            }

            START_EDIT_UNSCHEDULED_QUEST -> {
                startActionMode(state.icon)
                (parentController as CalendarViewController).onStartEdit()
                val dragView = view.dragContainer
                dragView.dragStartTime.visibility = View.GONE
                dragView.dragEndTime.visibility = View.GONE
                setupDragViewNameAndColor(dragView, state.name, state.color!!, state.reminder)
            }

            EVENT_UPDATED -> {
                calendarDayView.onEventUpdated()
            }

            EVENT_VALIDATION_ERROR -> {
                calendarDayView.onEventValidationError()
            }

            EVENT_REMOVED -> {
                PetMessageViewController(object : PetMessageViewController.UndoClickedListener {
                    override fun onClick() {
                        sendUndoRemovedEventIntent(state.removedEventId)
                    }
                }).show(router)
            }

            QUEST_COMPLETED -> {
            }

            UNDO_QUEST_COMPLETED -> {
            }

            EDIT_QUEST -> {
                startActionMode(state.icon)
            }

        }
    }

    private fun updateUnscheduledQuestsHeight(view: View) {
        val unscheduledQuestsToShow = Math.min(unscheduledEventsAdapter.itemCount, Constants.MAX_UNSCHEDULED_QUEST_VISIBLE_COUNT)

        val itemHeight = resources!!.getDimensionPixelSize(R.dimen.unscheduled_quest_item_height)

        val layoutParams = view.unscheduledEvents.layoutParams
        layoutParams.height = unscheduledQuestsToShow * itemHeight
        if (unscheduledQuestsToShow == Constants.MAX_UNSCHEDULED_QUEST_VISIBLE_COUNT) {
            layoutParams.height = layoutParams.height - itemHeight / 2
        }
        view.unscheduledEvents.layoutParams = layoutParams
    }

    private fun sendUndoRemovedEventIntent(eventId: String) {
        send(UndoRemoveEventIntent(eventId))
    }

    private fun startEditScheduledEvent(dragView: View, startTime: Time, endTime: Time) {
//        startActionMode()
        dragView.dragStartTime.text = startTime.toString()
        dragView.dragEndTime.text = endTime.toString()
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(dragView.dragStartTime, 8, 14, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(dragView.dragEndTime, 8, 14, 1, TypedValue.COMPLEX_UNIT_SP)
    }

    override fun onStartEditNewScheduledEvent(startTime: Time, duration: Int) {
        send(AddNewScheduledQuestIntent(startTime, duration))
    }

//    override fun onStartEditScheduledEvent(adapterPosition: Int) {
//        send(StartEditScheduledQuestIntent(eventsAdapter.events[adapterPosition]))
//    }
//
//
//    override fun onStartEditUnscheduledEvent(adapterPosition: Int) {
//        send(StartEditUnscheduledQuestIntent(unscheduledEventsAdapter.events[adapterPosition]))
////        startActionMode()
////        dragView.dragStartTime.visibility = View.GONE
////        dragView.dragEndTime.visibility = View.GONE
////        setupDragViewNameAndColor(dragView, name, color, unscheduledEventsAdapter.events[adapterPosition].reminder)
//    }

    private fun setupDragViewNameAndColor(dragView: View, name: String, color: AndroidColor, reminder: ReminderViewModel? = null) {
        dragView.dragName.setText(name)
        dragView.setBackgroundColor(ContextCompat.getColor(dragView.context, color.color500))

        dragView.dragName.setOnFocusChangeListener { _, isFocused ->
            if (isFocused) {
                calendarDayView.startEditDragEventName()
            }
        }

        dragView.dragName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                calendarDayView.updateDragEventName(text.toString())
            }

        })

        dragView.reminder.setOnClickListener {
            ReminderPickerDialogController(object : ReminderPickerDialogController.ReminderPickedListener {
                override fun onReminderPicked(reminder: ReminderViewModel?) {
                    calendarDayView.requestFocus()
                    ViewUtils.hideKeyboard(calendarDayView)
                    send(ReminderPickedIntent(reminder))
                }
            }, reminder)
                .showDialog(router, "pick_reminder_tag")
        }
    }

    override fun onDragViewClick(dragView: View) {
        ViewUtils.hideKeyboard(calendarDayView)
        dragView.requestFocus()
    }

    override fun onDragViewColorChange(dragView: View, color: AndroidColor) {
        ObjectAnimator.ofArgb(
            dragView,
            "backgroundColor",
            (dragView.background as ColorDrawable).color,
            ContextCompat.getColor(dragView.context, color.color500)
        )
            .setDuration(dragView.context.resources.getInteger(android.R.integer.config_longAnimTime).toLong())
            .start()
    }

    override fun onRescheduleScheduledEvent(position: Int, startTime: Time, duration: Int) {
        onStopEditMode()
        ViewUtils.hideKeyboard(calendarDayView)
        eventsAdapter.rescheduleEvent(position, startTime, duration)
    }

    override fun onEventValidationError(dragView: View) {
        dragView.dragName.error = "Think of a name"
    }

    override fun onScheduleUnscheduledEvent(position: Int, startTime: Time) {
        onStopEditMode()
        ViewUtils.hideKeyboard(calendarDayView)
        val ue = unscheduledEventsAdapter.removeEvent(position)
        val endTime = Time.plusMinutes(startTime, ue.duration)
        val vm = QuestViewModel(
            ue.id,
            ue.name,
            ue.duration,
            startTime.toMinuteOfDay(),
            startTime.toString(),
            endTime.toString(),
            AndroidIcon.PAW,
            ue.backgroundColor,
            ue.backgroundColor.color900,
            null,
            ue.isCompleted)
        eventsAdapter.addEvent(vm)
    }

    override fun onUnscheduleScheduledEvent(position: Int) {
        onStopEditMode()
        ViewUtils.hideKeyboard(calendarDayView)
        val e = eventsAdapter.removeEvent(position)
        val vm = UnscheduledQuestViewModel(
            e.id,
            e.name,
            e.duration,
            AndroidIcon.PAW,
            e.backgroundColor,
            e.textColor,
            e.isCompleted
        )
        unscheduledEventsAdapter.addEvent(vm)
    }

    override fun onCancelRescheduleUnscheduledEvent() {
        onStopEditMode()
    }

    override fun onRemoveEvent(eventId: String) {
        onStopEditMode()
        send(RemoveEventIntent(eventId))
    }

    override fun onMoveEvent(dragView: View, startTime: Time?, endTime: Time?) {
        if (startTime == null && endTime == null) {
            dragView.dragStartTime.visibility = View.GONE
            dragView.dragEndTime.visibility = View.GONE
        }
        if (startTime != null) {
            dragView.dragStartTime.visibility = View.VISIBLE
            dragView.dragStartTime.text = startTime.toString()
        }
        if (endTime != null) {
            dragView.dragEndTime.visibility = View.VISIBLE
            dragView.dragEndTime.text = endTime.toString()
        }
    }

    override fun onZoomEvent(adapterView: View) {
        eventsAdapter.adaptViewForHeight(adapterView, ViewUtils.pxToDp(adapterView.height, adapterView.context))
    }

    override fun onAddEvent(event: CalendarEvent) {
        send(AddEventIntent(event))
        ViewUtils.hideKeyboard(calendarDayView)
    }

    override fun onEditCalendarEvent(event: CalendarEvent, adapterPosition: Int) {
        send(EditEventIntent(event, eventsAdapter.events[adapterPosition].reminder))
        ViewUtils.hideKeyboard(calendarDayView)
    }

    override fun onEditUnscheduledCalendarEvent(event: CalendarEvent, adapterPosition: Int) {
        send(EditEventIntent(event, unscheduledEventsAdapter.events[adapterPosition].reminder))
        ViewUtils.hideKeyboard(calendarDayView)
    }

    override fun onEditUnscheduledEvent(event: UnscheduledEvent) {
        send(EditUnscheduledEventIntent(event))
        ViewUtils.hideKeyboard(calendarDayView)
    }

    private fun startActionMode(selectedIcon: AndroidIcon?) {
        parentController?.view?.startActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(am: ActionMode, item: MenuItem): Boolean {
                when (item.itemId) {

                    R.id.chooseIcon -> {
                        IconPickerDialogController({ icon ->
                            send(IconPickedIntent(icon))
                        }, selectedIcon).showDialog(router, "icon-picker")
                    }

                    R.id.chooseColor -> {
                        showColorPicker()
                    }

                    R.id.removeEvent -> {
                        calendarDayView.onRemoveEvent()
                    }
                }
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                actionMode = mode
                mode.menuInflater.inflate(R.menu.calendar_quest_edit_menu, menu)
                return true
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?) = false

            override fun onDestroyActionMode(p0: ActionMode?) {
                cancelEdit()
                (parentController as CalendarViewController).onStopEdit()
                actionMode = null
            }
        })
    }

    private fun showColorPicker() {
        ColorPickerDialogController(object : ColorPickerDialogController.ColorPickedListener {
            override fun onColorPicked(color: AndroidColor) {
                calendarDayView.updateDragBackgroundColor(color)
            }

        }, calendarDayView.getDragViewBackgroundColor())
            .showDialog(router, "pick_color_tag")
    }

    private fun onStopEditMode() {
        (parentController as CalendarViewController).onStopEdit()
        actionMode?.finish()
    }

    data class QuestViewModel(override val id: String,
                              override val name: String,
                              override val duration: Int,
                              override val startMinute: Int,
                              val startTime: String,
                              val endTime: String,
                              val icon: AndroidIcon?,
                              override val backgroundColor: AndroidColor,
                              @ColorRes val textColor: Int,
                              val reminder: ReminderViewModel?,
                              val isCompleted: Boolean) : CalendarEvent

    inner class QuestScheduledEventsAdapter(context: Context, events: List<QuestViewModel>, private val calendarDayView: CalendarDayView) :
        ScheduledEventsAdapter<QuestViewModel>(context, R.layout.item_calendar_quest, events.toMutableList()) {

        override fun bindView(view: View, position: Int) {
            val vm = getItem(position)

            view.setOnLongClickListener {
                send(StartEditScheduledQuestIntent(vm))
//                (parentController as CalendarViewController).onStartEdit()
                calendarDayView.startEventRescheduling(vm)
                true
            }

            view.startTime.text = vm.startTime
            view.endTime.text = vm.endTime

            view.backgroundView.setBackgroundColor(colorRes(vm.backgroundColor.color200))

            if (vm.isCompleted) {
                val span = SpannableString(vm.name)
                span.setSpan(StrikethroughSpan(), 0, vm.name.length, 0)
                view.questName.text = span
                view.questCategoryIndicator.setBackgroundResource(R.color.md_grey_500)
                view.checkBox.isChecked = true
                (view.checkBox as TintableCompoundButton).supportButtonTintList = tintList(R.color.md_grey_700)
                view.completedBackgroundView.visibility = View.VISIBLE
            } else {
                view.questName.text = vm.name
                view.questName.setTextColor(ContextCompat.getColor(context, vm.textColor))
                vm.icon?.let {
                    val icon = IconicsDrawable(context)
                        .icon(it.icon)
                        .colorRes(it.color)
                        .sizeDp(24)
                    view.questName.setCompoundDrawablesRelative(icon, null, null, null)
                    view.questName.compoundDrawablePadding = ViewUtils.dpToPx(8f, context).toInt()
                }

                view.questCategoryIndicator.setBackgroundResource(vm.backgroundColor.color700)
                (view.checkBox as TintableCompoundButton).supportButtonTintList = tintList(vm.backgroundColor.color500)
                view.completedBackgroundView.visibility = View.INVISIBLE
            }

            view.checkBox.setOnCheckedChangeListener { cb, checked ->
                if (checked) {
                    (view.checkBox as TintableCompoundButton).supportButtonTintList = tintList(R.color.md_grey_700)
                    val anim = RevealAnimator().create(view.completedBackgroundView, cb)
                    anim.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            view.completedBackgroundView.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            send(CompleteQuestIntent(vm.id))
                        }

                    })
                    anim.start()
                } else {

                    val anim = RevealAnimator().create(
                        view.completedBackgroundView,
                        cb,
                        reverse = true
                    )

                    anim.addListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator?) {
                            view.completedBackgroundView.visibility = View.INVISIBLE
                            send(UndoCompleteQuestIntent(vm.id))
                        }
                    })
                    anim.start()

                }
            }

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(view.questName, 8, 16, 1, TypedValue.COMPLEX_UNIT_SP)

            val millis = System.currentTimeMillis()
//            Timber.d("AAA before $position")
            view.post {
                Timber.d("AAA post $position ${System.currentTimeMillis() - millis}")
                adaptViewForHeight(view, ViewUtils.pxToDp(view.height, context))
            }
        }

        override fun rescheduleEvent(position: Int, startTime: Time, duration: Int) {
            val vm = getItem(position)
            events[position] = vm.copy(
                startMinute = startTime.toMinuteOfDay(),
                startTime = startTime.toString(),
                duration = duration,
                endTime = Time.plusMinutes(startTime, duration).toString()
            )
            notifyDataSetChanged()
        }

        override fun adaptViewForHeight(adapterView: View, height: Float) {
            val heightDp = ViewUtils.pxToDp(height.toInt(), adapterView.context)
            with(adapterView) {
                when {
                    heightDp < 12 -> ViewUtils.hideViews(checkBox, startTime, endTime)
                    heightDp < 26 -> {
                        ViewUtils.showViews(startTime, endTime)
                        ViewUtils.hideViews(checkBox)
                        ViewUtils.setMarginTop(startTime, 0)
                        ViewUtils.setMarginBottom(endTime, 0)

                        if (indicatorContainer.orientation == LinearLayout.VERTICAL) {
                            indicatorContainer.orientation = LinearLayout.HORIZONTAL
                            reverseIndicators(indicatorContainer)
                        }
                    }
                    else -> {
                        ViewUtils.showViews(checkBox, startTime, endTime)
                        ViewUtils.setMarginTop(startTime, 8)
                        ViewUtils.setMarginBottom(endTime, 8)
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
    }

    data class UnscheduledQuestViewModel(override val id: String,
                                         override val name: String,
                                         override val duration: Int,
                                         val icon: AndroidIcon?,
                                         override val backgroundColor: AndroidColor,
                                         @ColorRes val textColor: Int,
                                         val isCompleted: Boolean,
                                         val reminder: ReminderViewModel? = null) : UnscheduledEvent

    inner class UnscheduledQuestsAdapter(items: List<UnscheduledQuestViewModel>, calendarDayView: CalendarDayView) :
        UnscheduledEventsAdapter<UnscheduledQuestViewModel>
        (R.layout.unscheduled_quest_item, items.toMutableList(), calendarDayView) {

        override fun ViewHolder.bind(event: UnscheduledQuestViewModel, calendarDayView: CalendarDayView) {
            (itemView.unscheduledDone as TintableCompoundButton).supportButtonTintList = tintList(event.backgroundColor.color200, itemView.context)
            itemView.setOnLongClickListener {
                send(StartEditUnscheduledQuestIntent(event))
//                (parentController as CalendarViewController).onStartEdit()
                calendarDayView.startEventRescheduling(events[adapterPosition])
                true
            }

            if (!event.isCompleted) {
                itemView.name.text = event.name
                itemView.name.setTextColor(ContextCompat.getColor(itemView.context, event.textColor))
                (itemView.unscheduledDone as TintableCompoundButton).supportButtonTintList = tintList(event.backgroundColor.color500, itemView.context)
            } else {
                val span = SpannableString(event.name)
                span.setSpan(StrikethroughSpan(), 0, event.name.length, 0)
                itemView.name.text = span
                itemView.unscheduledDone.isChecked = true
                (itemView.unscheduledDone as TintableCompoundButton).supportButtonTintList = tintList(R.color.md_grey_700, itemView.context)
            }

            itemView.unscheduledDone.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    send(CompleteQuestIntent(event.id))
                } else {
                    send(UndoCompleteQuestIntent(event.id))
                }

            }
        }

        private fun tintList(@ColorRes color: Int, context: Context) = ContextCompat.getColorStateList(context, color)
    }
}