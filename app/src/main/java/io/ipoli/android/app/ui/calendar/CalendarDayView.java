package io.ipoli.android.app.ui.calendar;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.app.utils.ViewUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/6/16.
 */
public class CalendarDayView extends FrameLayout {

    public static final int HOURS_PER_SCREEN = 5;
    public static final int HOURS_IN_A_DAY = 24;
    public static final int TOP_PADDING_HOURS = 1;

    private float minuteHeight;
    private RelativeLayout eventsContainer;
    private int hourHeight;
    private View timeLine;
    private List<TextView> hourViews = new ArrayList<>();

    private BaseCalendarAdapter adapter;
    private Map<View, CalendarEvent> eventViewToCalendarEvent;

    private NestedScrollView scrollView;

    private int hourCellClickYPos;

    private OnHourCellLongClickListener hourCellListener;

    private boolean use24HourFormat = true;

    public interface OnHourCellLongClickListener {
        void onLongClickHourCell(Time atTime);
    }

    public CalendarDayView(Context context) {
        super(context);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public CalendarDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public CalendarDayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public CalendarDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    private void initUI(Context context) {
        use24HourFormat = true;

        eventViewToCalendarEvent = new HashMap<>();
        hourHeight = getScreenHeight(context) / HOURS_PER_SCREEN;
        minuteHeight = hourHeight / 60.0f;

        LayoutInflater inflater = LayoutInflater.from(context);
        FrameLayout mainContainer = initMainContainer(context);
        mainContainer.addView(initHourCellsContainer(context, inflater));
        mainContainer.addView(initTimeLineContainer(context, inflater));
        eventsContainer = initEventsContainer(context);
        mainContainer.addView(eventsContainer);

        addView(initScrollView(context, mainContainer));
    }

    private NestedScrollView initScrollView(Context context, FrameLayout mainContainer) {
        scrollView = new NestedScrollView(context);
        scrollView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setVerticalScrollBarEnabled(true);
        scrollView.addView(mainContainer);
        return scrollView;
    }

    @NonNull
    private FrameLayout initMainContainer(Context context) {
        FrameLayout mainContainer = new FrameLayout(context);
        mainContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return mainContainer;
    }

    @NonNull
    private LinearLayout initHourCellsContainer(Context context, LayoutInflater inflater) {
        LinearLayout hourCellsContainer = new LinearLayout(context);
        hourCellsContainer.setOrientation(LinearLayout.VERTICAL);
        hourCellsContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        for (int i = 0; i < HOURS_IN_A_DAY; i++) {
            hourCellsContainer.addView(initHourCell(i, hourCellsContainer, inflater));
        }

        hourCellsContainer.setOnTouchListener((view, event) -> {
            hourCellClickYPos = (int) event.getRawY();
            return false;
        });

        hourCellsContainer.setOnLongClickListener(view -> {
            if (hourCellListener != null) {
                hourCellListener.onLongClickHourCell(Time.at(getHoursFor(hourCellClickYPos), getMinutesFor(hourCellClickYPos, 15)));
            }
            return false;
        });
        return hourCellsContainer;
    }

    @NonNull
    private View initHourCell(int hour, LinearLayout hourCellsContainer, LayoutInflater inflater) {
        View hourCell = inflater.inflate(R.layout.calendar_hour_cell, hourCellsContainer, false);
        TextView tv = (TextView) hourCell.findViewById(R.id.time_label);
        hourViews.add(tv);
        if (hour > 0) {
            tv.setText(Time.atHours(hour).toString(use24HourFormat));
        }
        ViewGroup.LayoutParams hcp = hourCell.getLayoutParams();
        hcp.height = hourHeight;
        hourCell.setLayoutParams(hcp);
        return hourCell;
    }

    public void setTimeFormat(boolean use24HourFormat) {
        this.use24HourFormat = use24HourFormat;
        for (int i = 1; i < hourViews.size(); i++) {
            hourViews.get(i).setText(Time.atHours(i).toString(use24HourFormat));
        }
    }

    private RelativeLayout initEventsContainer(Context context) {
        eventsContainer = new RelativeLayout(context);
        eventsContainer.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return eventsContainer;
    }

    @NonNull
    private RelativeLayout initTimeLineContainer(Context context, LayoutInflater inflater) {
        RelativeLayout timeRL = new RelativeLayout(context);
        timeRL.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        timeLine = inflater.inflate(R.layout.calendar_time_line, timeRL, false);

        RelativeLayout.LayoutParams lPTime = (RelativeLayout.LayoutParams) timeLine.getLayoutParams();
        lPTime.topMargin = getCurrentTimeYPosition();
        timeRL.addView(timeLine, lPTime);

        return timeRL;
    }

    private int getScreenHeight(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    public void setAdapter(BaseCalendarAdapter<? extends CalendarEvent> adapter) {
        this.adapter = adapter;
        adapter.setCalendarDayView(this);
        adapter.notifyDataSetChanged();
    }

    <E extends CalendarEvent> void addEvent(E calendarEvent, int position) {
        final View eventView = adapter.getView(eventsContainer, position);
        RelativeLayout.LayoutParams qlp = (RelativeLayout.LayoutParams) eventView.getLayoutParams();
        qlp.topMargin = getYPositionFor(calendarEvent.getStartMinute());
        qlp.height = getHeightFor(Math.max(calendarEvent.getDuration(), Constants.CALENDAR_EVENT_MIN_DURATION));
        eventsContainer.addView(eventView, qlp);
        eventViewToCalendarEvent.put(eventView, calendarEvent);
    }

    public void onMinuteChanged() {
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) timeLine.getLayoutParams();
        p.topMargin = getCurrentTimeYPosition();
        timeLine.setLayoutParams(p);
    }

    int getHoursFor(float y) {
        float h = getRelativeY((int) y) / hourHeight;
        return Math.min(Math.round(h), 23);
    }

    int getMinutesFor(float y, int rangeLength) {
        int minutes = (int) ((getRelativeY((int) y) % hourHeight) / minuteHeight);
        minutes = Math.max(0, minutes);
        List<Integer> bounds = new ArrayList<>();
        int rangeStart = 0;
        for (int min = 0; min < 60; min++) {

            if (min % rangeLength == 0) {
                rangeStart = min;
            }
            bounds.add(rangeStart);
        }
        return bounds.get(minutes);
    }

    private int getYPositionFor(int hours, int minutes) {
        int y = hours * hourHeight;
        y += getMinutesHeight(minutes);
        return y;
    }

    private int getYPositionFor(int minutesAfterMidnight) {
        Time time = Time.of(minutesAfterMidnight);
        return getYPositionFor(time.getHours(), time.getMinutes());
    }

    int getHeightFor(int duration) {
        return (int) getMinutesHeight(duration);
    }

    private float getMinutesHeight(int minutes) {
        return minuteHeight * minutes;
    }

    private int getCurrentTimeYPosition() {
        return getCurrentTimeYPosition(0);
    }

    private int getCurrentTimeYPosition(int hourOffset) {
        Calendar c = Calendar.getInstance();
        int hour = Math.max(0, c.get(Calendar.HOUR_OF_DAY) - hourOffset);
        int minutes = c.get(Calendar.MINUTE);
        return getYPositionFor(hour, minutes);
    }

    private int getRelativeY(int y) {
        int offsets[] = new int[2];
        getLocationOnScreen(offsets);
        return getRelativeY(y, offsets[1]);
    }

    private int getRelativeY(int y, int yOffset) {
        return Math.max(0, scrollView.getScrollY() + y - yOffset);
    }

    public void scrollToNow() {
        scrollView.post(() -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            hour = Math.max(0, hour - TOP_PADDING_HOURS);
            if (hour == 0) {
                scrollView.scrollTo(scrollView.getScrollX(), 0);
            } else {
                int minutes = c.get(Calendar.MINUTE);
                scrollView.scrollTo(scrollView.getScrollX(), getYPositionFor(hour, minutes));
            }
        });
    }

