package io.ipoli.android.quest.schedule.calendar

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.redux.android.ReduxViewController
import io.ipoli.android.common.view.recyclerview.SimpleViewHolder
import io.ipoli.android.quest.schedule.ScheduleViewController
import io.ipoli.android.quest.schedule.calendar.dayview.view.DayViewController
import io.ipoli.android.quest.schedule.calendar.dayview.view.widget.CalendarDayView
import kotlinx.android.synthetic.main.calendar_hour_cell.view.*
import kotlinx.android.synthetic.main.controller_calendar.view.*
import kotlinx.android.synthetic.main.controller_day_view.view.*
import org.threeten.bp.LocalDate
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/31/2018.
 */
class CalendarViewController(args: Bundle? = null) :
    ReduxViewController<CalendarAction, CalendarViewState, CalendarReducer>(args) {

    override val reducer = CalendarReducer

//    private var dayViewPagerAdapter: DayViewPagerAdapter? = null

    private val pageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {
            dispatch(CalendarAction.SwipeChangeDate(position))
        }
    }

    private val snapHelper = object : LinearSnapHelper() {
        override fun findTargetSnapPosition(
            layoutManager: RecyclerView.LayoutManager?,
            velocityX: Int,
            velocityY: Int
        ): Int {
            val pos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
            if (pos != RecyclerView.NO_POSITION) {
                Timber.d("AAA $pos")
            }
            return pos
        }
    }

    private val dummyAdapter: PagerAdapter = object : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any) = false
        override fun getCount() = 0
    }

    private var startDate = LocalDate.now()

    constructor(startDate: LocalDate) : this() {
        this.startDate = startDate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.controller_calendar, container, false)
        val layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        view.pager.layoutManager =
            layoutManager
        view.pager.adapter = DayViewAdapter()
        view.pager.scrollToPosition(50)
//        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(view.pager)
        return view
    }

    override fun render(state: CalendarViewState, view: View) =
        when (state.type) {
            CalendarViewState.StateType.INITIAL -> {
                removeDayViewPagerAdapter(view)
                createDayViewPagerAdapter(state, view)
            }

            CalendarViewState.StateType.CALENDAR_DATE_CHANGED -> {
                removeDayViewPagerAdapter(view)
                createDayViewPagerAdapter(state, view)
            }

            CalendarViewState.StateType.SWIPE_DATE_CHANGED -> {
                updateDayViewPagerAdapter(state)
                dispatch(CalendarAction.ChangeVisibleDate(state.currentDate))
            }

            else -> {
            }
        }

    override fun onCreateLoadAction() = CalendarAction.Load(startDate)

    private fun removeDayViewPagerAdapter(view: View) {
//        view.pager.removeOnPageChangeListener(pageChangeListener)
//        view.pager.adapter = dummyAdapter
    }

    private fun updateDayViewPagerAdapter(state: CalendarViewState) {
//        dayViewPagerAdapter?.date = state.currentDate
//        dayViewPagerAdapter?.pagerPosition = state.adapterPosition
    }

    private fun createDayViewPagerAdapter(state: CalendarViewState, view: View) {
//        dayViewPagerAdapter =
//            CalendarViewController.DayViewPagerAdapter(
//                state.currentDate,
//                state.adapterPosition,
//                this
//            )
//        view.pager.adapter = dayViewPagerAdapter
//        view.pager.currentItem = state.adapterPosition
//        view.pager.addOnPageChangeListener(pageChangeListener)
    }

    override fun onDestroyView(view: View) {
        if (!activity!!.isChangingConfigurations) {
            view.pager.adapter = null
        }
        super.onDestroyView(view)
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

        override fun getCount() =
            MAX_VISIBLE_DAYS

        companion object {
            const val MAX_VISIBLE_DAYS = 100
        }
    }

    inner class DayViewAdapter : RecyclerView.Adapter<SimpleViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SimpleViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.controller_day_view,
                    parent,
                    false
                )
            )

        override fun getItemCount() = 100

        override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {

            val view = holder.itemView

            view.calendar.setCalendarChangeListener(object : CalendarDayView.CalendarChangeListener{
                override fun onCalendarReady() {
                    view.calendar.scrollToNow()
                }

                override fun onStartEditNewScheduledEvent(startTime: Time, duration: Int) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDragViewClick(dragView: View) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onEventValidationError(dragView: View) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onMoveEvent(startTime: Time?, endTime: Time?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResizeEvent(startTime: Time?, endTime: Time?, duration: Int) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onZoomEvent(adapterView: View) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onAddEvent() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onEditCalendarEvent() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onEditUnscheduledCalendarEvent() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onEditUnscheduledEvent() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onRemoveEvent(eventId: String) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            })
            view.calendar.setHourAdapter(object : CalendarDayView.HourCellAdapter {
                override fun bind(view: View, hour: Int) {
                    if (hour > 0) {
                        view.timeLabel.text = Time.atHours(hour).toString(shouldUse24HourFormat)
                    }
                }
            })

//            Timber.d("AAA binding ${holder.adapterPosition}")
        }

    }

    fun onStartEdit() {
//        view!!.pager.isLocked = true
        (parentController as ScheduleViewController).onStartEdit()
    }

    fun onStopEdit() {
//        view!!.pager.isLocked = false
        (parentController as ScheduleViewController).onStopEdit()
    }


}