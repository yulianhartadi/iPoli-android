package io.ipoli.android.quest.schedule.summary

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.*
import android.widget.TextView
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import io.ipoli.android.Constants
import io.ipoli.android.MainActivity
import io.ipoli.android.R
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.datetime.isToday
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.text.DateFormatter
import io.ipoli.android.common.view.*
import io.ipoli.android.common.view.recyclerview.MultiViewRecyclerViewAdapter
import io.ipoli.android.common.view.recyclerview.RecyclerViewViewModel
import io.ipoli.android.common.view.recyclerview.SimpleSwipeCallback
import io.ipoli.android.quest.schedule.summary.ScheduleSummaryViewState.StateType.*
import kotlinx.android.synthetic.main.controller_schedule_summary.view.*
import kotlinx.android.synthetic.main.item_monthly_preview_scheduled_quest.view.*
import kotlinx.android.synthetic.main.item_monthly_preview_unscheduled_quest.view.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.format.TextStyle
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 07/03/2018.
 */
class ScheduleSummaryViewController(args: Bundle? = null) :
    ReduxViewController<ScheduleSummaryAction, ScheduleSummaryViewState, ScheduleSummaryReducer>(
        args
    ) {

    override val reducer = ScheduleSummaryReducer

    private var currentDate = LocalDate.now()

    constructor(currentDate: LocalDate) : this() {
        this.currentDate = currentDate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = container.inflate(R.layout.controller_schedule_summary)
        setToolbar(view.toolbar)
        activity?.let {
            (it as MainActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_text_secondary_24dp)
        }
        setHasOptionsMenu(true)
        view.dailyQuests.layoutManager = LinearLayoutManager(view.context)
        view.dailyQuests.adapter = DayItemAdapter()

        view.calendarView.scrollToCalendar(
            currentDate.year,
            currentDate.monthValue,
            currentDate.dayOfMonth
        )

        when (DateUtils.firstDayOfWeek) {
            DayOfWeek.SATURDAY -> view.calendarView.setWeekStarWithSat()
            DayOfWeek.SUNDAY -> view.calendarView.setWeekStarWithSun()
            else -> view.calendarView.setWeekStarWithMon()
        }

        view.calendarView.setOnDateSelectedListener(SkipFirstChangeDateListener { calendar, _ ->
            val newDate = LocalDate.of(calendar.year, calendar.month, calendar.day)
            dispatch(ScheduleSummaryAction.ChangeDate(newDate))
        })

        renderToolbarDate(view, currentDate.monthValue, currentDate.year)

        val swipeHandler = object : SimpleSwipeCallback(
            R.drawable.ic_event_white_24dp,
            R.color.md_blue_500,
            R.drawable.ic_delete_white_24dp,
            R.color.md_red_500
        ) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.END) {
                    navigate().toReschedule(!currentDate.isToday, {
                        dispatch(ScheduleSummaryAction.RescheduleQuest(questId(viewHolder), it))

                        if (it == null) {
                            showShortToast(R.string.quest_moved_to_bucket_list)
                        } else {
                            showShortToast(R.string.quest_rescheduled)
                        }
                    }) {
                        view.dailyQuests.adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                } else if (direction == ItemTouchHelper.START) {
                    val questId = questId(viewHolder)
                    dispatch(ScheduleSummaryAction.RemoveQuest(questId))
                    activity?.let {
                        PetMessagePopup(
                            stringRes(R.string.remove_quest_undo_message),
                            { dispatch(ScheduleSummaryAction.UndoRemoveQuest(questId)) },
                            stringRes(R.string.undo)
                        ).show(it)
                    }
                }
            }

            private fun questId(holder: RecyclerView.ViewHolder): String {
                val adapter = view.dailyQuests.adapter as DayItemAdapter
                return if (holder.itemViewType == ViewType.SCHEDULED_QUEST.ordinal) {
                    val item =
                        adapter.getItemAt<DailyItemViewModel.ScheduledQuestItem>(holder.adapterPosition)
                    item.id
                } else {
                    val item =
                        adapter.getItemAt<DailyItemViewModel.UnscheduledQuestItem>(holder.adapterPosition)
                    item.id
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) = when {
                viewHolder.itemViewType == ViewType.SCHEDULED_QUEST.ordinal -> (ItemTouchHelper.END or ItemTouchHelper.START)
                viewHolder.itemViewType == ViewType.UNSCHEDULED_QUEST.ordinal -> (ItemTouchHelper.END or ItemTouchHelper.START)
                else -> 0
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(view.dailyQuests)

        return view
    }

    private fun renderToolbarDate(view: View, month: Int, year: Int) {
        view.currentMonth.text =
            Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
        view.currentYear.text = "$year"
    }

    override fun onCreateLoadAction() = ScheduleSummaryAction.Load(currentDate)

    override fun colorStatusBars() {
        activity?.let {
            it.window.statusBarColor = colorRes(attrResourceId(android.R.attr.colorBackground))
            it.window.navigationBarColor = colorRes(attrResourceId(android.R.attr.colorBackground))
            if (it.isDarkTheme) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                it.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        showBackButton()

        view.toolbar.onDebounceMenuClick({ item ->
            if (item.itemId == R.id.actionGoToToday) {
                view.calendarView.scrollToCurrent(true)
            }
        }, { _ ->
            router.handleBack()
        })
    }

    override fun onDetach(view: View) {
        resetDecorView()
        view.toolbar.clearDebounceListeners()
        super.onDetach(view)
    }

    private fun resetDecorView() {
        activity?.let {
            it.window.decorView.systemUiVisibility = 0
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.month_preview_menu, menu)
    }

    override fun render(state: ScheduleSummaryViewState, view: View) {
        when (state.type) {

            DATE_DATA_CHANGED -> {
                currentDate = state.currentDate
                renderToolbarDate(view, state.currentDate.monthValue, state.currentDate.year)
            }

            SCHEDULE_SUMMARY_DATA_CHANGED -> {
                view.calendarView.setSchemeDate(state.calendars)
            }

            SCHEDULE_DATA_CHANGED ->
                (view.dailyQuests.adapter as DayItemAdapter).updateAll(state.dailyItemViewModels)

            else -> {
            }
        }
    }

    enum class ViewType {
        DAY_HEADER,
        DAY_SUMMARY,
        UNSCHEDULED_QUEST,
        UNSCHEDULED_COMPLETED_QUEST,
        UNSCHEDULED_PLACEHOLDER_QUEST,
        TIME_OF_DAY_SECTION,
        SCHEDULED_QUEST,
        SCHEDULED_COMPLETED_QUEST,
        SCHEDULED_PLACEHOLDER_QUEST,
        EVENT_ITEM,
        ALL_DAY_EVENT_ITEM
    }

    sealed class DailyItemViewModel(override val id: String) : RecyclerViewViewModel {

        data class DayHeader(val text: String) : DailyItemViewModel(text)

        data class DaySummary(val text: String) : DailyItemViewModel(text)

        data class UnscheduledQuestItem(
            override val id: String,
            val name: String,
            @ColorRes val color: Int
        ) : DailyItemViewModel(id)

        data class UnscheduledCompletedQuestItem(
            override val id: String,
            val name: String,
            @ColorRes val color: Int
        ) : DailyItemViewModel(id)

        data class UnscheduledPlaceholderQuestItem(
            val name: String,
            @ColorRes val color: Int
        ) : DailyItemViewModel(name)

        data class TimeOfDaySection(val text: String) : DailyItemViewModel(text)

        data class ScheduledQuestItem(
            override val id: String,
            val name: String,
            @ColorRes val color: Int,
            override val startTime: Time,
            val scheduleText: String
        ) : DailyItemViewModel(id), ScheduledItem

        data class ScheduledCompletedQuestItem(
            override val id: String,
            val name: String,
            @ColorRes val color: Int,
            override val startTime: Time,
            val scheduleText: String
        ) : DailyItemViewModel(id), ScheduledItem

        data class ScheduledPlaceholderQuestItem(
            val name: String,
            @ColorRes val color: Int,
            override val startTime: Time,
            val scheduleText: String
        ) : DailyItemViewModel(name), ScheduledItem

        data class EventItem(
            override val id: String,
            val name: String,
            @ColorInt val color: Int,
            override val startTime: Time,
            val scheduleText: String
        ) : DailyItemViewModel(id), ScheduledItem

        data class AllDayEventItem(
            override val id: String,
            val name: String,
            @ColorInt val color: Int
        ) : DailyItemViewModel(id)

        interface ScheduledItem {
            val startTime: Time
        }
    }

    inner class DayItemAdapter : MultiViewRecyclerViewAdapter<DailyItemViewModel>() {

        override fun onRegisterItemBinders() {
            registerBinder<DailyItemViewModel.DayHeader>(
                ViewType.DAY_HEADER.ordinal,
                R.layout.item_monthly_preview_day_header
            ) { vm, view, _ ->
                (view as TextView).text = vm.text
            }

            registerBinder<DailyItemViewModel.DaySummary>(
                ViewType.DAY_SUMMARY.ordinal,
                R.layout.item_monthly_preview_day_summary
            ) { vm, view, _ ->
                (view as TextView).text = vm.text
            }

            registerBinder<DailyItemViewModel.AllDayEventItem>(
                ViewType.ALL_DAY_EVENT_ITEM.ordinal,
                R.layout.item_monthly_preview_unscheduled_quest
            ) { vm, view, _ ->
                view.previewUnscheduledQuestIndicator.background.setColorFilter(
                    vm.color,
                    PorterDuff.Mode.SRC_ATOP
                )

                view.previewUnscheduledQuestName.text = vm.name
                view.setOnClickListener(null)
                view.isClickable = false
            }

            registerBinder<DailyItemViewModel.UnscheduledQuestItem>(
                ViewType.UNSCHEDULED_QUEST.ordinal,
                R.layout.item_monthly_preview_unscheduled_quest
            ) { vm, view, _ ->
                view.previewUnscheduledQuestIndicator.background.setColorFilter(
                    colorRes(vm.color),
                    PorterDuff.Mode.SRC_ATOP
                )

                view.previewUnscheduledQuestName.text = vm.name
                view.onDebounceClick {
                    navigateFromRoot().toQuest(vm.id, HorizontalChangeHandler())
                }
            }

            registerBinder<DailyItemViewModel.UnscheduledPlaceholderQuestItem>(
                ViewType.UNSCHEDULED_PLACEHOLDER_QUEST.ordinal,
                R.layout.item_monthly_preview_unscheduled_quest
            ) { vm, view, _ ->
                view.previewUnscheduledQuestIndicator.background.setColorFilter(
                    colorRes(vm.color),
                    PorterDuff.Mode.SRC_ATOP
                )

                view.previewUnscheduledQuestName.text = vm.name
                view.setOnClickListener(null)
                view.isClickable = false
            }

            registerBinder<DailyItemViewModel.UnscheduledCompletedQuestItem>(
                ViewType.UNSCHEDULED_COMPLETED_QUEST.ordinal,
                R.layout.item_monthly_preview_unscheduled_quest
            ) { vm, view, _ ->
                view.previewUnscheduledQuestIndicator.background.setColorFilter(
                    colorRes(vm.color),
                    PorterDuff.Mode.SRC_ATOP
                )

                val span = SpannableString(vm.name)
                span.setSpan(StrikethroughSpan(), 0, vm.name.length, 0)
                view.previewUnscheduledQuestName.text = span
                view.onDebounceClick {
                    navigateFromRoot().toCompletedQuest(vm.id, HorizontalChangeHandler())
                }
            }

            registerBinder<DailyItemViewModel.TimeOfDaySection>(
                ViewType.TIME_OF_DAY_SECTION.ordinal,
                R.layout.item_monthly_preview_time_of_day
            ) { vm, view, _ ->
                (view as TextView).text = vm.text
            }

            registerBinder<DailyItemViewModel.ScheduledQuestItem>(
                ViewType.SCHEDULED_QUEST.ordinal,
                R.layout.item_monthly_preview_scheduled_quest
            ) { vm, view, _ ->
                view.previewQuestIndicator.background.setColorFilter(
                    colorRes(vm.color),
                    PorterDuff.Mode.SRC_ATOP
                )

                view.previewQuestSchedule.text = vm.scheduleText

                view.previewQuestName.text = vm.name
                view.onDebounceClick {
                    navigateFromRoot().toQuest(vm.id, HorizontalChangeHandler())
                }
            }

            registerBinder<DailyItemViewModel.ScheduledPlaceholderQuestItem>(
                ViewType.SCHEDULED_PLACEHOLDER_QUEST.ordinal,
                R.layout.item_monthly_preview_scheduled_quest
            ) { vm, view, _ ->
                view.previewQuestIndicator.background.setColorFilter(
                    colorRes(vm.color),
                    PorterDuff.Mode.SRC_ATOP
                )

                view.previewQuestSchedule.text = vm.scheduleText

                view.previewQuestName.text = vm.name
                view.setOnClickListener(null)
                view.isClickable = false
            }

            registerBinder<DailyItemViewModel.ScheduledCompletedQuestItem>(
                ViewType.SCHEDULED_COMPLETED_QUEST.ordinal,
                R.layout.item_monthly_preview_scheduled_quest
            ) { vm, view, _ ->
                view.previewQuestIndicator.background.setColorFilter(
                    colorRes(vm.color),
                    PorterDuff.Mode.SRC_ATOP
                )

                view.previewQuestSchedule.text = vm.scheduleText

                val span = SpannableString(vm.name)
                span.setSpan(StrikethroughSpan(), 0, vm.name.length, 0)
                view.previewQuestName.text = span
                view.onDebounceClick {
                    navigateFromRoot().toCompletedQuest(vm.id, HorizontalChangeHandler())
                }
            }

            registerBinder<DailyItemViewModel.EventItem>(
                ViewType.EVENT_ITEM.ordinal,
                R.layout.item_monthly_preview_scheduled_quest
            ) { vm, view, _ ->
                view.previewQuestIndicator.background.setColorFilter(
                    vm.color,
                    PorterDuff.Mode.SRC_ATOP
                )

                view.previewQuestName.text = vm.name
                view.previewQuestSchedule.text = vm.scheduleText
                view.setOnClickListener(null)
                view.isClickable = false
            }
        }
    }

    private val ScheduleSummaryViewState.dailyItemViewModels: List<DailyItemViewModel>
        get() {
            val c = activity ?: return emptyList()
            val s = schedule!!
            val items = mutableListOf<DailyItemViewModel>()

            items.add(DailyItemViewModel.DayHeader(DateFormatter.format(c, s.date)))

            val questCount = s.scheduledQuests.size + s.unscheduledQuests.size
            val eventCount = s.events.size + s.allDayEvents.size
            val completedCount =
                s.scheduledQuests.count { it.isCompleted } + s.unscheduledQuests.count { it.isCompleted }

            val qText = if (questCount > 0)
                quantityString(R.plurals.quest_quantity, questCount)
            else
                stringRes(R.string.no_quests)

            val eText = if (eventCount > 0)
                quantityString(R.plurals.event_quantity, eventCount)
            else
                stringRes(R.string.no_events)

            val doneText = stringRes(R.string.done).toLowerCase()

            if (questCount == 0 && eventCount == 0) {
                items.add(DailyItemViewModel.DaySummary(stringRes(R.string.nothing_scheduled)))
                return items
            } else if (questCount == 0 && eventCount > 0) {
                items.add(DailyItemViewModel.DaySummary("$qText, $eText"))
            } else if (questCount > 0 && eventCount == 0) {
                items.add(DailyItemViewModel.DaySummary("$qText ($completedCount $doneText), $eText"))
            } else {
                items.add(DailyItemViewModel.DaySummary("$qText ($completedCount $doneText), $eText"))
            }

            s.allDayEvents.forEach {
                items.add(
                    DailyItemViewModel.AllDayEventItem(
                        id = it.id,
                        name = it.name,
                        color = it.color
                    )
                )
            }

            s.unscheduledQuests.forEach {

                val item = when {
                    it.id.isEmpty() ->
                        DailyItemViewModel.UnscheduledPlaceholderQuestItem(
                            name = it.name,
                            color = it.color.androidColor.color500
                        )
                    it.isCompleted ->
                        DailyItemViewModel.UnscheduledCompletedQuestItem(
                            id = it.id,
                            name = it.name,
                            color = it.color.androidColor.color500
                        )
                    else ->
                        DailyItemViewModel.UnscheduledQuestItem(
                            id = it.id,
                            name = it.name,
                            color = it.color.androidColor.color500
                        )
                }

                items.add(item)
            }

            if (s.scheduledQuests.isEmpty() && s.events.isEmpty()) {
                return items
            }

            val questItems: List<DailyItemViewModel.ScheduledItem> = s.scheduledQuests.map {
                val item: DailyItemViewModel.ScheduledItem = when {
                    it.id.isEmpty() ->
                        DailyItemViewModel.ScheduledPlaceholderQuestItem(
                            name = it.name,
                            color = it.color.androidColor.color500,
                            startTime = it.startTime!!,
                            scheduleText = "${it.startTime.toString(shouldUse24HourFormat)} - ${it.endTime!!.toString(
                                shouldUse24HourFormat
                            )}"
                        )
                    it.isCompleted ->
                        DailyItemViewModel.ScheduledCompletedQuestItem(
                            id = it.id,
                            name = it.name,
                            color = it.color.androidColor.color500,
                            startTime = it.startTime!!,
                            scheduleText = "${it.startTime.toString(shouldUse24HourFormat)} - ${it.endTime!!.toString(
                                shouldUse24HourFormat
                            )}"
                        )
                    else ->
                        DailyItemViewModel.ScheduledQuestItem(
                            id = it.id,
                            name = it.name,
                            color = it.color.androidColor.color500,
                            startTime = it.startTime!!,
                            scheduleText = "${it.startTime.toString(shouldUse24HourFormat)} - ${it.endTime!!.toString(
                                shouldUse24HourFormat
                            )}"
                        )
                }
                item
            }

            val eventItems = s.events.map {
                DailyItemViewModel.EventItem(
                    id = it.id,
                    name = it.name,
                    color = it.color,
                    startTime = it.startTime,
                    scheduleText = "${it.startTime.toString(shouldUse24HourFormat)} - ${it.endTime.toString(
                        shouldUse24HourFormat
                    )}"
                )
            }

            val scheduledItems: List<DailyItemViewModel.ScheduledItem> = questItems + eventItems
            val sortedItems = scheduledItems.sortedBy { it.startTime.toMinuteOfDay() }

            val (morningItems, o) = sortedItems.partition { it.startTime < Constants.AFTERNOON_TIME_START }
            val (afternoonItems, eveningItems) = o.partition { it.startTime < Constants.EVENING_TIME_START }

            if (morningItems.isNotEmpty()) {
                items.add(DailyItemViewModel.TimeOfDaySection(stringRes(R.string.morning)))
                morningItems.forEach {
                    items.add(it as DailyItemViewModel)
                }
            }

            if (afternoonItems.isNotEmpty()) {
                items.add(DailyItemViewModel.TimeOfDaySection(stringRes(R.string.afternoon)))
                afternoonItems.forEach {
                    items.add(it as DailyItemViewModel)
                }
            }

            if (eveningItems.isNotEmpty()) {
                items.add(DailyItemViewModel.TimeOfDaySection(stringRes(R.string.evening)))
                eveningItems.forEach {
                    items.add(it as DailyItemViewModel)
                }
            }

            return items
        }

    private val ScheduleSummaryViewState.calendars: List<Calendar>
        get() = items.map {
            val itemDate = it.date
            Calendar().apply {
                day = itemDate.dayOfMonth
                month = itemDate.monthValue
                year = itemDate.year
                isCurrentDay = itemDate == currentDate
                isCurrentMonth = itemDate.month == currentDate.month
                isLeapYear = itemDate.isLeapYear
                val tagColors =
                    if (it.tagColors.isEmpty()) "" else "," + it.tagColors.joinToString(
                        ","
                    ) { it.name }
                scheme =
                    "${it.morningFullness.name},${it.afternoonFullness.name},${it.eveningFullness.name}$tagColors"
            }
        }

    class SkipFirstChangeDateListener(private inline val onChange: (Calendar, Boolean) -> Unit) :
        CalendarView.OnDateSelectedListener {

        private var isFirstChange = true

        override fun onDateSelected(calendar: Calendar, isClick: Boolean) {
            if (isFirstChange) {
                isFirstChange = false
                return
            }

            onChange(calendar, isClick)
        }

    }
}