    public void smoothScrollToTime(final Time time) {
        int hour = Math.max(0, time.getHours() - TOP_PADDING_HOURS);
        int scrollTo = hour == 0 ? 0 : getYPositionFor(hour, time.getMinutes());
        int animationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        ObjectAnimator.ofInt(scrollView, "scrollY", scrollTo).setDuration(animationDuration).start();
    }

    public void removeAllEvents() {
        eventsContainer.removeAllViews();
        eventViewToCalendarEvent.clear();
    }

    DragStrategy getEditViewDragStrategy(final View dragView) {
        return new DragStrategy() {
            private boolean hasDropped;
            private int initialTouchHeight;

            @Override
            public void onDragStarted(DragEvent event) {
                hasDropped = false;
                CalendarEvent calendarEvent = eventViewToCalendarEvent.get(dragView);
                adapter.onDragStarted(dragView, Time.of(calendarEvent.getStartMinute()));

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
                int[] dragViewLoc = new int[2];
                dragView.getLocationOnScreen(dragViewLoc);
                int[] calendarViewLoc = new int[2];
                getLocationOnScreen(calendarViewLoc);
                int dragViewTop = dragViewLoc[1] - calendarViewLoc[1];
                initialTouchHeight = (int) (event.getY() - getTop() - dragViewTop);
            }

            @Override
            public void onDragMoved(DragEvent event) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) dragView.getLayoutParams();
                layoutParams.topMargin = getRelativeY((int) (event.getY() - initialTouchHeight), getTop());
                dragView.setLayoutParams(layoutParams);
                int h = getHoursFor(ViewUtils.getViewRawTop(dragView));
                int m = getMinutesFor(ViewUtils.getViewRawTop(dragView), 5);
                adapter.onDragMoved(dragView, Time.at(h, m));
            }

            @Override
            public void onDragDropped(DragEvent event) {
                hasDropped = true;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) dragView.getLayoutParams();
                int h = getHoursFor(ViewUtils.getViewRawTop(dragView));
                int m = getMinutesFor(ViewUtils.getViewRawTop(dragView), 5);
                layoutParams.topMargin = getYPositionFor(h, m);
                dragView.setLayoutParams(layoutParams);
                CalendarEvent calendarEvent = eventViewToCalendarEvent.get(dragView);
                int oldStartTime = calendarEvent.getStartMinute();
                calendarEvent.setStartMinute(Time.at(h, m).toMinuteOfDay());
                adapter.onStartTimeUpdated(calendarEvent, oldStartTime);
                adapter.onDragEnded(dragView);
            }

            @Override
            public void onDragExited(DragEvent event) {
            }

            @Override
            public void onDragEnded() {
                if (!hasDropped) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) dragView.getLayoutParams();
                    CalendarEvent calendarEvent = eventViewToCalendarEvent.get(dragView);
                    layoutParams.topMargin = getYPositionFor(calendarEvent.getStartMinute());
                    dragView.setLayoutParams(layoutParams);
                    adapter.onDragEnded(dragView);
                }
            }
        };
    }

    public void showTimeLine() {
        timeLine.setVisibility(VISIBLE);
    }

    public void hideTimeLine() {
        timeLine.setVisibility(GONE);
    }

    public void setOnHourCellLongClickListener(OnHourCellLongClickListener hourCellListener) {
        this.hourCellListener = hourCellListener;
    }
}