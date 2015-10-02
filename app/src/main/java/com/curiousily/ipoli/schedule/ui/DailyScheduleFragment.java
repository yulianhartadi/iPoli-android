package com.curiousily.ipoli.schedule.ui;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.quest.Quest;
import com.curiousily.ipoli.quest.services.events.QuestUpdatedEvent;
import com.curiousily.ipoli.schedule.DailySchedule;
import com.curiousily.ipoli.schedule.events.DailyScheduleLoadedEvent;
import com.curiousily.ipoli.schedule.events.LoadDailyScheduleEvent;
import com.curiousily.ipoli.schedule.ui.dayview.DayView;
import com.curiousily.ipoli.schedule.ui.dayview.loaders.DailyEventsLoader;
import com.curiousily.ipoli.schedule.ui.events.ChangeToolbarTitleEvent;
import com.curiousily.ipoli.schedule.ui.events.ShowQuestEvent;
import com.curiousily.ipoli.user.User;
import com.curiousily.ipoli.user.events.LoadUserEvent;
import com.curiousily.ipoli.user.events.UserLoadedEvent;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class DailyScheduleFragment extends Fragment implements DailyEventsLoader {

    @Bind(R.id.day_view)
    DayView dayView;

    @Bind(R.id.schedule_loader)
    ProgressWheel loader;

    @Bind(R.id.schedule_empty_layout)
    View emptySchedule;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_daily_schedule, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Subscribe
    public void onUserLoadedEvent(UserLoadedEvent e) {
        Log.d("iPoli", "User loaded");
        dayView.setScrollListener(new DayView.ScrollListener() {
            @Override
            public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
                Log.d("iPoli", "Day changed " + newFirstVisibleDay.toString());
                Calendar tomorrow = dayView.today();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                Calendar yesterday = dayView.today();
                yesterday.add(Calendar.DAY_OF_YEAR, -1);
                if (dayView.isSameDay(newFirstVisibleDay, dayView.today())) {
                    post(new ChangeToolbarTitleEvent("TODAY"));
                } else if (dayView.isSameDay(newFirstVisibleDay, tomorrow)) {
                    post(new ChangeToolbarTitleEvent("TOMORROW"));
                } else if (dayView.isSameDay(newFirstVisibleDay, yesterday)) {
                    post(new ChangeToolbarTitleEvent("YESTERDAY"));
                } else {
                    post(new ChangeToolbarTitleEvent(dayView.getDateTimeInterpreter().interpretDate(newFirstVisibleDay)));
                }

            }
        });
        dayView.setOnEventClickListener(new DayView.EventClickListener() {
            @Override
            public void onEventClick(Quest quest, RectF eventRect) {
                Log.d("iPoli", "Event selected");
                post(new ShowQuestEvent(quest));
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
        loadSchedule();
    }

    @Subscribe
    public void onDailyScheduleLoaded(DailyScheduleLoadedEvent e) {
        loader.setVisibility(View.GONE);
        DailySchedule schedule = e.schedule;
        if (schedule.quests.isEmpty()) {
            displayEmptySchedule();
        } else {
            displaySchedule(schedule);
        }
    }

    public void displaySchedule(DailySchedule e) {
        emptySchedule.setVisibility(View.GONE);
        dayView.setVisibility(View.VISIBLE);
        dayView.setEvents(e.quests);
    }

    @Override
    public List<Quest> loadEventsFor(Calendar day) {
        Log.d("iPoli", "Loading events");
        loadSchedule(day);
        return new ArrayList<>();
    }

    private void displayEmptySchedule() {
        emptySchedule.setVisibility(View.VISIBLE);
    }

    private void post(Object event) {
        EventBus.get().post(event);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.get().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.get().register(this);
        loader.setVisibility(View.VISIBLE);
        dayView.setVisibility(View.GONE);
        post(new LoadUserEvent());
    }

    @Subscribe
    public void onQuestUpdated(QuestUpdatedEvent e) {
        loadSchedule();
    }

    private void loadSchedule() {
        loadSchedule(Calendar.getInstance());
    }

    private void loadSchedule(Calendar day) {
        loader.setVisibility(View.VISIBLE);
        dayView.setVisibility(View.GONE);
        Date scheduledFor = day.getTime();
        Log.d("iPoli", "Loading daily quests");
        post(new LoadDailyScheduleEvent(scheduledFor, User.getCurrent(getContext()).id));
    }
}
