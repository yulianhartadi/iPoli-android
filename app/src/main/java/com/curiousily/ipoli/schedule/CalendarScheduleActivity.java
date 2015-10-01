package com.curiousily.ipoli.schedule;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.app.BaseActivity;
import com.curiousily.ipoli.quest.AddQuestActivity;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.viewmodel.QuestViewModel;
import com.curiousily.ipoli.schedule.events.DailyScheduleLoadedEvent;
import com.curiousily.ipoli.schedule.events.LoadDailyScheduleEvent;
import com.curiousily.ipoli.schedule.ui.dayview.DayView;
import com.curiousily.ipoli.schedule.ui.dayview.loaders.DailyEventsLoader;
import com.curiousily.ipoli.user.User;
import com.curiousily.ipoli.user.events.LoadUserEvent;
import com.curiousily.ipoli.user.events.UserLoadedEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/30/15.
 */
public class CalendarScheduleActivity extends BaseActivity implements DailyEventsLoader {

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


    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.post(new LoadUserEvent());
    }

    private void loadSchedule(Calendar day) {
        Date scheduledFor = day.getTime();
        EventBus.post(new LoadDailyScheduleEvent(scheduledFor, User.getCurrent(this).id));
    }

    @Subscribe
    public void onUserLoadedEvent(UserLoadedEvent e) {
//        dayView.setMonthChangeListener(this);
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
        dayView.setDailyEventsLoader(this);
        dayView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

    }

    @OnClick(R.id.add_button)
    public void onAddButtonClick() {
        Intent intent = new Intent(this, AddQuestActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Subscribe
    public void onDailyScheduleLoaded(DailyScheduleLoadedEvent e) {
        List<QuestViewModel> events = new ArrayList<>();
        Log.d("iPoli", "Loading daily events");
        for(Quest q : e.schedule.quests) {
            QuestViewModel m = QuestViewModel.from(q);
            m.startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            endTime.add(Calendar.MINUTE, 45);
            m.endTime = endTime;
            events.add(m);
        }
        dayView.setEvents(events);
    }

    @Override
    public List<QuestViewModel> loadEventsFor(Calendar day) {
        loadSchedule(day);
        return new ArrayList<>();
    }
}
