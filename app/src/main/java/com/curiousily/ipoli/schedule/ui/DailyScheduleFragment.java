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
public class DailyScheduleFragment extends Fragment implements DailyEventsLoader, DayView.DayChangeListener {

    @Bind(R.id.day_view)
    DayView dayView;

    @Bind(R.id.schedule_loader)
    ProgressWheel loader;

    @Bind(R.id.schedule_empty_layout)
    View newUserView;

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
        if (e.isNewUser) {
            loader.setVisibility(View.GONE);
            showNewUserView();
            return;
        }
        dayView.setDayChangeListener(this);
        dayView.setOnQuestClickListener(new DayView.QuestClickListener() {
            @Override
            public void onQuestClick(Quest quest, RectF eventRect) {
                post(new ShowQuestEvent(quest));
            }
        });
        dayView.setEmptyCellClickListener(new DayView.EmptyCellClickListener() {
            @Override
            public void onEmptyCellClick(Calendar time) {
                Log.d("iPoli", time.get(Calendar.HOUR_OF_DAY) + "");
            }
        });
        dayView.setDailyEventsLoader(this);
        dayView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        loadScheduleAsync();
    }

    @Subscribe
    public void onDailyScheduleLoaded(DailyScheduleLoadedEvent e) {
        loader.setVisibility(View.GONE);
        showSchedule(e.schedule);
    }

    public void showSchedule(DailySchedule e) {
        dayView.setVisibility(View.VISIBLE);
        dayView.setEvents(e.quests);
    }

    @Override
    public List<Quest> loadEventsFor(Calendar day) {
        loadScheduleAsync(day);
        return new ArrayList<>();
    }

    private void showNewUserView() {
        newUserView.setVisibility(View.VISIBLE);
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
        newUserView.setVisibility(View.GONE);
        post(new LoadUserEvent());
    }

    @Subscribe
    public void onQuestUpdated(QuestUpdatedEvent e) {
        loadScheduleAsync();
    }

    private void loadScheduleAsync() {
        loadScheduleAsync(Calendar.getInstance());
    }

    private void loadScheduleAsync(Calendar day) {
        loader.setVisibility(View.VISIBLE);
        dayView.setVisibility(View.GONE);
        Date scheduledFor = day.getTime();
        post(new LoadDailyScheduleEvent(scheduledFor, User.getCurrent(getContext()).id));
    }

    @Override
    public void onDayChanged(Calendar newDay, Calendar oldDay) {
        Log.d("iPoli", "Day changed " + newDay.toString());
        Calendar tomorrow = dayView.today();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Calendar yesterday = dayView.today();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (dayView.isSameDay(newDay, dayView.today())) {
            post(new ChangeToolbarTitleEvent("TODAY"));
        } else if (dayView.isSameDay(newDay, tomorrow)) {
            post(new ChangeToolbarTitleEvent("TOMORROW"));
        } else if (dayView.isSameDay(newDay, yesterday)) {
            post(new ChangeToolbarTitleEvent("YESTERDAY"));
        } else {
            post(new ChangeToolbarTitleEvent(dayView.getDateTimeInterpreter().interpretDate(newDay)));
        }
    }
}
