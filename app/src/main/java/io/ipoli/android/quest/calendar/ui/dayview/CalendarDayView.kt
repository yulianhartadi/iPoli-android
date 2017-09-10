package io.ipoli.android.quest.calendar.ui.dayview

import android.content.Context
import android.content.res.Resources
import android.database.DataSetObserver
import android.graphics.drawable.Drawable
import android.support.transition.TransitionManager
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import kotlinx.android.synthetic.main.calendar_hour_cell.view.*
import kotlinx.android.synthetic.main.view_calendar_day.view.*
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
class CalendarDayView : LinearLayout {

    private val MIN_EVENT_DURATION = 10
    private val MAX_EVENT_DURATION = Time.h2Min(4)
    private var hourHeight: Float = 0f
    private var minuteHeight: Float = 0f
    private lateinit var dragImage: Drawable
    private var dragImageSize: Int = toPx(16)
    private val adapterViews = mutableListOf<View>()

    private lateinit var editModeBackground: View
    private lateinit var topDragView: View
    private lateinit var bottomDragView: View
    private lateinit var positionToTimeMapper: PositionToTimeMapper

    private var calendarAdapter: CalendarAdapter<*>? = null
    private var unscheduledEventsAdapter: UnscheduledEventsAdapter<*>? = null

    private val dataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            refreshEventsFromAdapter()
        }

        override fun onInvalidated() {
            removeAllViews()
        }
    }

    constructor(context: Context) : super(context) {
        initUi(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initUi(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initUi(attrs, defStyleAttr)
    }

    private fun initUi(attrs: AttributeSet?, defStyleAttr: Int) {
        setMainLayout()
        fetchStyleAttributes(attrs, defStyleAttr)
        orientation = VERTICAL
        val screenHeight = getScreenHeight()
        hourHeight = screenHeight / 6f
        minuteHeight = hourHeight / 60f

        positionToTimeMapper = PositionToTimeMapper(minuteHeight)

        setupScroll()
        setupHourCells()
        setupEditBackgroundView()
        setupUnscheduledQuests()

        topDragView = addDragView()
        bottomDragView = addDragView()
    }

    private fun setupScroll() {
        scrollView.isVerticalScrollBarEnabled = false
    }

    private fun setMainLayout() {
        LayoutInflater.from(context).inflate(R.layout.view_calendar_day, this, true)
    }

    private fun fetchStyleAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarDayView, defStyleAttr, 0)
            dragImage = a.getDrawable(R.styleable.CalendarDayView_dragImage)
            dragImageSize = a.getDimensionPixelSize(R.styleable.CalendarDayView_dragImageSize, dragImageSize)
            a.recycle()
        }
    }

    private fun setupEditBackgroundView() {
        editModeBackground = View(context)
        editModeBackground.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        editModeBackground.setBackgroundResource(R.color.md_dark_text_26)
        editModeBackground.visibility = View.GONE
        eventContainer.addView(editModeBackground)
    }

    private fun setupHourCells() {
        for (hour in 0..23) {
            val hourView = LayoutInflater.from(context).inflate(R.layout.calendar_hour_cell, this, false)
            if (hour > 0) {
                hourView.timeLabel.text = hour.toString()
            }
            val layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, hourHeight.toInt())
            layoutParams.topMargin = (hour * hourHeight).toInt()
            hourView.layoutParams = layoutParams
            eventContainer.addView(hourView)
        }
    }

    private fun addDragView(): View {
        val view = ImageView(context)
        view.layoutParams = LayoutParams(dragImageSize, dragImageSize)
        view.setImageDrawable(dragImage)
        view.visibility = View.GONE
        eventContainer.addView(view)
        return view
    }

    private fun setupUnscheduledQuests() {
        unscheduledQuests.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        unscheduledQuests.isNestedScrollingEnabled = false
    }

    fun setUnscheduledQuestsAdapter(adapter: UnscheduledEventsAdapter<*>) {
        unscheduledEventsAdapter = adapter
        unscheduledQuests.adapter = adapter
    }

    fun setCalendarAdapter(adapter: CalendarAdapter<*>) {
        this.calendarAdapter?.unregisterDataSetObserver(dataSetObserver)
        this.calendarAdapter = adapter
        this.calendarAdapter?.registerDataSetObserver(dataSetObserver)
        addEventsFromAdapter()
    }

    private fun addEventsFromAdapter() {
//        removeAllViews()
        val a = calendarAdapter!!
        for (i in 0 until a.count) {
            val adapterView = a.getView(i, null, eventContainer)
            val event = a.getItem(i)
            adapterView.setPositionAndHeight(
                event.startMinute * minuteHeight,
                (event.duration * minuteHeight).toInt())
            adapterViews.add(i, adapterView)
            eventContainer.addView(adapterView)
        }
    }

    private fun refreshEventsFromAdapter() {
        val a = calendarAdapter!!
        val eventsInViewCount = childCount
        val eventsInAdapterCount = a.count
        val reuseCount = Math.min(eventsInViewCount, eventsInAdapterCount)

        for (i in 0 until reuseCount) {
            a.getView(i, getChildAt(i), eventContainer)
        }

        if (eventsInViewCount < eventsInAdapterCount) {
            for (i in eventsInViewCount until eventsInAdapterCount) {
                eventContainer.addView(a.getView(i, null, eventContainer), i)
            }
        } else if (eventsInViewCount > eventsInAdapterCount) {
            removeViews(eventsInAdapterCount, eventsInViewCount)
        }
    }

    fun scheduleEvent(unscheduledEvent: UnscheduledEvent) {
        val eventPosition = unscheduledEventsAdapter?.events?.indexOf(unscheduledEvent)
    }

    fun startEditMode(editView: View, position: Int) {
        scrollView.locked = true
        editModeBackground.bringToFront()
        setupEditView(editView, position)
        setupTopDragView(editView)
        setupBottomDragView(editView)
        TransitionManager.beginDelayedTransition(this)
        showViews(editModeBackground, topDragView, bottomDragView)
        calendarAdapter?.onStartEdit(editView, position)
    }

    private fun setupEditView(editView: View, position: Int) {
        editView.bringToFront()
        setEditViewTouchListener(editView, position)
    }

    private fun setEditViewTouchListener(editView: View, position: Int) {
        var startY = -1f
        editView.setOnTouchListener { _, e ->
            setEditModeTouchListener(editView, position)
            val action = e.actionMasked

            if (action == MotionEvent.ACTION_DOWN) {
                startY = e.y
            }
            if (action == MotionEvent.ACTION_MOVE) {
                if (startY < 0) {
                    startY = e.y
                }
                val dy = e.y - startY
                onChangeEditViewPosition(editView, dy)
                calendarAdapter?.onStartTimeChanged(editView, position, positionToTimeMapper.timeAt((editView.layoutParams as MarginLayoutParams).topMargin.toFloat(), 5))
            }
            true
        }
    }

    private fun setupBottomDragView(editView: View) {
        bottomDragView.bringToFront()
        positionBottomDragView(editView)
        setBottomDragViewListener(bottomDragView, editView)
    }

    private fun setupTopDragView(editView: View) {
        topDragView.bringToFront()
        positionTopDragView(editView)
        setTopDragViewListener(topDragView, editView)
    }

    private fun positionBottomDragView(editView: View) {
        val lp = bottomDragView.layoutParams as MarginLayoutParams
        lp.topMargin = editView.bottom - dragImageSize / 2
        lp.marginStart = editView.left + editView.width / 2
        bottomDragView.layoutParams = lp
    }

    private fun positionTopDragView(editView: View) {
        val lp = topDragView.layoutParams as MarginLayoutParams
        lp.topMargin = editView.top - dragImageSize / 2
        lp.marginStart = editView.left + editView.width / 2
        topDragView.layoutParams = lp
    }

    private fun onChangeEditViewPosition(editView: View, deltaY: Float) {
        editView.changePosition(deltaY)
        topDragView.changePosition(deltaY)
        bottomDragView.changePosition(deltaY)
    }

    private fun setEditModeTouchListener(editView: View, position: Int) {
        editModeBackground.setOnTouchListener { _, _ ->
            stopEditMode(editView, position)
            true
        }
    }

    private fun stopEditMode(editView: View, position: Int) {
        scrollView.locked = false
        editView.setPosition(getAdjustedYPosFor(editView, rangeLength = 5))
        editView.setOnTouchListener(null)
        editModeBackground.setOnTouchListener(null)
        TransitionManager.beginDelayedTransition(this)
        hideViews(editModeBackground, topDragView, bottomDragView)
        calendarAdapter?.onStopEdit(editView, position)
    }

    private fun setBottomDragViewListener(bottomDragView: View, editView: View) {
        var lastY = 0f
        bottomDragView.setOnTouchListener { _, e ->
            if (e.actionMasked == MotionEvent.ACTION_DOWN) {
                lastY = e.y
            }

            if (e.actionMasked == MotionEvent.ACTION_MOVE) {
                val dy = e.y - lastY
                val height = editView.height + dy.toInt()
                if (isValidHeightForEvent(height)) {
                    editView.changeHeight(height)
                    bottomDragView.changePosition(dy)
                    lastY = e.y
                }
            }

            true
        }
    }

    private fun setTopDragViewListener(topDragView: View, editView: View) {
        var lastY = 0f
        topDragView.setOnTouchListener { _, e ->
            if (e.actionMasked == MotionEvent.ACTION_DOWN) {
                lastY = e.y
            }

            if (e.actionMasked == MotionEvent.ACTION_MOVE) {
                val dy = e.y - lastY
                val height = editView.height - dy.toInt()
                if (isValidHeightForEvent(height)) {
                    editView.changePositionAndHeight(dy, height)
                    topDragView.changePosition(dy)
                    Timber.d("New start time " + positionToTimeMapper.timeAt((topDragView.layoutParams as MarginLayoutParams).topMargin.toFloat()))
                    lastY = e.y
                }
            }

            true
        }
    }

    private fun View.changePositionAndHeight(yDelta: Float, height: Int) =
        changeLayoutParams<MarginLayoutParams> {
            it.topMargin += yDelta.toInt()
            it.height = height
        }

    private fun View.setPositionAndHeight(yPosition: Float, height: Int) =
        changeLayoutParams<MarginLayoutParams> {
            it.topMargin = yPosition.toInt()
            it.height = height
        }

    private fun View.setPosition(yPosition: Float) =
        changeLayoutParams<MarginLayoutParams> { it.topMargin = yPosition.toInt() }

    private fun View.changePosition(yDelta: Float) =
        changePosition(yDelta.toInt())

    private fun View.changePosition(yDelta: Int) =
        changeLayoutParams<MarginLayoutParams> { it.topMargin += yDelta }

    private fun View.changeHeight(height: Int) =
        changeLayoutParams<MarginLayoutParams> { it.height = height }

    private fun <T : ViewGroup.LayoutParams> View.changeLayoutParams(cb: (layoutParams: T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val lp = layoutParams as T
        cb(lp)
        layoutParams = lp
    }

    private fun isValidHeightForEvent(height: Int): Boolean =
        getMinutesFor(height) in MIN_EVENT_DURATION..MAX_EVENT_DURATION

    private fun getAdjustedYPosFor(view: View, rangeLength: Int): Float =
        getYPositionFor(positionToTimeMapper.timeAt((view.layoutParams as MarginLayoutParams).topMargin.toFloat(), rangeLength))

    private fun getScreenHeight(): Int {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

    private fun getYPositionFor(time: Time): Float =
        time.hours * hourHeight + getMinutesHeight(time.getMinutes())

    private fun getMinutesHeight(minutes: Int): Float =
        minuteHeight * minutes

    private fun getMinutesFor(height: Int): Int =
        (height / minuteHeight).toInt()

    private fun showViews(vararg views: View) =
        views.forEach { it.visibility = View.VISIBLE }

    private fun hideViews(vararg views: View) =
        views.forEach { it.visibility = View.GONE }

    private fun toPx(dp: Int): Int =
        (dp * Resources.getSystem().displayMetrics.density).toInt()
}