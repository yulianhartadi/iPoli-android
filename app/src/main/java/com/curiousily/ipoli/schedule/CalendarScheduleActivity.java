package com.curiousily.ipoli.schedule;

import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.app.BaseActivity;
import com.curiousily.ipoli.schedule.ui.dayview.DayView;
import com.curiousily.ipoli.schedule.ui.dayview.DayViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/30/15.
 */
public class CalendarScheduleActivity extends BaseActivity implements DayView.MonthChangeListener {

    @Bind(R.id.day_view)
    DayView dayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_schedule);
        ButterKnife.bind(this);
        dayView.setMonthChangeListener(this);
        dayView.setScrollListener(new DayView.ScrollListener() {
            @Override
            public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
                Log.d("iPoli", "Day changed " + newFirstVisibleDay.toString());
            }
        });
        dayView.setOnEventClickListener(new DayView.EventClickListener() {
            @Override
            public void onEventClick(DayViewEvent event, RectF eventRect) {
                Log.d("iPoli", "Event selected");
            }
        });
        dayView.setEmptyViewClickListener(new DayView.EmptyViewClickListener() {
            @Override
            public void onEmptyViewClicked(Calendar time) {
                Log.d("iPoli", time.get(Calendar.HOUR_OF_DAY) + "");
            }
        });
        dayView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    @Override
    public List<DayViewEvent> onMonthChange(int newYear, int newMonth) {
        List<DayViewEvent> events = new ArrayList<>();
        DayViewEvent e = new DayViewEvent(1, "Read a book with Vihar", 2015, 9, 30, 10, 0, 2015, 9, 30, 10, 45);
        e.setColor(getResources().getColor(R.color.md_blue_500));
        if (e.getStartTime().get(Calendar.MONTH) == newMonth && e.getStartTime().get(Calendar.YEAR) == newYear) {
            events.add(e);
        }
        return events;
    }
}
