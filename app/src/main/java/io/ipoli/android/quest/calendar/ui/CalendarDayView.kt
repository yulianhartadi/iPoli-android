package io.ipoli.android.quest.calendar.ui

import android.content.Context
import android.content.res.Resources
import android.database.DataSetObserver
import android.support.transition.TransitionManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.Adapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import io.ipoli.android.R
import io.ipoli.android.common.datetime.Time
import kotlinx.android.synthetic.main.calendar_hour_cell.view.*

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
class CalendarDayView : ScrollView {
    private val MIN_EVENT_DURATION = 10
    private val MAX_EVENT_DURATION = Time.h2Min(4)

    private val DRAG_VIEW_SIZE = toPx(16)

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
    private lateinit var topDragView: View
    private lateinit var bottomDragView: View

    private var mode = Mode.NONE

    private var startY = 0f

    private var dy = 0f

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

        topDragView = createEditDragView()
        bottomDragView = createEditDragView()
        mainContainer.addView(topDragView)
        mainContainer.addView(bottomDragView)

    }

    private fun createEditDragView(): View {
        val view = ImageView(context)
        view.layoutParams = LayoutParams(DRAG_VIEW_SIZE, DRAG_VIEW_SIZE)
        view.setImageResource(R.drawable.circle_accent)
        view.visibility = View.GONE
        return view
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

    private fun getMinutesFor(height: Int): Int {
        return (height / minuteHeight).toInt()
    }

    fun getViewRawTop(v: View): Int {
        val loc = IntArray(2)
        v.getLocationInWindow(loc)
        return loc[1]
    }

    fun startEditMode(editView: View, position: Int) {
        setOnTouchListener { view, motionEvent -> true }
        editModeBackground.bringToFront()
        editView.bringToFront()
        topDragView.bringToFront()
        bottomDragView.bringToFront()
        TransitionManager.beginDelayedTransition(this)
        editModeBackground.visibility = View.VISIBLE

        val topLP = topDragView.layoutParams as LayoutParams
        topLP.topMargin = editView.top - DRAG_VIEW_SIZE / 2
        topLP.marginStart = editView.left + editView.width / 2
        topDragView.layoutParams = topLP
        topDragView.visibility = View.VISIBLE

        val bottomLP = bottomDragView.layoutParams as LayoutParams
        bottomLP.topMargin = editView.bottom - DRAG_VIEW_SIZE / 2
        bottomLP.marginStart = editView.left + editView.width / 2
        bottomDragView.layoutParams = bottomLP
        bottomDragView.visibility = View.VISIBLE

        setBottomDragViewListener(bottomDragView, editView)
        setTopDragViewListener(topDragView, editView)


        startY = -1f
        editView.setOnTouchListener { v, e ->
            editModeBackground.setOnTouchListener { view, motionEvent ->
                stopEditMode(editView, position)
                true
            }

            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    mode = Mode.DRAG
                    startY = e.y
                }

                MotionEvent.ACTION_MOVE -> {
                    if (startY < 0) {
                        startY = e.y
                    }
                    dy = e.y - startY
                    (editView.layoutParams as FrameLayout.LayoutParams).topMargin += dy.toInt()
                    (topDragView.layoutParams as FrameLayout.LayoutParams).topMargin += dy.toInt()
                    (bottomDragView.layoutParams as FrameLayout.LayoutParams).topMargin += dy.toInt()

                    val yPosition = getViewRawTop(editView).toFloat()
                    val hours = getHoursFor(yPosition)
                    val minutes = getMinutesFor(yPosition, 5)
                    adapter?.onStartTimeChanged(editView, position, Time.at(hours, minutes))
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    mode = Mode.NONE
                }

                MotionEvent.ACTION_UP -> {
                    mode = Mode.NONE
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

    private fun setBottomDragViewListener(bottomDragView: View, editView: View) {
        var lastY = 0f
        bottomDragView.setOnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                lastY = e.y
            }

            if (e.action == MotionEvent.ACTION_MOVE) {
                dy = e.y - lastY
                val height = editView.height + dy.toInt()
                if (isEventHeightValid(height)) {
                    val lp = editView.layoutParams as LayoutParams
                    lp.height = height
                    editView.layoutParams = lp
                    (bottomDragView.layoutParams as FrameLayout.LayoutParams).topMargin += dy.toInt()
                    lastY = e.y
                }
            }

            true
        }
    }

    private fun setTopDragViewListener(topDragView: View, editView: View) {
        var lastY = 0f
        topDragView.setOnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                lastY = e.y
            }

            if (e.action == MotionEvent.ACTION_MOVE) {
                dy = e.y - lastY
                val height = editView.height - dy.toInt()
                if (isEventHeightValid(height)) {
                    val lp = editView.layoutParams as LayoutParams
                    lp.topMargin += dy.toInt()
                    lp.height = height
                    editView.layoutParams = lp
                    (topDragView.layoutParams as FrameLayout.LayoutParams).topMargin += dy.toInt()
                    lastY = e.y
                }
            }

            true
        }
    }

    private fun isEventHeightValid(height: Int): Boolean =
        getMinutesFor(height) in MIN_EVENT_DURATION..MAX_EVENT_DURATION


    private fun stopEditMode(editView: View, position: Int) {
        setOnTouchListener(null)
        val layoutParams = editView.layoutParams as FrameLayout.LayoutParams
        val h = getHoursFor(getViewRawTop(editView).toFloat())
        val m = getMinutesFor(getViewRawTop(editView).toFloat(), 5)
        layoutParams.topMargin = getYPositionFor(h, m).toInt()

        adapter?.onStopEdit(editView, position)
        editView.setOnTouchListener(null)
        editModeBackground.setOnTouchListener(null)
        TransitionManager.beginDelayedTransition(this)
        editModeBackground.visibility = View.GONE
        topDragView.visibility = View.GONE
        bottomDragView.visibility = View.GONE
    }

    fun toPx(dp: Int): Int =
        (dp * Resources.getSystem().displayMetrics.density).toInt()

}