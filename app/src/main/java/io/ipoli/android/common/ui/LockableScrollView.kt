package io.ipoli.android.common.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView


/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/5/17.
 */
class LockableScrollView : ScrollView {

    var locked: Boolean = false
        set(value) {
            field = value
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        if (locked)
            false
        else
            super.onInterceptTouchEvent(ev)
}