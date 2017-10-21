package io.ipoli.android.quest.calendar

import android.content.Context
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.common.di.Module
import io.ipoli.android.common.mvi.MviViewController
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.iPoliApp
import io.ipoli.android.quest.calendar.dayview.view.DayViewController
import kotlinx.android.synthetic.main.controller_calendar.view.*
import kotlinx.android.synthetic.main.controller_calendar_toolbar.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required
import sun.bob.mcalendarview.CellConfig
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.listeners.OnMonthScrollListener
import sun.bob.mcalendarview.vo.DateData

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/8/17.
 */
class CalendarViewController :
    MviViewController<CalendarViewState, CalendarViewController, CalendarPresenter, CalendarIntent>,
    Injects<Module>,
    ViewStateRenderer<CalendarViewState> {


    private val presenter by required { calendarPresenter }

    private lateinit var calendarToolbar: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {

        val view = inflater.inflate(R.layout.controller_calendar, container, false)

        view.pager.adapter = pagerAdapter
        view.pager.currentItem = Companion.MID_POSITION

        val toolbar = activity!!.findViewById<Toolbar>(R.id.toolbar)
        calendarToolbar = inflater.inflate(R.layout.controller_calendar_toolbar, toolbar, false) as ViewGroup
        toolbar.addView(calendarToolbar)

        initDayPicker(view, calendarToolbar)

        view.pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {

//                view.calendar.visibility = if (Random().nextDouble() > 0.5) View.GONE else View.VISIBLE
            }
        })

        return view
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        send(LoadDataIntent(LocalDate.now()))
    }

    private var pickerState = 0

    private var currentMidDate = LocalDate.now()

    constructor() : super()

    protected constructor(args: Bundle) : super(args)

    private val pagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val plusDays = position - Companion.MID_POSITION
                val dayViewDate = currentMidDate.plusDays(plusDays.toLong())
                val page = DayViewController(dayViewDate)
                router.setRoot(RouterTransaction.with(page))
            }
        }

        override fun getCount(): Int = Companion.MAX_VISIBLE_DAYS

        override fun getItemPosition(item: Any?): Int =
            PagerAdapter.POSITION_NONE
    }

    private fun changeCurrentDay(date: LocalDate) {
        currentMidDate = date
        pagerAdapter.notifyDataSetChanged()
        view!!.pager.setCurrentItem(Companion.MID_POSITION, false)
    }

    private fun initDayPicker(view: View, calendarToolbar: ViewGroup) {
        val monthPattern = DateTimeFormatter.ofPattern("MMMM")
        view.dayPickerContainer.visibility = View.GONE
        val calendarIndicator = calendarToolbar.calendarIndicator

        var currentDate = LocalDate.now()
        view.dayPicker.markDate(DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth))
        var isOpen = false

        calendarToolbar.setOnClickListener {
            calendarIndicator.animate().rotationBy(180f).duration = 200
            view.currentMonth.text = LocalDate.now().format(monthPattern)

            val layoutParams = view.pager.layoutParams as ViewGroup.MarginLayoutParams
            if (!isOpen) {
                CellConfig.Month2WeekPos = CellConfig.middlePosition
                CellConfig.ifMonth = false
                view.dayPicker.shrink()
                isOpen = true
                layoutParams.topMargin = ViewUtils.dpToPx(-12f, view.context).toInt()
                view.pager.layoutParams = layoutParams
                view.dayPickerContainer.visibility = View.VISIBLE
            } else {
                view.dayPickerContainer.visibility = View.GONE
                isOpen = false
                layoutParams.topMargin = 0
            }
            view.pager.layoutParams = layoutParams
        }

        view.expander.setOnClickListener({

            if (CellConfig.ifMonth) {
                CellConfig.Month2WeekPos = CellConfig.middlePosition
                CellConfig.ifMonth = false
                CellConfig.weekAnchorPointDate = DateData(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
                view.dayPicker.shrink()
            } else {
                CellConfig.ifMonth = true
                CellConfig.Week2MonthPos = CellConfig.middlePosition
                view.dayPicker.expand()
            }
        })


        view.dayPicker.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(v: View, date: DateData) {
                view.dayPicker.markedDates.removeAdd()
                view.dayPicker.markDate(date)
                currentDate = LocalDate.of(date.year, date.month, date.day)
                view.currentMonth.text = currentDate.format(monthPattern)
            }
        })

        view.dayPicker.setOnMonthScrollListener(object : OnMonthScrollListener() {
            override fun onMonthChange(year: Int, month: Int) {
                val localDate = LocalDate.of(year, month, 1)
                view.currentMonth.text = localDate.format(monthPattern)
            }

            override fun onMonthScroll(positionOffset: Float) {
            }

        })
    }

    override fun createPresenter() = presenter

    override fun render(state: CalendarViewState, view: View) {
        calendarToolbar.day.text = state.dayText
        calendarToolbar.date.text = state.dateText
    }

    override fun onDestroyView(view: View) {
        if (!activity!!.isChangingConfigurations) {
            view.pager.adapter = null
        }
        super.onDestroyView(view)
    }

    override fun onContextAvailable(context: Context) {
        inject(iPoliApp.module(context, router))
    }

    companion object {
        const val MID_POSITION = 49
        const val MAX_VISIBLE_DAYS = 100
    }
}