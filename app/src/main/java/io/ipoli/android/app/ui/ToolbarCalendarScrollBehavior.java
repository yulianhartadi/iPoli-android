package io.ipoli.android.app.ui;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/2/16.
 */
public class ToolbarCalendarScrollBehavior extends AppBarLayout.Behavior {
    public ToolbarCalendarScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        return false;
    }
}
