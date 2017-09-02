package io.ipoli.android.quest.calendar.ui

import android.content.Context
import android.database.DataSetObserver
import android.support.transition.TransitionManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.Adapter
import android.widget.FrameLayout
import android.widget.ScrollView
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import kotlinx.android.synthetic.main.calendar_hour_cell.view.*


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
class CalendarDayView : ScrollView {

    private var hourHeight: Float = 0f
    private var minuteHeight: Float = 0f

    private val adapterViews = mutableListOf<View>()

    private enum class Mode {
        NONE,
        DRAG,
        ZOOM
    }

    private lateinit var editModeBackground: View
    private lateinit var mainContainer: FrameLayout

    private var mode = Mode.NONE

    private var startY = 0f

    private var dy = 0f
    private var prevDy = 0f

    private var adapter: CalendarAdapter<*>? = null

    private val dataSetObserver = object : DataSetObserver() {

        override fun onChanged() {
            refreshViewsFromAdapter()
        }

        override fun onInvalidated() {
            removeAllViews()
        }
    }

    constructor(context: Context) : super(context) {
        initUi()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initUi()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initUi()
    }

    private fun initUi() {
        val screenHeight = getScreenHeight()
        hourHeight = screenHeight / 6f
        minuteHeight = hourHeight / 60f

        isVerticalScrollBarEnabled = false
        mainContainer = FrameLayout(context)
        mainContainer.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        addView(mainContainer)

        for (hour in 0..23) {

            val hourView = LayoutInflater.from(context).inflate(R.layout.calendar_hour_cell, this, false)
            if (hour > 0) {
                hourView.timeLabel.text = hour.toString()
            }
            val layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, hourHeight.toInt())
            layoutParams.topMargin = (hour * hourHeight).toInt()
            hourView.layoutParams = layoutParams
            mainContainer.addView(hourView)
        }

        editModeBackground = View(context)
        editModeBackground.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        editModeBackground.setBackgroundResource(R.color.md_dark_text_26)
        editModeBackground.visibility = View.GONE
        mainContainer.addView(editModeBackground)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    fun setAdapter(adapter: CalendarAdapter<*>) {
        this.adapter?.unregisterDataSetObserver(dataSetObserver)
        this.adapter = adapter
        this.adapter?.registerDataSetObserver(dataSetObserver)
        initViewsFromAdapter()
    }

    fun getAdapter(): Adapter? {
        return adapter
    }

    private fun initViewsFromAdapter() {
//        removeAllViews()
        val a = adapter!!
        for (i in 0 until a.count) {
            val adapterView = a.getView(i, null, this)
            val event = a.getItem(i)
            val layoutParams = adapterView.layoutParams as FrameLayout.LayoutParams
            layoutParams.topMargin = (event.startMinute * minuteHeight).toInt()
            layoutParams.height = (event.duration * minuteHeight).toInt()
            adapterViews.add(i, adapterView)
            mainContainer.addView(adapterView)
        }
    }

    private fun refreshViewsFromAdapter() {
        val childCount = childCount
        val a = adapter!!
        val adapterSize = a.count
        val reuseCount = Math.min(childCount, adapterSize)

        for (i in 0 until reuseCount) {
            a.getView(i, getChildAt(i), this)
        }

        if (childCount < adapterSize) {
            for (i in childCount until adapterSize) {
                mainContainer.addView(a.getView(i, null, this), i)
            }
        } else if (childCount > adapterSize) {
            removeViews(adapterSize, childCount)
        }
    }

