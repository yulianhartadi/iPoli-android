package io.ipoli.android.quest.schedule

import android.os.Bundle
import android.view.*
import com.mikepenz.entypo_typeface_library.Entypo
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.*
import io.ipoli.android.quest.schedule.ScheduleViewState.DatePickerState.*
import io.ipoli.android.quest.schedule.ScheduleViewState.StateType.*
import io.ipoli.android.quest.schedule.addquest.AddQuestAnimationHelper
import io.ipoli.android.quest.schedule.calendar.CalendarViewController
import kotlinx.android.synthetic.main.controller_schedule.view.*
import kotlinx.android.synthetic.main.view_calendar_toolbar.view.*
import org.threeten.bp.LocalDate
import sun.bob.mcalendarview.CellConfig
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.listeners.OnMonthScrollListener
import sun.bob.mcalendarview.vo.DateData

class ScheduleViewController(args: Bundle? = null) :
    ReduxViewController<ScheduleAction, ScheduleViewState, ScheduleReducer>(args) {

    override val reducer = ScheduleReducer

    private var calendarToolbar: ViewGroup? = null

    private lateinit var addQuestAnimationHelper: AddQuestAnimationHelper

    private var viewModeIcon: IIcon = Entypo.Icon.ent_sweden

    private var viewModeTitle = "Agenda"

    private var showDailyChallenge = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {

        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_schedule, container, false)

        addQuestAnimationHelper = AddQuestAnimationHelper(
            controller = this,
            addContainer = view.addContainer,
            fab = view.addQuest,
            background = view.addContainerBackground
        )
        initAddQuest(view)

        parentController!!.view!!.post {
            addToolbarView(R.layout.view_calendar_toolbar)?.let {
                calendarToolbar = it as ViewGroup
            }
            initDayPicker(view)
        }

        setChildController(
            view.contentContainer,
            CalendarViewController(LocalDate.now())
        )

        return view
    }

    override fun onCreateLoadAction() = ScheduleAction.Load

    override fun onDestroyView(view: View) {
        calendarToolbar?.let { removeToolbarView(it) }
        super.onDestroyView(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.schedule_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.actionViewMode).setIcon(
            IconicsDrawable(view!!.context)
                .icon(viewModeIcon)
                .colorRes(R.color.md_white)
                .sizeDp(24)
        ).title = viewModeTitle
        menu.findItem(R.id.actionDailyChallenge).isVisible = showDailyChallenge
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {

            R.id.actionViewMode -> {
                dispatch(ScheduleAction.ToggleViewMode)
                true
            }

            R.id.actionDailyChallenge -> {
                navigateFromRoot().toDailyChallenge()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    private fun initAddQuest(view: View) {
        view.addContainerBackground.setOnClickListener {
            val containerRouter = addContainerRouter(view)
            if (containerRouter.hasRootController()) {
                containerRouter.popCurrentController()
            }
            ViewUtils.hideKeyboard(view)
            addQuestAnimationHelper.closeAddContainer()
        }
    }

    private fun addContainerRouter(view: View) =
        getChildRouter(view.addContainer, "add-quest")

    private fun initDayPicker(view: View) {
        view.datePickerContainer.visibility = View.GONE

        view.datePicker.setMarkedStyle(MarkStyle.BACKGROUND, attrData(R.attr.colorAccent))

        val currentDate = LocalDate.now()

        val dateData = DateData(
            currentDate.year,
            currentDate.monthValue,
            currentDate.dayOfMonth
        )

        CellConfig.m2wPointDate = dateData
        CellConfig.w2mPointDate = dateData

        view.datePicker.markDate(dateData)

        calendarToolbar?.dispatchOnClick { ScheduleAction.ExpandToolbar }
        view.expander.dispatchOnClick { ScheduleAction.ExpandWeekToolbar }

        view.datePicker.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(v: View, date: DateData) {
                dispatch(
                    ScheduleAction.ScheduleChangeDate(
                        LocalDate.of(
                            date.year,
                            date.month,
                            date.day
                        )
                    )
                )
            }
        })

        view.datePicker.setOnMonthScrollListener(object : OnMonthScrollListener() {
            override fun onMonthChange(year: Int, month: Int) {
                dispatch(ScheduleAction.ChangeMonth(year, month))
            }

            override fun onMonthScroll(positionOffset: Float) {
            }

        })
    }


    override fun render(state: ScheduleViewState, view: View) {
        view.currentMonth.text = state.monthText

        view.addQuest.setOnClickListener {
            addQuestAnimationHelper.openAddContainer(state.currentDate)
        }

        when (state.type) {

            INITIAL -> {
                renderCalendarToolbar(state)
                showDailyChallenge = state.showDailyChallenge
                activity?.invalidateOptionsMenu()
            }

            SHOW_DAILY_CHALLENGE_CHANGED -> {
                showDailyChallenge = state.showDailyChallenge
                activity?.invalidateOptionsMenu()
            }

            DATE_PICKER_CHANGED -> renderDatePicker(
                state.datePickerState,
                view,
                state.currentDate
            )

            DATE_AUTO_CHANGED -> {
                val dateData = DateData(
                    state.currentDate.year,
                    state.currentDate.monthValue,
                    state.currentDate.dayOfMonth
                )
                view.datePicker.markedDates.removeAdd()
                view.datePicker.markDate(
                    dateData
                )
                view.datePicker.travelTo(dateData)

                renderDatePicker(
                    state.datePickerState,
                    view,
                    state.currentDate
                )
                renderCalendarToolbar(state)
            }

            CALENDAR_DATE_CHANGED -> {
                markSelectedDate(view, state.currentDate)
                renderCalendarToolbar(state)
            }

            SWIPE_DATE_CHANGED -> {
                markSelectedDate(view, state.currentDate)
                renderCalendarToolbar(state)
            }

            VIEW_MODE_CHANGED -> {

                val childRouter = getChildRouter(view.contentContainer, null)
                val n = Navigator(childRouter)
                if (state.viewMode == ScheduleViewState.ViewMode.CALENDAR) {
                    n.replaceWithCalendar(state.currentDate)
                } else {
                    n.replaceWithAgenda(state.currentDate)
                }

                viewModeIcon = state.viewModeIcon
                viewModeTitle = state.viewModeTitle
                activity?.invalidateOptionsMenu()
            }

            else -> {
            }
        }
    }

    private fun renderCalendarToolbar(state: ScheduleViewState) {
        calendarToolbar?.day?.text = state.dayText(activity!!)
        calendarToolbar?.date?.text = state.dateText(activity!!)
    }

    private fun renderDatePicker(
        datePickerState: ScheduleViewState.DatePickerState,
        view: View,
        currentDate: LocalDate
    ) {
        when (datePickerState) {
            SHOW_MONTH -> showMonthDatePicker(view)
            SHOW_WEEK -> showWeekDatePicker(view, currentDate)
            INVISIBLE -> hideDatePicker(view, currentDate)
        }
    }

    private fun showWeekDatePicker(view: View, currentDate: LocalDate) {
        calendarToolbar?.let {
            it.calendarIndicator.animate().rotation(180f).duration = shortAnimTime
        }
        CellConfig.Month2WeekPos = CellConfig.middlePosition
        CellConfig.ifMonth = false
        CellConfig.weekAnchorPointDate =
            DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
        view.datePicker.shrink()
        view.datePickerContainer.visibility = View.VISIBLE
        view.expander.setImageResource(R.drawable.ic_arrow_drop_down_white_24dp)
    }

    private fun showMonthDatePicker(view: View) {
        CellConfig.ifMonth = true
        CellConfig.Week2MonthPos = CellConfig.middlePosition
        view.datePicker.expand()
        view.expander.setImageResource(R.drawable.ic_arrow_drop_up_white_24dp)
    }

    private fun hideDatePicker(view: View, currentDate: LocalDate) {
        calendarToolbar?.let {
            it.calendarIndicator.animate().rotation(0f).duration = shortAnimTime
        }
        view.datePickerContainer.visibility = View.GONE
        CellConfig.Month2WeekPos = CellConfig.middlePosition
        CellConfig.ifMonth = false
        CellConfig.weekAnchorPointDate =
            DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
        view.datePicker.shrink()
    }

    private fun markSelectedDate(view: View, currentDate: LocalDate) {
        CellConfig.weekAnchorPointDate =
            DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
        view.datePicker.markedDates.removeAdd()

        val dateData = DateData(
            currentDate.year,
            currentDate.monthValue,
            currentDate.dayOfMonth
        )
        view.datePicker.markDate(
            dateData
        )
    }

    fun onStartEdit() {
        view!!.addQuest.visible = false
    }

    fun onStopEdit() {
        view!!.addQuest.visible = true
    }

    private val ScheduleViewState.viewModeIcon: IIcon
        get() = if (viewMode == ScheduleViewState.ViewMode.CALENDAR)
            Entypo.Icon.ent_sweden
        else
            GoogleMaterial.Icon.gmd_event
}