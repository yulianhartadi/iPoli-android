package io.ipoli.android.quest.schedule.calendar.dayview.view.widget

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent


/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 15.12.17.
 */

class LockableViewPager : ViewPager {

    var isLocked = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return !isLocked && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return !isLocked && super.onInterceptTouchEvent(event)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return !isLocked && super.canScrollHorizontally(direction)
    }
}