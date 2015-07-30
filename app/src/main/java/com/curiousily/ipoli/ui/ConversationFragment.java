package com.curiousily.ipoli.ui;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.curiousily.ipoli.EventBus;
import com.curiousily.ipoli.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class ConversationFragment extends Fragment implements WeekView.MonthChangeListener, WeekView.EventClickListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        WeekView view = (WeekView) inflater.inflate(
                R.layout.fragment_conversation, container, false);
        view.setOnEventClickListener(this);
        view.setMonthChangeListener(this);
        view.goToHour(3);

        return view;
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
    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<>();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 1);
        endTime.set(Calendar.MINUTE, 0);
        WeekViewEvent event = new WeekViewEvent(1, "Cunk Poli", startTime, endTime);
        event.setColor(getResources().getColor(R.color.md_blue_500));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 6);
        startTime.set(Calendar.MINUTE, 0);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.MINUTE, 30);
        event = new WeekViewEvent(1, "Clean Vihur poop", startTime, endTime);
        event.setColor(getResources().getColor(R.color.md_indigo_500));
        events.add(event);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 7);
        startTime.set(Calendar.MINUTE, 0);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.MINUTE, 45);
        event = new WeekViewEvent(1, "Workout", startTime, endTime);
        event.setColor(getResources().getColor(R.color.md_red_500));
        events.add(event);

        return events;
    }

    @Override
    public void onEventClick(WeekViewEvent weekViewEvent, RectF rectF) {

    }
}
