package io.ipoli.android.quest.calendar.ui

import android.content.Context
import android.database.DataSetObserver
import android.support.transition.TransitionManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.Adapter
import android.widget.FrameLayout
import io.ipoli.android.R
import kotlinx.android.synthetic.main.calendar_hour_cell.view.*


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
class CalendarDayView : FrameLayout {

    private var hourHeight: Float = 0f
    private var minuteHeight: Float = 0f

    private val adapterViews = mutableListOf<View>()

    private enum class Mode {
        NONE,
        DRAG,
        ZOOM
    }

    private lateinit var editModeBackground: View

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

        for (hour in 0..23) {

            val hourView = LayoutInflater.from(context).inflate(R.layout.calendar_hour_cell, this, false)
            if (hour > 0) {
                hourView.timeLabel.text = hour.toString()
            }
            val layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, hourHeight.toInt())
            layoutParams.topMargin = (hour * hourHeight).toInt()
            hourView.layoutParams = layoutParams
            addView(hourView)
        }

        editModeBackground = View(context)
        editModeBackground.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        editModeBackground.setBackgroundResource(R.color.md_dark_text_26)
        editModeBackground.visibility = View.GONE
        addView(editModeBackground)
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
            addView(adapterView)
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
                addView(a.getView(i, null, this), i)
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

    fun startEditMode(position: Int) {
        requestDisallowInterceptTouchEvent(true)
        editModeBackground.bringToFront()
        val adapterView = adapterViews[position]
        adapterView.bringToFront()
        TransitionManager.beginDelayedTransition(this)
        editModeBackground.visibility = View.VISIBLE
        adapterView.setOnTouchListener { v, e ->

//            setOnTouchListener { view, motionEvent ->
//                adapter?.onStopEdit(position)
//                adapterView.setOnTouchListener(null)
//                editModeBackground.setOnTouchListener(null)
//                setOnTouchListener(null)
//                requestDisallowInterceptTouchEvent(false)
//                TransitionManager.beginDelayedTransition(this)
//                editModeBackground.visibility = View.GONE
//                true
//            }

            editModeBackground.setOnTouchListener { view, motionEvent ->
                adapter?.onStopEdit(position)
                adapterView.setOnTouchListener(null)
                editModeBackground.setOnTouchListener(null)
//                setOnTouchListener(null)
                requestDisallowInterceptTouchEvent(false)
                TransitionManager.beginDelayedTransition(this)
                editModeBackground.visibility = View.GONE
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
        adapter?.onStartEdit(position)
    }

}