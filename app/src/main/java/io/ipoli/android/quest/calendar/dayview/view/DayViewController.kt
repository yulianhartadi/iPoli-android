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
import com.mikepenz.iconics.IconicsDrawable
import io.ipoli.android.Constants
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.isNotEqual
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.quest.Icon
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
import space.traversal.kapsule.required

class DayViewController :
    MviViewController<DayViewState, DayViewController, DayViewPresenter, DayViewIntent>,
    CalendarDayView.CalendarChangeListener {

    private lateinit var currentDate: LocalDate

    private var actionMode: ActionMode? = null

    private lateinit var eventsAdapter: QuestScheduledEventsAdapter

    private lateinit var unscheduledEventsAdapter: UnscheduledQuestsAdapter

    private var colorPickListener: () -> Unit = {}

    private var iconPickedListener: () -> Unit = {}

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

    override fun render(state: DayViewState, view: View) {
        when (state.type) {
            SCHEDULE_LOADED -> {
                eventsAdapter = QuestScheduledEventsAdapter(activity!!, state.scheduledQuests, calendarDayView)
                calendarDayView.setScheduledEventsAdapter(eventsAdapter)
                unscheduledEventsAdapter = UnscheduledQuestsAdapter(state.unscheduledQuests, calendarDayView)
                calendarDayView.setUnscheduledEventsAdapter(unscheduledEventsAdapter)
                updateUnscheduledQuestsHeight(view)
            }

            ADD_NEW_SCHEDULED_QUEST -> {

                colorPickListener = { showColorPicker(state.color) }
                iconPickedListener = { showIconPicker(state.icon) }

                startActionMode()
                (parentController as CalendarViewController).onStartEdit()
                val dragView = view.dragContainer
                dragView.dragStartTime.visibility = View.VISIBLE
                dragView.dragEndTime.visibility = View.VISIBLE
                startEditScheduledEvent(dragView, state.startTime!!, state.endTime!!)
                setupDragViewUI(dragView, state.name, state.color!!, state.icon)
            }

            START_EDIT_SCHEDULED_QUEST -> {
                colorPickListener = { showColorPicker(state.color) }
                iconPickedListener = { showIconPicker(state.icon) }
                startActionMode()
                (parentController as CalendarViewController).onStartEdit()
                val dragView = view.dragContainer
                dragView.dragStartTime.visibility = View.VISIBLE
                dragView.dragEndTime.visibility = View.VISIBLE
                startEditScheduledEvent(dragView, state.startTime!!, state.endTime!!)
                setupDragViewUI(dragView, state.name, state.color!!, state.icon, state.reminder)
            }

            START_EDIT_UNSCHEDULED_QUEST -> {
                colorPickListener = { showColorPicker(state.color) }
                iconPickedListener = { showIconPicker(state.icon) }
                startActionMode()
                (parentController as CalendarViewController).onStartEdit()
                val dragView = view.dragContainer
                dragView.dragStartTime.visibility = View.GONE
                dragView.dragEndTime.visibility = View.GONE
                setupDragViewUI(dragView, state.name, state.color!!, state.icon, state.reminder)
            }

            EVENT_UPDATED -> {
                onStopEditMode()
                calendarDayView.onEventUpdated()
                ViewUtils.hideKeyboard(calendarDayView)
            }

            EVENT_VALIDATION_ERROR -> {
                calendarDayView.onEventValidationError()
            }

            EVENT_REMOVED -> {
                PetMessagePopup(
                    stringRes(R.string.remove_quest_undo_message),
                    { sendUndoRemovedEventIntent(state.removedEventId) }
                ).show(view.context)
            }

            NEW_EVENT_REMOVED -> {

            }

            COLOR_PICKED -> {
                colorPickListener = { showColorPicker(state.color) }
                val dragView = view.dragContainer
                ObjectAnimator.ofArgb(
                    dragView,
                    "backgroundColor",
                    (dragView.background as ColorDrawable).color,
                    ContextCompat.getColor(dragView.context, state.color!!.color500)
                )
                    .setDuration(dragView.context.resources.getInteger(android.R.integer.config_longAnimTime).toLong())
                    .start()
            }

            ICON_PICKED -> {
                iconPickedListener = { showIconPicker(state.icon) }
                val dragIcon = view.dragContainer.dragIcon
                if (state.icon == null) {
                    dragIcon.setImageDrawable(null)
                } else {
                    dragIcon.setImageDrawable(
                        IconicsDrawable(dragIcon.context)
                            .icon(state.icon.icon)
                            .colorRes(R.color.md_white)
                            .sizeDp(24)
                    )
                }
            }

            QUEST_COMPLETED -> {
            }

            UNDO_QUEST_COMPLETED -> {
            }

            EDIT_QUEST -> {
                colorPickListener = { showColorPicker(state.color) }
                iconPickedListener = { showIconPicker(state.icon) }
                startActionMode()
            }

            EDIT_VIEW_DRAGGED -> {
                val dragView = view.dragContainer
                if (state.startTime == null && state.endTime == null) {
                    dragView.dragStartTime.visibility = View.GONE
                    dragView.dragEndTime.visibility = View.GONE
                }
                if (state.startTime != null) {
                    dragView.dragStartTime.visibility = View.VISIBLE
                    dragView.dragStartTime.text = state.startTime.toString()
                }
                if (state.endTime != null) {
                    dragView.dragEndTime.visibility = View.VISIBLE
                    dragView.dragEndTime.text = state.endTime.toString()
                }
            }

            REMINDER_PICKED -> {

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
        dragView.dragStartTime.text = startTime.toString()
        dragView.dragEndTime.text = endTime.toString()
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(dragView.dragStartTime, 8, 14, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(dragView.dragEndTime, 8, 14, 1, TypedValue.COMPLEX_UNIT_SP)
    }

    override fun onStartEditNewScheduledEvent(startTime: Time, duration: Int) {
        send(AddNewScheduledQuestIntent(startTime, duration))
    }

    private fun setupDragViewUI(dragView: View, name: String, color: AndroidColor, icon: AndroidIcon? = null, reminder: ReminderViewModel? = null) {
        dragView.dragName.setText(name)
        dragView.setBackgroundColor(ContextCompat.getColor(dragView.context, color.color500))
        if (icon != null) {
            dragView.dragIcon.setImageDrawable(
                IconicsDrawable(dragView.context)
                    .icon(icon.icon)
                    .colorRes(R.color.md_white)
                    .sizeDp(24)
            )

        } else {
            dragView.dragIcon.setImageDrawable(null)
        }
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
                send(ChangeEditViewNameIntent(text.toString()))
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

    override fun onEventValidationError(dragView: View) {
        dragView.dragName.error = "Think of a name"
    }

    override fun onRemoveEvent(eventId: String) {
        onStopEditMode()
        send(RemoveEventIntent(eventId))
    }

    override fun onMoveEvent(startTime: Time?, endTime: Time?) {
        send(DragMoveViewIntent(startTime, endTime))
    }

    override fun onResizeEvent(startTime: Time?, endTime: Time?, duration: Int) {
        send(DragResizeViewIntent(startTime, endTime, duration))
    }

    override fun onZoomEvent(adapterView: View) {
        eventsAdapter.adaptViewForHeight(adapterView, ViewUtils.pxToDp(adapterView.height, adapterView.context))
    }

    override fun onAddEvent() {
        send(AddQuestIntent)
    }

    override fun onEditCalendarEvent() {
        send(EditQuestIntent)
    }

    override fun onEditUnscheduledCalendarEvent() {
        send(EditQuestIntent)
    }

    override fun onEditUnscheduledEvent() {
        send(EditUnscheduledQuestIntent)
    }

    private fun startActionMode() {
        parentController?.view?.startActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(am: ActionMode, item: MenuItem): Boolean {
                when (item.itemId) {

                    R.id.chooseIcon -> {
                        iconPickedListener()
                    }

                    R.id.chooseColor -> {
                        colorPickListener()
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

    private fun showIconPicker(selectedIcon: AndroidIcon?) {
        IconPickerDialogController({ icon ->
            val ic = icon?.let {
                Icon.valueOf(it.name)
            }
            send(IconPickedIntent(ic))
        }, selectedIcon).showDialog(router, "icon-picker")
    }

    private fun showColorPicker(selectedColor: AndroidColor?) {
        ColorPickerDialogController(object : ColorPickerDialogController.ColorPickedListener {
            override fun onColorPicked(color: AndroidColor) {
                send(ColorPickedIntent(color))
            }

        }, selectedColor)
            .showDialog(router, "pick_color_tag")
    }

    private fun onStopEditMode() {
        (parentController as CalendarViewController).onStopEdit()
        actionMode?.finish()
    }

    data class QuestViewModel(override val id: String,
                              val name: String,
                              override val duration: Int,
                              override val startMinute: Int,
                              val startTime: String,
                              val endTime: String,
                              val icon: AndroidIcon?,
                              val backgroundColor: AndroidColor,
                              @ColorRes val textColor: Int,
                              val reminder: ReminderViewModel?,
                              val isCompleted: Boolean) : CalendarEvent

    inner class QuestScheduledEventsAdapter(context: Context, events: List<QuestViewModel>, private val calendarDayView: CalendarDayView) :
        ScheduledEventsAdapter<QuestViewModel>(context, R.layout.item_calendar_quest, events.toMutableList()) {

        override fun bindView(view: View, position: Int) {
            val vm = getItem(position)

            view.setOnLongClickListener {
                send(StartEditScheduledQuestIntent(vm))
                calendarDayView.startEventRescheduling(vm)
                true
            }

            view.startTime.text = vm.startTime
            view.endTime.text = vm.endTime

            view.backgroundView.setBackgroundColor(colorRes(vm.backgroundColor.color300))

            if (vm.isCompleted) {
                val span = SpannableString(vm.name)
                span.setSpan(StrikethroughSpan(), 0, vm.name.length, 0)
                view.questName.text = span
                view.questName.setTextColor(colorRes(R.color.md_dark_text_54))

                view.startTime.setTextColor(colorRes(R.color.md_dark_text_54))
                view.endTime.setTextColor(colorRes(R.color.md_dark_text_54))

                view.checkBox.isChecked = true
                (view.checkBox as TintableCompoundButton).supportButtonTintList = tintList(R.color.md_grey_700)
                view.completedBackgroundView.visibility = View.VISIBLE

                vm.icon?.let {
                    val icon = IconicsDrawable(context)
                        .icon(it.icon)
                        .colorRes(R.color.md_dark_text_26)
                        .sizeDp(24)
                    view.questIcon.visible = true
                    view.questIcon.setImageDrawable(icon)
                }


            } else {

                view.questCategoryIndicator.setBackgroundResource(vm.backgroundColor.color700)

                view.questName.text = vm.name
                view.questName.setTextColor(colorRes(R.color.md_dark_text_54))
                view.startTime.setTextColor(colorRes(R.color.md_dark_text_54))
                view.endTime.setTextColor(colorRes(R.color.md_dark_text_54))

                vm.icon?.let {
                    val icon = IconicsDrawable(context)
                        .icon(it.icon)
                        .colorRes(vm.backgroundColor.color600)
                        .sizeDp(24)
                    view.questIcon.visible = true
                    view.questIcon.setImageDrawable(icon)
                }

                (view.checkBox as TintableCompoundButton).supportButtonTintList = tintList(vm.backgroundColor.color700)
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

            view.post {
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
            val cbHeight = adapterView.checkBox.height
            val avHeight = adapterView.height
            val stHeight = adapterView.startTime.height
            val etHeight = adapterView.endTime.height

            with(adapterView) {
                when {
                    avHeight <= cbHeight -> ViewUtils.goneViews(checkBox, startTime, endTime)
                    avHeight > cbHeight && avHeight < cbHeight + stHeight + etHeight + ViewUtils.dpToPx(4f, adapterView.context) -> {
                        ViewUtils.showViews(checkBox)
                        ViewUtils.hideViews(startTime, endTime)
                    }
                    else -> {
                        ViewUtils.showViews(checkBox, startTime, endTime)
                        ViewUtils.setMarginTop(startTime, 2)
                        ViewUtils.setMarginBottom(endTime, 2)
                    }
                }
            }
        }

        private fun tintList(@ColorRes color: Int) = ContextCompat.getColorStateList(context, color)
    }

    data class UnscheduledQuestViewModel(override val id: String,
                                         val name: String,
                                         override val duration: Int,
                                         val icon: AndroidIcon?,
                                         val backgroundColor: AndroidColor,
                                         @ColorRes val textColor: Int,
                                         val isCompleted: Boolean,
                                         val reminder: ReminderViewModel? = null) : UnscheduledEvent

    inner class UnscheduledQuestsAdapter(items: List<UnscheduledQuestViewModel>, calendarDayView: CalendarDayView) :
        UnscheduledEventsAdapter<UnscheduledQuestViewModel>
        (R.layout.unscheduled_quest_item, items.toMutableList(), calendarDayView) {

        override fun ViewHolder.bind(event: UnscheduledQuestViewModel, calendarDayView: CalendarDayView) {
            itemView.setOnLongClickListener {
                send(StartEditUnscheduledQuestIntent(event))
                calendarDayView.startEventRescheduling(events[adapterPosition])
                true
            }

            (itemView.unscheduledDone as TintableCompoundButton).supportButtonTintList = tintList(event.backgroundColor.color500, itemView.context)

            if (!event.isCompleted) {
                itemView.unscheduledQuestName.text = event.name
                itemView.unscheduledQuestName.setTextColor(ContextCompat.getColor(itemView.context, event.textColor))


                event.icon?.let {
                    val icon = IconicsDrawable(itemView.context)
                        .icon(it.icon)
                        .colorRes(it.color)
                        .sizeDp(24)
                    itemView.unscheduledQuestIcon.visible = true
                    itemView.unscheduledQuestIcon.setImageDrawable(icon)
                }

            } else {
                val span = SpannableString(event.name)
                span.setSpan(StrikethroughSpan(), 0, event.name.length, 0)
                itemView.unscheduledQuestName.text = span
                itemView.unscheduledDone.isChecked = true

                event.icon?.let {
                    val icon = IconicsDrawable(itemView.context)
                        .icon(it.icon)
                        .colorRes(R.color.md_dark_text_26)
                        .sizeDp(24)
                    itemView.unscheduledQuestIcon.visible = true
                    itemView.unscheduledQuestIcon.setImageDrawable(icon)
                }

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