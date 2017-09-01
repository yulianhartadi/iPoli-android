package io.ipoli.android.quest.calendar.ui

import android.content.Context
import android.support.transition.TransitionManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import io.ipoli.android.R
import timber.log.Timber


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/2/17.
 */
class CalendarDayView : FrameLayout {

    private var hourHeight: Int = 0

    private enum class Mode {
        NONE,
        DRAG,
        ZOOM
    }


    private var mode = Mode.NONE
    private val scale = 1.0f
    private val lastScaleFactor = 0f

    // Where the finger first  touches the screen
    private var startX = 0f
    private var startY = 0f

    // How much to translate the canvas
    private var dx = 0f
    private var dy = 0f
    private var prevDx = 0f
    private var prevDy = 0f

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
        hourHeight = screenHeight / 6

        for (hour in 1..23) {
            val hourView = TextView(context)
            hourView.text = hour.toString()
            val layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, hourHeight)
            layoutParams.topMargin = (hour - 1) * hourHeight
            hourView.layoutParams = layoutParams
            addView(hourView)
        }

        val editModeBackground = View(context)
        editModeBackground.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        editModeBackground.setBackgroundResource(R.color.md_grey_700)
        editModeBackground.visibility = View.GONE
        addView(editModeBackground)

        val inflater = LayoutInflater.from(context)

        val questView = inflater.inflate(R.layout.item_calendar_quest, this, false)

        (questView.layoutParams as FrameLayout.LayoutParams).topMargin = 2 * hourHeight - (hourHeight / 2)

        addView(questView)

        questView.setOnLongClickListener {
            TransitionManager.beginDelayedTransition(this)
            editModeBackground.visibility = View.VISIBLE

            questView.setOnTouchListener { view, motionEvent ->

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mode = Mode.DRAG;

                        startX = motionEvent.x - prevDx
                        startY = motionEvent.y - prevDy
                    }

                    MotionEvent.ACTION_MOVE -> {
                        dx = motionEvent.getX() - startX
                        dy = motionEvent.getY() - startY

                        questView.top += dy.toInt()
                        questView.bottom += dy.toInt()
                    }

                    MotionEvent.ACTION_POINTER_UP -> {
                        mode = Mode.NONE
                    }

                    MotionEvent.ACTION_UP -> {
                        mode = Mode.NONE
                        prevDx = dx
                        prevDy = dy
                    }

                    else -> {

                    }
                }

                if (mode == Mode.DRAG) {
                    requestDisallowInterceptTouchEvent(true)

//                    val maxDx = child().getWidth() * (scale - 1)  // adjusted for zero pivot
//                    val maxDy = child().getHeight() * (scale - 1)  // adjusted for zero pivot
//                    dx = Math.min(Math.max(dx, -maxDx), 0)  // adjusted for zero pivot
//                    dy = Math.min(Math.max(dy, -maxDy), 0)  // adjusted for zero pivot
                }

                true
            }

            true
        }

        Timber.d(screenHeight.toString())
    }

    private fun getScreenHeight(): Int {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

}