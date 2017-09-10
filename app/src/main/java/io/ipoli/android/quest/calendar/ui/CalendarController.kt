package io.ipoli.android.quest.calendar.ui

import android.support.transition.TransitionManager
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import io.ipoli.android.quest.calendar.DayViewController
import kotlinx.android.synthetic.main.controller_calendar.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import sun.bob.mcalendarview.CellConfig
import sun.bob.mcalendarview.listeners.OnDateClickListener
import sun.bob.mcalendarview.listeners.OnMonthScrollListener
import sun.bob.mcalendarview.vo.DateData

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/8/17.
 */
class CalendarController : Controller() {

    val MID_POSITION = 49
    val MAX_VISIBLE_DAYS = 100

    private var pickerState = 0

    private var currentMidDate = LocalDate.now()

    private val pagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val plusDays = position - MID_POSITION

                val page = DayViewController()
                router.setRoot(RouterTransaction.with(page))
            }
        }

        override fun getCount(): Int = MAX_VISIBLE_DAYS

        override fun getItemPosition(item: Any?): Int =
            PagerAdapter.POSITION_NONE
    }

    private fun changeCurrentDay(date: LocalDate) {
        currentMidDate = date
        pagerAdapter.notifyDataSetChanged()
        view!!.pager.setCurrentItem(MID_POSITION, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.controller_calendar, container, false)

        view.pager.adapter = pagerAdapter
        view.pager.currentItem = MID_POSITION

        val toolbar = activity!!.findViewById<Toolbar>(R.id.toolbar)

//        view.dayPicker.markDate(2017, 9, 12)
        initDayPicker(view, toolbar)

//        calendarAdapter.switchToWeek(monthPager.rowIndex)

//
//        val appBarLayout = activity!!.findViewById<AppBarLayout>(R.id.appbar)
//        toolbar.title = "Friday Sept 8th 2017"
//        val lp = toolbar.layoutParams as ViewGroup.MarginLayoutParams
//        val window = activity!!.window
//        toolbar.setOnClickListener {
//            TransitionManager.beginDelayedTransition(appBarLayout)
//            val existingView = toolbar.findViewWithTag<View>("calendar")
//            if (existingView == null) {
//
//
//
//// clear FLAG_TRANSLUCENT_STATUS flag:
////                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
////
////// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
////                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//
//// finally change the color
//                window.statusBarColor = ContextCompat.getColor(activity, R.color.md_white)
//
//                lp.marginStart = ViewUtils.dpToPx(-32f, view.context).toInt()
//                toolbar.layoutParams = lp
//                appBarLayout.setBackgroundResource(R.color.md_white)
//                val calendarView = inflater.inflate(R.layout.toolbar_calendar, toolbar, false)
//                calendarView.tag = "calendar"
//                calendarView.toolbarTitle.text = toolbar.title
//                toolbar.addView(calendarView)
//            } else {
//
//                lp.marginStart = ViewUtils.dpToPx(0f, view.context).toInt()
//                toolbar.layoutParams = lp
//                toolbar.removeView(existingView)
//                appBarLayout.setBackgroundResource(R.color.colorPrimary)
//                window.statusBarColor = ContextCompat.getColor(activity, R.color.colorPrimaryDark)
//            }
//        }

        view.pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {

//                view.calendar.visibility = if (Random().nextDouble() > 0.5) View.GONE else View.VISIBLE
            }
        })

        return view
    }

    private fun initDayPicker(view: View, toolbar: Toolbar) {
        view.dayPickerContainer.visibility = View.GONE
        val dayPicker = view.dayPicker

        dayPicker.weekColumnView

        toolbar.setOnClickListener {

            view.currentMonth.text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM"))

            TransitionManager.beginDelayedTransition(view.dayPickerContainer)

            val layoutParams = view.pager.layoutParams as ViewGroup.MarginLayoutParams
            if (pickerState == 0) {
                CellConfig.Month2WeekPos = CellConfig.middlePosition
                CellConfig.ifMonth = false
                dayPicker.shrink()
                pickerState = 1
                layoutParams.topMargin = ViewUtils.dpToPx(-12f, view.context).toInt()
                view.pager.layoutParams = layoutParams
                view.dayPickerContainer.visibility = View.VISIBLE
            } else {
                view.dayPickerContainer.visibility = View.GONE
                pickerState = 0
                layoutParams.topMargin = 0
            }
            view.pager.layoutParams = layoutParams
        }

        view.expander.setOnClickListener({
            CellConfig.Week2MonthPos = CellConfig.middlePosition
            if (CellConfig.ifMonth) {
                CellConfig.ifMonth = false
                dayPicker.shrink()
            } else {
                CellConfig.ifMonth = true
                dayPicker.expand()
            }
        })

        dayPicker.setOnDateClickListener(object : OnDateClickListener() {
            override fun onDateClick(v: View, date: DateData) {
                dayPicker.markedDates.removeAdd()
                dayPicker.markDate(date)
                val localDate = LocalDate.of(date.year, date.month, date.day)
                view.currentMonth.text = localDate.format(DateTimeFormatter.ofPattern("MMMM"))
            }
        })

        dayPicker.setOnMonthScrollListener(object : OnMonthScrollListener() {
            override fun onMonthChange(year: Int, month: Int) {
                val localDate = LocalDate.of(year, month, 1)
                view.currentMonth.text = localDate.format(DateTimeFormatter.ofPattern("MMMM"))
            }

            override fun onMonthScroll(positionOffset: Float) {
            }

        })
    }

    override fun onDestroyView(view: View) {
        if (!activity!!.isChangingConfigurations) {
            view.pager.adapter = null
        }
        super.onDestroyView(view)
    }
}