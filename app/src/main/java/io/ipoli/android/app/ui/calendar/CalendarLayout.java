package io.ipoli.android.app.ui.calendar;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/16/16.
 */
public class CalendarLayout extends RelativeLayout {
    private boolean shouldIntercept = false;
    private View dragView;
    private float x;
    private float y;
    private CalendarListener calendarListener;
    private CalendarEvent calendarEvent;

    public CalendarLayout(Context context) {
        super(context);
    }

    public CalendarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CalendarLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setCalendarListener(CalendarListener calendarListener) {
        this.calendarListener = calendarListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        x = ev.getX();
        y = ev.getY();
        return shouldIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!shouldIntercept) {
            return false;
        }

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_MOVE:

                dragView.animate()
                        .x(event.getX() - dragView.getWidth() / 2)
                        .y(event.getY() - dragView.getHeight() / 2)
                        .setDuration(0)
                        .start();
                break;

            case MotionEvent.ACTION_UP:
                shouldIntercept = false;
                CalendarDayView calendarDayView = (CalendarDayView) findViewById(R.id.calendar);
                if (event.getY() <= calendarDayView.getTop() || event.getY() > calendarDayView.getBottom()) {
                    // return to list
                    if (calendarListener != null) {
                        calendarListener.onUnableToAcceptNewEvent(calendarEvent);
                    }
                    removeView(dragView);
                } else {

                    float yPos = event.getRawY() - dragView.getHeight() / 2;
                    int h = calendarDayView.getHoursFor(yPos);
                    int m = calendarDayView.getMinutesFor(yPos, 15);

                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, h);
                    c.set(Calendar.MINUTE, m);
                    calendarEvent.setStartTime(c.getTime());

                    if (calendarListener != null) {
                        calendarListener.onAcceptEvent(calendarEvent);
                    }


                    removeView(dragView);
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public void acceptNewEvent(CalendarEvent calendarEvent) {
        this.calendarEvent = calendarEvent;
        shouldIntercept = true;
        CalendarDayView calendarDayView = (CalendarDayView) findViewById(R.id.calendar);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        dragView = inflater.inflate(R.layout.calendar_quest_item, this, false);
        dragView.setBackgroundResource(calendarEvent.getBackgroundColor());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dragView.getLayoutParams();
        params.height = calendarDayView.getHeightFor(calendarEvent.getDuration());
        dragView.setLayoutParams(params);
        TextView nameView = (TextView) dragView.findViewById(R.id.quest_name);
        nameView.setText(calendarEvent.getName());

        addView(dragView);
        dragView.setVisibility(INVISIBLE);

        dragView.post(new Runnable() {
            @Override
            public void run() {
                dragView.setVisibility(VISIBLE);
                dragView.animate()
                        .x(x - dragView.getWidth() / 2)
                        .y(y - dragView.getHeight() / 2)
                        .setDuration(0)
                        .start();
            }
        });

    }
}
