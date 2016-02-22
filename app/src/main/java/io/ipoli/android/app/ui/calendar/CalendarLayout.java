package io.ipoli.android.app.ui.calendar;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

import io.ipoli.android.Constants;
import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/16/16.
 */
public class CalendarLayout extends RelativeLayout {
    private float y;
    private CalendarListener calendarListener;
    private CalendarDayView calendarDayView;
    private LayoutInflater inflater;

    private DragStrategy dragStrategy;

    public CalendarLayout(Context context) {
        super(context);
        initUI();
    }

    public CalendarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI();
    }

    public CalendarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI();
    }

    public CalendarLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initUI();
    }

    private void initUI() {
        setOnDragListener(dragListener);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        calendarDayView = (CalendarDayView) findViewById(R.id.calendar);
        inflater = LayoutInflater.from(getContext());
    }

    public void setCalendarListener(CalendarListener calendarListener) {
        this.calendarListener = calendarListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        y = ev.getY();
        return false;
    }

    public void acceptNewEvent(CalendarEvent calendarEvent) {

        View dragView = inflater.inflate(R.layout.calendar_quest_item, this, false);
        dragView.setBackgroundResource(calendarEvent.getBackgroundColor());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dragView.getLayoutParams();
        params.height = calendarDayView.getHeightFor(calendarEvent.getDuration());
        params.topMargin = (int) y - params.height / 2;
        dragView.setLayoutParams(params);
        if (calendarEvent.getDuration() <= Constants.QUEST_CALENDAR_EVENT_MIN_DURATION) {
            adjustQuestDetailsView(dragView);
        }
        TextView nameView = (TextView) dragView.findViewById(R.id.quest_text);
        nameView.setText(calendarEvent.getName());

        addView(dragView);

        DragStrategy dragStrategy = new DragStrategy() {
            public View dragView;
            private CalendarEvent calendarEvent;
            public int initialTouchHeight;

            @Override
            public void onDragStarted(DragEvent event) {
                int[] loc = new int[2];
                calendarDayView.getLocationOnScreen(loc);
                initialTouchHeight = (int) (event.getY() - dragView.getTop()) - loc[1];
            }

            @Override
            public void onDragEntered(DragEvent event) {

            }

            @Override
            public void onDragMoved(DragEvent event) {
                LayoutParams layoutParams = (LayoutParams) dragView.getLayoutParams();
                layoutParams.topMargin = (int) event.getY() - initialTouchHeight;
                dragView.setLayoutParams(layoutParams);
            }

            @Override
            public void onDragEnded(DragEvent event) {
                CalendarDayView calendarDayView = (CalendarDayView) findViewById(R.id.calendar);
                if (event.getY() <= calendarDayView.getTop() || event.getY() > calendarDayView.getBottom()) {
                    // return to list
                    if (calendarListener != null) {
                        calendarListener.onUnableToAcceptNewEvent(calendarEvent);
                    }
                } else {
                    Calendar c = Calendar.getInstance();
                    int hours = calendarDayView.getHoursFor(getViewTop(dragView));
                    int minutes = calendarDayView.getMinutesFor(getViewTop(dragView), 5);
                    c.set(Calendar.HOUR_OF_DAY, hours);
                    c.set(Calendar.MINUTE, minutes);
                    calendarEvent.setStartTime(c.getTime());

                    if (calendarListener != null) {
                        calendarListener.onAcceptEvent(calendarEvent);
                    }
                }
                removeView(dragView);
            }

            private DragStrategy init(View dragView, CalendarEvent calendarEvent) {
                this.dragView = dragView;
                this.calendarEvent = calendarEvent;
                return this;
            }

        }.init(dragView, calendarEvent);

        setDragStrategy(dragStrategy);

        dragView.startDrag(ClipData.newPlainText("", ""),
                new DummyDragShadowBuilder(),
                dragView,
                0
        );

    }

    public void setDragStrategy(DragStrategy dragStrategy) {
        this.dragStrategy = dragStrategy;
    }

    private void adjustQuestDetailsView(View v) {
        LinearLayout detailsContainer = (LinearLayout) v.findViewById(R.id.quest_details_container);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) detailsContainer.getLayoutParams();
        params.topMargin = 0;
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        detailsContainer.setLayoutParams(params);
    }

    private int getViewTop(View v) {
        int[] loc = new int[2];
        v.getLocationInWindow(loc);
        return loc[1];
    }

    private Point getTouchPositionFromDragEvent(DragEvent event) {
        Rect rItem = new Rect();
        calendarDayView.getGlobalVisibleRect(rItem);
        return new Point(rItem.left + Math.round(event.getX()), rItem.top + Math.round(event.getY()));
    }

    private OnDragListener dragListener = new OnDragListener() {

        @Override
        public boolean onDrag(View _, DragEvent event) {

            if (dragStrategy == null) {
                return false;
            }

            switch (event.getAction()) {

                case DragEvent.ACTION_DRAG_STARTED:
                    dragStrategy.onDragStarted(event);
                    break;

                case DragEvent.ACTION_DRAG_ENTERED:
                    dragStrategy.onDragEntered(event);
                    break;

                case DragEvent.ACTION_DRAG_LOCATION:
                    dragStrategy.onDragMoved(event);
                    break;

                case DragEvent.ACTION_DROP:
                    dragStrategy.onDragEnded(event);
                    break;

                default:
                    break;
            }
            return true;
        }
    };


    public void editView(View calendarEventView) {
        setDragStrategy(calendarDayView.getEditViewDragStrategy(calendarEventView));
        calendarEventView.startDrag(ClipData.newPlainText("", ""),
                new DummyDragShadowBuilder(),
                calendarEventView,
                0
        );
    }
}
