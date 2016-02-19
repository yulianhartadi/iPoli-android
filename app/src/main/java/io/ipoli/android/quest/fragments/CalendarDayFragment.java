package io.ipoli.android.quest.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.OnQuestLongClickListener;
import io.ipoli.android.app.UnscheduledQuestsAdapter;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.app.ui.calendar.CalendarLayout;
import io.ipoli.android.app.ui.calendar.CalendarListener;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestCalendarAdapter;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.QuestCalendarEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class CalendarDayFragment extends Fragment implements OnQuestLongClickListener, CalendarListener {
    @Inject
    Bus eventBus;

    @Bind(R.id.unscheduled_quests)
    RecyclerView unscheduledQuestList;

    @Bind(R.id.calendar)
    CalendarDayView calendarDayView;

    @Bind(R.id.calendar_container)
    CalendarLayout calendarContainer;

    @Inject
    QuestPersistenceService questPersistenceService;

    private int movingQuestPosition;

    private Quest movingQuest;
    private UnscheduledQuestsAdapter adapter;
    private QuestCalendarAdapter calendarAdapter;

    BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            calendarDayView.onMinuteChanged();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar_day_view, container, false);
        ButterKnife.bind(this, v);
        App.getAppComponent(getContext()).inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        unscheduledQuestList.setLayoutManager(layoutManager);

        calendarContainer.setCalendarListener(this);

        List<Quest> todayQuests = questPersistenceService.findAllForToday();

        List<Quest> unscheduledQuests = new ArrayList<>();
        List<QuestCalendarEvent> calendarEvents = new ArrayList<>();
        for (Quest q : todayQuests) {
            if (q.getStartTime() == null) {
                unscheduledQuests.add(q);
            } else {
                calendarEvents.add(new QuestCalendarEvent(q));
            }
        }

        adapter = new UnscheduledQuestsAdapter(getContext(), unscheduledQuests, eventBus, this);
        setUnscheduledQuestsHeight();

        unscheduledQuestList.setAdapter(adapter);
        unscheduledQuestList.setNestedScrollingEnabled(false);

        calendarDayView.scrollToNow();

        calendarAdapter = new QuestCalendarAdapter(calendarEvents, eventBus);
        calendarDayView.setAdapter(calendarAdapter);
        return v;
    }

    private void setUnscheduledQuestsHeight() {
        int unscheduledQuestsToShow = Math.min(adapter.getItemCount(), Constants.MAX_UNSCHEDULED_QUEST_VISIBLE_COUNT);

        int itemHeight = getResources().getDimensionPixelSize(R.dimen.unscheduled_quest_item_height);

        ViewGroup.LayoutParams layoutParams = unscheduledQuestList.getLayoutParams();
        layoutParams.height = unscheduledQuestsToShow * itemHeight;
        unscheduledQuestList.setLayoutParams(layoutParams);
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onPause() {
        getContext().unregisterReceiver(tickReceiver);
        super.onPause();
    }

    @Override
    public void onLongClick(int position, Quest quest) {
        this.movingQuestPosition = position;
        movingQuest = quest;
        CalendarEvent calendarEvent = new QuestCalendarEvent(quest);
        calendarContainer.acceptNewEvent(calendarEvent);
    }

    @Override
    public void onUnableToAcceptNewEvent(CalendarEvent calendarEvent) {
        adapter.addQuest(movingQuestPosition, movingQuest);
        setUnscheduledQuestsHeight();
    }

    @Override
    public void onAcceptEvent(CalendarEvent calendarEvent) {
        if (calendarAdapter.canAddEvent((QuestCalendarEvent) calendarEvent)) {
            calendarAdapter.addEvent((QuestCalendarEvent) calendarEvent);
        } else {
            adapter.addQuest(movingQuestPosition, movingQuest);
        }
        setUnscheduledQuestsHeight();
    }
}
