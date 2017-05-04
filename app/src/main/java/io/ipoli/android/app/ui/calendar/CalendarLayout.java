package io.ipoli.android.app.ui.calendar;

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.ViewUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/16/16.
 */
public class CalendarLayout extends FrameLayout {
    private float y;
    private CalendarListener calendarListener;
    private CalendarDayView calendarDayView;
    private LayoutInflater inflater;
    private boolean isInEditMode = false;
    private boolean use24HourFormat = true;

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
        use24HourFormat = true;
        setOnDragListener(dragListener);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        calendarDayView = (CalendarDayView) findViewById(R.id.calendar);
        inflater = LayoutInflater.from(getContext());
    }

    public void setTimeFormat(boolean use24HourFormat) {
        this.use24HourFormat = use24HourFormat;
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

        View dragView = inflater.inflate(R.layout.calendar_drag_item, this, false);
        dragView.findViewById(R.id.quest_details_container).setBackgroundResource(calendarEvent.getDragBackgroundColor());
        CalendarLayout.LayoutParams params = (CalendarLayout.LayoutParams) dragView.getLayoutParams();
        params.height = calendarDayView.getHeightFor(Math.max(calendarEvent.getDuration(), Constants.CALENDAR_EVENT_MIN_DURATION));
        params.topMargin = (int) y - params.height / 2;

        TextView nameView = (TextView) dragView.findViewById(R.id.quest_text);
        nameView.setText(calendarEvent.getName());

        if (calendarEvent.getDuration() <= Constants.CALENDAR_EVENT_MIN_DURATION) {
            adjustQuestDetailsView(dragView);
        }

        if (calendarEvent.getDuration() <= Constants.CALENDAR_EVENT_MIN_SINGLE_LINE_DURATION) {
            nameView.setSingleLine(true);
            nameView.setEllipsize(TextUtils.TruncateAt.END);
        }

        addView(dragView);

        DragStrategy dragStrategy = new DragStrategy() {

            private boolean hasDropped;

            private View dragView;
            private CalendarEvent calendarEvent;
            private int initialTouchHeight;

            @Override
            public void onDragStarted(DragEvent event) {
                hasDropped = false;
                //event.getY() is different in Started and Entered
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    computeInitialTouchHeight(event);
                }
            }

            @Override
            public void onDragEntered(DragEvent event) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    computeInitialTouchHeight(event);
                }
            }

            private void computeInitialTouchHeight(DragEvent event) {
                initialTouchHeight = (int) (event.getY() - dragView.getTop());
            }

            @Override
            public void onDragMoved(DragEvent event) {
                LayoutParams layoutParams = (LayoutParams) dragView.getLayoutParams();
                layoutParams.topMargin = (int) event.getY() - initialTouchHeight;
                dragView.setLayoutParams(layoutParams);

                int hours = calendarDayView.getHoursFor(ViewUtils.getViewRawTop(dragView));
                int minutes = calendarDayView.getMinutesFor(ViewUtils.getViewRawTop(dragView), 5);
                ((TextView) dragView.findViewById(R.id.quest_current_time_indicator)).setText(Time.at(hours, minutes).toString(use24HourFormat));
            }

            @Override
            public void onDragDropped(DragEvent event) {
                isInEditMode = false;
                hasDropped = true;
                CalendarDayView calendarDayView = (CalendarDayView) findViewById(R.id.calendar);
                if (event.getY() <= calendarDayView.getTop() || event.getY() > calendarDayView.getBottom()) {
                    // return to list
                    if (calendarListener != null) {
                        calendarListener.onUnableToAcceptNewEvent(calendarEvent);
                    }
                } else {
                    int hours = calendarDayView.getHoursFor(ViewUtils.getViewRawTop(dragView));
                    int minutes = calendarDayView.getMinutesFor(ViewUtils.getViewRawTop(dragView), 5);
                    calendarEvent.setStartMinute(Time.at(hours, minutes).toMinuteOfDay());

                    if (calendarListener != null) {
                        calendarListener.onAcceptEvent(calendarEvent);
                    }
                }
                removeView(dragView);
            }

            @Override
            public void onDragExited(DragEvent event) {

            }

            @Override
            public void onDragEnded() {
                if (!hasDropped) {
                    if (calendarListener != null) {
                        calendarListener.onUnableToAcceptNewEvent(calendarEvent);
                    }
                    removeView(dragView);
                }
            }

            private DragStrategy init(View dragView, CalendarEvent calendarEvent) {
                this.dragView = dragView;
                this.calendarEvent = calendarEvent;
                return this;
            }

        }.init(dragView, calendarEvent);

        isInEditMode = true;
        dragView(dragView, dragStrategy);

    }

    private void setDragStrategy(DragStrategy dragStrategy) {
        this.dragStrategy = dragStrategy;
    }

    private void adjustQuestDetailsView(View v) {
        ViewGroup detailsContainer = (ViewGroup) v.findViewById(R.id.quest_details_container);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) detailsContainer.getLayoutParams();
        params.topMargin = 0;
        detailsContainer.setLayoutParams(params);
    }

    private OnDragListener dragListener = new OnDragListener() {

        @Override
        public boolean onDrag(View v, DragEvent event) {

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
                    dragStrategy.onDragDropped(event);
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    dragStrategy.onDragExited(event);
                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    dragStrategy.onDragEnded();
                    dragStrategy = null;
                    break;

                default:
                    break;
            }
            return true;
        }
    };


    public void editView(View calendarEventView) {
        dragView(calendarEventView, calendarDayView.getEditViewDragStrategy(calendarEventView));
    }

    private void dragView(View calendarEventView, DragStrategy dragStrategy) {
        setDragStrategy(dragStrategy);
        calendarEventView.startDrag(ClipData.newPlainText("", ""),
                new DummyDragShadowBuilder(),
                calendarEventView,
                0
        );
    }

    public boolean isInEditMode() {
        return isInEditMode;
    }
}
