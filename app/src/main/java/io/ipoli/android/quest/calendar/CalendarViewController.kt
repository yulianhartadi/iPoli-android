package io.ipoli.android.quest.calendar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.common.view.RevealAnimator
import io.ipoli.android.common.view.changehandler.CircularRevealChangeHandler
import io.ipoli.android.common.view.colorRes
import io.ipoli.android.common.view.intRes
import io.ipoli.android.common.view.visible
import io.ipoli.android.quest.calendar.CalendarViewState.DatePickerState.*
import io.ipoli.android.quest.calendar.CalendarViewState.StateType.*
import io.ipoli.android.quest.calendar.addquest.AddQuestViewController
import io.ipoli.android.quest.calendar.dayview.view.DayViewController
import kotlinx.android.synthetic.main.controller_calendar.view.*
import kotlinx.android.synthetic.main.controller_calendar_toolbar.view.*
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Injects
import space.traversal.kapsule.required
import sun.bob.mcalendarview.CellConfig
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.listeners.OnMonthScrollListener
import sun.bob.mcalendarview.vo.DateData

class CalendarViewController(args: Bundle? = null) :
    MviViewController<CalendarViewState, CalendarViewController, CalendarPresenter, CalendarIntent>(args),
    Injects<ControllerModule>,
    ViewStateRenderer<CalendarViewState> {

    companion object {
        const val MAX_VISIBLE_DAYS = 100
    }

    private val presenter by required { calendarPresenter }

    private lateinit var calendarToolbar: ViewGroup

    private var dayViewPagerAdapter: DayViewPagerAdapter? = null

    private val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            send(SwipeChangeDateIntent(position))
        }
    }

    private val dummyAdapter: PagerAdapter = object : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any) = false
        override fun getCount() = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {

        val view = inflater.inflate(R.layout.controller_calendar, container, false)

        val toolbar = activity!!.findViewById<Toolbar>(R.id.toolbar)
        calendarToolbar = inflater.inflate(R.layout.controller_calendar_toolbar, toolbar, false) as ViewGroup
        toolbar.addView(calendarToolbar)

        initDayPicker(view, calendarToolbar)

        initAddQuest(view)

        return view
    }

    private fun initAddQuest(view: View) {
        view.addQuest.setOnClickListener {
            openAddContainer()
        }
    }

    private fun openAddContainer() {
        val addContainer = view!!.addContainer
        val fab = view!!.addQuest

        val halfWidth = addContainer.width / 2

        val fabSet = createFabAnimator(fab, halfWidth.toFloat() - fab.width / 2)
        fabSet.start()

        fabSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                addContainer.visibility = View.VISIBLE
                fab.visibility = View.INVISIBLE
                val handler = CircularRevealChangeHandler(addContainer, addContainer, duration = 200)
                val childRouter = getChildRouter(view!!.addContainer, "add-quest")
                val addQuestViewController = AddQuestViewController()

                addQuestViewController.addLifecycleListener(object : LifecycleListener() {
                    override fun postDestroy(controller: Controller) {
                        super.postDestroy(controller)
                        closeAddContainer()
                    }

                })

                childRouter.setRoot(
                    RouterTransaction.with(addQuestViewController)
                        .pushChangeHandler(handler)
                        .popChangeHandler(handler)
                )

            }
        })
    }

    private fun closeAddContainer() {
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

    private fun createFabAnimator(fab: FloatingActionButton, x: Float, reverse: Boolean = false): AnimatorSet {
        val duration = view!!.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        val fabTranslation = ObjectAnimator.ofFloat(fab, "x", x)

        val fabColor = ContextCompat.getColor(fab.context, R.color.md_green_500)
        val whiteColor = ContextCompat.getColor(fab.context, R.color.md_white)

        val startColor = if (reverse) whiteColor else fabColor
        val endColor = if (reverse) fabColor else whiteColor

        val rgbAnim = ObjectAnimator.ofArgb(
            fab,
            "backgroundTint",
            startColor, endColor
        )
        rgbAnim.addUpdateListener({ animation ->
            val value = animation.animatedValue as Int
            fab.backgroundTintList = ColorStateList.valueOf(value)
        })

        val fabSet = AnimatorSet()
        fabSet.playTogether(fabTranslation, rgbAnim)
        fabSet.interpolator = AccelerateDecelerateInterpolator()
        fabSet.duration = duration
        return fabSet
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadDataIntent(LocalDate.now()))
    }

    private fun initDayPicker(view: View, calendarToolbar: ViewGroup) {
        view.datePickerContainer.visibility = View.GONE
        val calendarIndicator = calendarToolbar.calendarIndicator
        view.datePicker.setMarkedStyle(MarkStyle.BACKGROUND, colorRes(R.color.colorAccentAlternative))

        val currentDate = LocalDate.now()
        view.datePicker.markDate(DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth))

        calendarToolbar.setOnClickListener {
            calendarIndicator.animate().rotationBy(180f).duration = 200
            send(ExpandToolbarIntent)
        }

        view.expander.setOnClickListener {
            send(ExpandToolbarWeekIntent)
        }

        view.datePicker.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(v: View, date: DateData) {
                send(CalendarChangeDateIntent(date.year, date.month, date.day))
            }
        })

        view.datePicker.setOnMonthScrollListener(object : OnMonthScrollListener() {
            override fun onMonthChange(year: Int, month: Int) {
                send(ChangeMonthIntent(year, month))
            }

            override fun onMonthScroll(positionOffset: Float) {
            }

        })
    }

    override fun createPresenter() = presenter

    override fun render(state: CalendarViewState, view: View) {
        val levelProgress = view.levelProgress

        calendarToolbar.day.text = state.dayText
        calendarToolbar.date.text = state.dateText
        view.currentMonth.text = state.monthText

        if (state.type == LOADING) {
            levelProgress.visible = false
        }

        if (state.type == XP_CHANGED) {
            levelProgress.visible = true
            val animator = ObjectAnimator.ofInt(levelProgress, "progress", levelProgress.progress, state.progress)
            animator.duration = intRes(android.R.integer.config_shortAnimTime).toLong()
            animator.start()
        }

        if (state.type == LEVEL_CHANGED) {
            levelProgress.progress = state.progress
            levelProgress.max = state.maxProgress
            calendarToolbar.playerLevel.text = resources!!.getString(R.string.player_level, state.level)
        }

        if (state.type == DATE_PICKER_CHANGED) {
            renderDatePicker(state.datePickerState, view, state.currentDate)
        }

        if (state.type == DATA_LOADED) {
            removeDayViewPagerAdapter(view)
            createDayViewPagerAdapter(state, view)
            levelProgress.visible = true
            levelProgress.progress = state.progress
            levelProgress.max = state.maxProgress
            calendarToolbar.playerLevel.text = resources!!.getString(R.string.player_level, state.level)
        }

        if (state.type == CALENDAR_DATE_CHANGED) {
            markSelectedDate(view, state.currentDate)
            removeDayViewPagerAdapter(view)
            createDayViewPagerAdapter(state, view)
        }

        if (state.type == SWIPE_DATE_CHANGED) {
            markSelectedDate(view, state.currentDate)
            updateDayViewPagerAdapter(state)
        }
    }

    private fun renderDatePicker(datePickerState: CalendarViewState.DatePickerState, view: View, currentDate: LocalDate) {
        when (datePickerState) {
            SHOW_MONTH -> showMonthDatePicker(view)
            SHOW_WEEK -> showWeekDatePicker(view, currentDate)
            INVISIBLE -> hideDatePicker(view, currentDate)
        }
    }

    private fun showWeekDatePicker(view: View, currentDate: LocalDate) {
        val layoutParams = view.pager.layoutParams as ViewGroup.MarginLayoutParams
        CellConfig.Month2WeekPos = CellConfig.middlePosition
        CellConfig.ifMonth = false
        CellConfig.weekAnchorPointDate = DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
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
        view.datePickerContainer.visibility = View.GONE
        val layoutParams = view.pager.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = 0
        view.pager.layoutParams = layoutParams

        CellConfig.Month2WeekPos = CellConfig.middlePosition
        CellConfig.ifMonth = false
        CellConfig.weekAnchorPointDate = DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
        view.datePicker.shrink()
    }

    private fun markSelectedDate(view: View, currentDate: LocalDate) {
        view.datePicker.markedDates.removeAdd()
        view.datePicker.markDate(DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth))
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

        val toolbar = activity!!.findViewById<Toolbar>(R.id.toolbar)
        toolbar.removeView(calendarToolbar)

        super.onDestroyView(view)
    }

    class DayViewPagerAdapter(var date: LocalDate, var pagerPosition: Int, controller: Controller) : RouterPagerAdapter(controller) {
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

    }

    fun onStartEdit() {
        view!!.addQuest.visible = false
    }

    fun onStopEdit() {
        view!!.addQuest.visible = true
    }
}
