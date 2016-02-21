package io.ipoli.android.app.ui.calendar;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * This is due to NestedScrollView bug in Android
 *
 * @link https://code.google.com/p/android/issues/detail?id=178041
 * <p/>
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/21/16.
 */
public class CalendarScrollView extends NestedScrollView {

    public CalendarScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarScrollView(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // Explicitly call computeScroll() to make the Scroller compute itself
            computeScroll();
        }
        return super.onInterceptTouchEvent(ev);
    }
}
