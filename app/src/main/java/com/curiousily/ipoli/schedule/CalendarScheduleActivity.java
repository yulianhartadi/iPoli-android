package com.curiousily.ipoli.schedule;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.curiousily.ipoli.R;
import com.curiousily.ipoli.app.BaseActivity;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.viewmodel.QuestViewModel;
import com.curiousily.ipoli.schedule.ui.dayview.DayView;

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

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_schedule);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        dayView.setMonthChangeListener(this);
        dayView.setScrollListener(new DayView.ScrollListener() {
            @Override
            public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
                Log.d("iPoli", "Day changed " + newFirstVisibleDay.toString());
                Calendar tomorrow = dayView.today();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                Calendar yesterday = dayView.today();
                yesterday.add(Calendar.DAY_OF_YEAR, -1);
                if (dayView.isSameDay(newFirstVisibleDay, dayView.today())) {
                    toolbarTitle.setText("TODAY");
                } else if (dayView.isSameDay(newFirstVisibleDay, tomorrow)) {
                    toolbarTitle.setText("TOMORROW");
                } else if (dayView.isSameDay(newFirstVisibleDay, yesterday)) {
                    toolbarTitle.setText("YESTERDAY");
                } else {
                    toolbarTitle.setText(dayView.getDateTimeInterpreter().interpretDate(newFirstVisibleDay));
                }

            }
        });
        dayView.setOnEventClickListener(new DayView.EventClickListener() {
            @Override
            public void onEventClick(QuestViewModel event, RectF eventRect) {
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
    public List<QuestViewModel> onMonthChange(int newYear, int newMonth) {
        List<QuestViewModel> events = new ArrayList<>();
        QuestViewModel q = new QuestViewModel();
        q.backgroundColor = Quest.Context.ACTIVITY.getPrimaryColor();
        q.name = "Do a HIT workout";
        q.startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.MINUTE, 45);
        q.endTime = endTime;
        if (q.startTime.get(Calendar.MONTH) == newMonth && q.startTime.get(Calendar.YEAR) == newYear) {
            events.add(q);
        }
        return events;
    }
}
