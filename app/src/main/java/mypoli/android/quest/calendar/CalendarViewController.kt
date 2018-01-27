package mypoli.android.quest.calendar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import kotlinx.android.synthetic.main.controller_calendar.view.*
import kotlinx.android.synthetic.main.view_calendar_toolbar.view.*
import mypoli.android.R
import mypoli.android.common.ViewUtils
import mypoli.android.common.redux.android.ReduxViewController
import mypoli.android.common.view.*
import mypoli.android.common.view.changehandler.CircularRevealChangeHandler
import mypoli.android.quest.agenda.AgendaViewController
import mypoli.android.quest.calendar.CalendarViewState.DatePickerState.*
import mypoli.android.quest.calendar.CalendarViewState.StateType.*
import mypoli.android.quest.calendar.addquest.AddQuestViewController
import mypoli.android.quest.calendar.dayview.view.DayViewController
import org.threeten.bp.LocalDate
import sun.bob.mcalendarview.CellConfig
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.listeners.OnMonthScrollListener
import sun.bob.mcalendarview.vo.DateData

class CalendarViewController(args: Bundle? = null) :
    ReduxViewController<CalendarAction, CalendarViewState, CalendarReduxPresenter>(args) {

    override val presenter get() = CalendarReduxPresenter()

    private lateinit var calendarToolbar: ViewGroup

    private var dayViewPagerAdapter: DayViewPagerAdapter? = null

    private val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            dispatch(CalendarAction.SwipeChangeDate(position))
        }
    }

    private val dummyAdapter: PagerAdapter = object : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any) = false
        override fun getCount() = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.controller_calendar, container, false)

        initAddQuest(view)

        calendarToolbar = addToolbarView(R.layout.view_calendar_toolbar) as ViewGroup

        initDayPicker(view, calendarToolbar)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.calendar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {

            R.id.actionAgenda -> {
                val handler = FadeChangeHandler()
                val childRouter = getChildRouter(view!!.contentContainer, null)
                if (!childRouter.hasRootController()) {
                    childRouter.setRoot(
                        RouterTransaction.with(AgendaViewController())
                            .pushChangeHandler(handler)
                            .popChangeHandler(handler)
                    )
                }
                item.setIcon(R.drawable.ic_event_white_24dp)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    private fun initAddQuest(view: View) {
        view.addContainerBackground.setOnClickListener {
            addContainerRouter(view).popCurrentController()
            ViewUtils.hideKeyboard(view)
            closeAddContainer()
        }
    }

    private fun openAddContainer(currentDate: LocalDate) {
        val addContainer = view!!.addContainer

        val fab = view!!.addQuest

        val halfWidth = addContainer.width / 2

        val fabSet = createFabAnimator(fab, halfWidth.toFloat() - fab.width / 2)
        fabSet.start()

        fabSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                addContainer.visibility = View.VISIBLE
                fab.visibility = View.INVISIBLE

                animateShowAddContainer()

                val handler = CircularRevealChangeHandler(
                    addContainer,
                    addContainer,
                    duration = shortAnimTime
                )
                val childRouter = addContainerRouter(view!!)
                val addQuestViewController = AddQuestViewController({
                    childRouter.popCurrentController()
                    closeAddContainer()
                }, currentDate)

                childRouter.setRoot(
                    RouterTransaction.with(addQuestViewController)
                        .pushChangeHandler(handler)
                        .popChangeHandler(handler)
                )
            }
        })
    }

    private fun addContainerRouter(view: View) =
        getChildRouter(view.addContainer, "add-quest")

    private fun animateShowAddContainer() {
        val addContainerBackground = view!!.addContainerBackground
        addContainerBackground.alpha = 0f
        addContainerBackground.visibility = View.VISIBLE
        addContainerBackground.animate().alpha(1f).setDuration(longAnimTime).start()
    }

    private fun closeAddContainer() {
        view!!.addContainerBackground.visibility = View.GONE
        val duration = view!!.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        val addContainer = view!!.addContainer
        val fab = view!!.addQuest

        val revealAnim = RevealAnimator().createWithEndRadius(
            view = addContainer,
            endRadius = (fab.width / 2).toFloat(),
            reverse = true
        )
        revealAnim.duration = duration
        revealAnim.startDelay = 300
        revealAnim.interpolator = AccelerateDecelerateInterpolator()
        revealAnim.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                if (view == null) {
                    return
                }
                addContainer.visibility = View.INVISIBLE
                view!!.addContainer.requestFocus()
                fab.visibility = View.VISIBLE

                val fabSet = createFabAnimator(
                    fab,
                    (addContainer.width - fab.width - ViewUtils.dpToPx(16f, fab.context)),
                    reverse = true
                )
                fabSet.start()

            }

        })
        revealAnim.start()
    }

    private fun createFabAnimator(
        fab: FloatingActionButton,
        x: Float,
        reverse: Boolean = false
    ): AnimatorSet {
        val duration = view!!.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        val fabTranslation = ObjectAnimator.ofFloat(fab, "x", x)

        val fabColor = attr(R.attr.colorAccent)
        val primaryColor = attr(R.attr.colorPrimary)

        val startColor = if (reverse) primaryColor else fabColor
        val endColor = if (reverse) fabColor else primaryColor

        val rgbAnim = ObjectAnimator.ofArgb(
            fab,
            "backgroundTint",
            startColor, endColor
        )
        rgbAnim.addUpdateListener({ animation ->
            val value = animation.animatedValue as Int
            fab.backgroundTintList = ColorStateList.valueOf(value)
        })

        return AnimatorSet().also {
            it.playTogether(fabTranslation, rgbAnim)
            it.interpolator = AccelerateDecelerateInterpolator()
            it.duration = duration
        }
    }

    private fun initDayPicker(view: View, calendarToolbar: ViewGroup) {
        view.datePickerContainer.visibility = View.GONE

        view.datePicker.setMarkedStyle(MarkStyle.BACKGROUND, attr(R.attr.colorAccent))

        val currentDate = LocalDate.now()
        view.datePicker.markDate(
            DateData(
                currentDate.year,
                currentDate.monthValue,
                currentDate.dayOfMonth
            )
        )

        calendarToolbar.dispatchOnClick(CalendarAction.ExpandToolbar)
        view.expander.dispatchOnClick(CalendarAction.ExpandWeekToolbar)

        view.datePicker.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(v: View, date: DateData) {
                dispatch(CalendarAction.CalendarChangeDate(date.year, date.month, date.day))
            }
        })

        view.datePicker.setOnMonthScrollListener(object : OnMonthScrollListener() {
            override fun onMonthChange(year: Int, month: Int) {
                dispatch(CalendarAction.ChangeMonth(year, month))
            }

            override fun onMonthScroll(positionOffset: Float) {
            }

        })
    }

    override fun render(state: CalendarViewState, view: View) {

        val levelProgress = view.levelProgress

        calendarToolbar.day.text = state.dayText
        calendarToolbar.date.text = state.dateText
        view.currentMonth.text = state.monthText

        view.addQuest.setOnClickListener {
            openAddContainer(state.currentDate)
        }

        when (state.type) {

            LOADING -> levelProgress.visible = false

            XP_AND_COINS_CHANGED -> {
                levelProgress.visible = true
                val animator = ObjectAnimator.ofInt(
                    levelProgress,
                    "progress",
                    levelProgress.progress,
                    state.progress
                )
                animator.duration = shortAnimTime
                animator.start()
                calendarToolbar.playerGems.text = state.coins.toString()
            }

            LEVEL_CHANGED -> {
                levelProgress.max = state.maxProgress
                levelProgress.progress = state.progress
                calendarToolbar.playerLevel.text =
                    resources!!.getString(R.string.player_level, state.level)
            }

            DATE_PICKER_CHANGED -> renderDatePicker(state.datePickerState, view, state.currentDate)

            INITIAL -> {
                removeDayViewPagerAdapter(view)
                createDayViewPagerAdapter(state, view)
                levelProgress.visible = true
                levelProgress.max = state.maxProgress
                levelProgress.progress = state.progress
                calendarToolbar.playerLevel.text =
                    resources!!.getString(R.string.player_level, state.level)
                calendarToolbar.playerGems.text = state.coins.toString()
            }

            DATA_CHANGED -> {
                levelProgress.visible = true
                levelProgress.max = state.maxProgress
                levelProgress.progress = state.progress
                calendarToolbar.playerLevel.text =
                    resources!!.getString(R.string.player_level, state.level)
                calendarToolbar.playerGems.text = state.coins.toString()
            }

            CALENDAR_DATE_CHANGED -> {
                markSelectedDate(view, state.currentDate)
                removeDayViewPagerAdapter(view)
                createDayViewPagerAdapter(state, view)
            }

            SWIPE_DATE_CHANGED -> {
                markSelectedDate(view, state.currentDate)
                updateDayViewPagerAdapter(state)
            }
        }
    }

    private fun renderDatePicker(
        datePickerState: CalendarViewState.DatePickerState,
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
        calendarToolbar.calendarIndicator.animate().rotation(180f).duration = shortAnimTime
        val layoutParams = view.pager.layoutParams as ViewGroup.MarginLayoutParams
        CellConfig.Month2WeekPos = CellConfig.middlePosition
        CellConfig.ifMonth = false
        CellConfig.weekAnchorPointDate =
            DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
        view.datePicker.shrink()
        layoutParams.topMargin = ViewUtils.dpToPx(-14f, view.context).toInt()
        view.pager.layoutParams = layoutParams
        view.datePickerContainer.visibility = View.VISIBLE
        view.pager.layoutParams = layoutParams
        view.expander.setImageResource(R.drawable.ic_arrow_drop_down_white_24dp)
    }

    private fun showMonthDatePicker(view: View) {
        CellConfig.ifMonth = true
        CellConfig.Week2MonthPos = CellConfig.middlePosition
        view.datePicker.expand()
        view.expander.setImageResource(R.drawable.ic_arrow_drop_up_white_24dp)
    }

    private fun hideDatePicker(view: View, currentDate: LocalDate) {
        calendarToolbar.calendarIndicator.animate().rotation(0f).duration = shortAnimTime
        view.datePickerContainer.visibility = View.GONE
        val layoutParams = view.pager.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = 0
        view.pager.layoutParams = layoutParams

        CellConfig.Month2WeekPos = CellConfig.middlePosition
        CellConfig.ifMonth = false
        CellConfig.weekAnchorPointDate =
            DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
        view.datePicker.shrink()
    }

    private fun markSelectedDate(view: View, currentDate: LocalDate) {
        view.datePicker.markedDates.removeAdd()
        view.datePicker.markDate(
            DateData(
                currentDate.year,
                currentDate.monthValue,
                currentDate.dayOfMonth
            )
        )
    }

    private fun removeDayViewPagerAdapter(view: View) {
        view.pager.removeOnPageChangeListener(pageChangeListener)
        view.pager.adapter = dummyAdapter
    }

    private fun updateDayViewPagerAdapter(state: CalendarViewState) {
        dayViewPagerAdapter?.date = state.currentDate
        dayViewPagerAdapter?.pagerPosition = state.adapterPosition
    }

    private fun createDayViewPagerAdapter(state: CalendarViewState, view: View) {
        dayViewPagerAdapter = DayViewPagerAdapter(state.currentDate, state.adapterPosition, this)
        view.pager.adapter = dayViewPagerAdapter
        view.pager.currentItem = state.adapterPosition
        view.pager.addOnPageChangeListener(pageChangeListener)
    }

    override fun onDestroyView(view: View) {
        if (!activity!!.isChangingConfigurations) {
            view.pager.adapter = null
        }
        super.onDestroyView(view)
    }

    override fun onDestroy() {
        removeToolbarView(calendarToolbar)
        super.onDestroy()
    }

    class DayViewPagerAdapter(var date: LocalDate, var pagerPosition: Int, controller: Controller) :
        RouterPagerAdapter(controller) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val plusDays = position - pagerPosition
                val dayViewDate = date.plusDays(plusDays.toLong())
                val page = DayViewController(dayViewDate)
                router.setRoot(RouterTransaction.with(page))
            }
        }

        override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE

        override fun getCount() = MAX_VISIBLE_DAYS

        companion object {
            const val MAX_VISIBLE_DAYS = 100
        }

    }

    fun onStartEdit() {
        view!!.addQuest.visible = false
        view!!.pager.isLocked = true
    }

    fun onStopEdit() {
        view!!.addQuest.visible = true
        view!!.pager.isLocked = false
    }
}