    private fun getScreenHeight(): Int {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

    protected fun getHoursFor(y: Float): Int {
        val h = getRelativeY(y) / hourHeight
        return Math.min(h.toInt(), 23)
    }

    protected fun getMinutesFor(y: Float, rangeLength: Int): Int {
        var minutes = (getRelativeY(y) % hourHeight / minuteHeight).toInt()
        minutes = Math.max(0, minutes)
        val bounds = mutableListOf<Int>()
        var rangeStart = 0
        for (min in 0..59) {

            if (min % rangeLength == 0) {
                rangeStart = min
            }
            bounds.add(rangeStart)
        }
        return bounds.get(minutes)
    }

    private fun getRelativeY(y: Float): Float {
        val offsets = IntArray(2)
        getLocationOnScreen(offsets)
        return getRelativeY(y, offsets[1].toFloat())
    }

    private fun getRelativeY(y: Float, yOffset: Float): Float {
        return Math.max(0f, scrollY + y - yOffset)
    }


    private fun getYPositionFor(hours: Int, minutes: Int): Float {
        var y = hours * hourHeight
        y += getMinutesHeight(minutes)
        return y
    }

    private fun getYPositionFor(minutesAfterMidnight: Int): Float {
        val time = Time.of(minutesAfterMidnight)
        return getYPositionFor(time.hours, time.getMinutes())
    }

    protected fun getHeightFor(duration: Int): Int {
        return getMinutesHeight(duration).toInt()
    }

    private fun getMinutesHeight(minutes: Int): Float {
        return minuteHeight * minutes
    }

    fun getViewRawTop(v: View): Int {
        val loc = IntArray(2)
        v.getLocationInWindow(loc)
        return loc[1]
    }

    fun startEditMode(editView: View, position: Int) {
        requestDisallowInterceptTouchEvent(true)
        editModeBackground.bringToFront()
        val adapterView = adapterViews[position]
        adapterView.bringToFront()
        TransitionManager.beginDelayedTransition(this)
        editModeBackground.visibility = View.VISIBLE
        adapterView.setOnTouchListener { v, e ->

            editModeBackground.setOnTouchListener { view, motionEvent ->
                stopEditMode(adapterView, position)
                true
            }

            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    mode = Mode.DRAG
                    startY = e.y - prevDy
                }

                MotionEvent.ACTION_MOVE -> {
                    dy = e.y - startY

                    adapterView.top += dy.toInt()
                    adapterView.bottom += dy.toInt()
                    (adapterView.layoutParams as FrameLayout.LayoutParams).topMargin += dy.toInt()
                    val yPosition = getViewRawTop(adapterView).toFloat()
                    val hours = getHoursFor(yPosition)
                    val minutes = getMinutesFor(yPosition, 5)
                    adapter?.onStartTimeChanged(editView, position, Time.at(hours, minutes))
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    mode = Mode.NONE
                }

                MotionEvent.ACTION_UP -> {
                    mode = Mode.NONE
                    prevDy = dy
                }

                else -> {

                }
            }

//            if (mode == Mode.DRAG) {
//                requestDisallowInterceptTouchEvent(true)
//
////                    val maxDx = child().getWidth() * (scale - 1)  // adjusted for zero pivot
////                    val maxDy = child().getHeight() * (scale - 1)  // adjusted for zero pivot
////                    dx = Math.min(Math.max(dx, -maxDx), 0)  // adjusted for zero pivot
////                    dy = Math.min(Math.max(dy, -maxDy), 0)  // adjusted for zero pivot
//            }

            true
        }
        adapter?.onStartEdit(editView, position)
    }

    private fun stopEditMode(editView: View, position: Int) {

        val layoutParams = editView.layoutParams as FrameLayout.LayoutParams
        val h = getHoursFor(getViewRawTop(editView).toFloat())
        val m = getMinutesFor(getViewRawTop(editView).toFloat(), 5)
        layoutParams.topMargin = getYPositionFor(h, m).toInt()

        adapter?.onStopEdit(editView, position)
        editView.setOnTouchListener(null)
        editModeBackground.setOnTouchListener(null)
        //                setOnTouchListener(null)
        requestDisallowInterceptTouchEvent(false)
        TransitionManager.beginDelayedTransition(this)
        editModeBackground.visibility = View.GONE
    }

}