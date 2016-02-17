package io.ipoli.android.app;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.app.ui.calendar.CalendarLayout;
import io.ipoli.android.app.ui.calendar.CalendarListener;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestCalendarAdapter;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.Status;
import io.ipoli.android.quest.ui.QuestCalendarEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class CalendarDayFragment extends Fragment implements OnQuestLongClickListener, CalendarListener {
    @Inject
    Bus eventBus;

    @Bind(R.id.unscheduled_quests)
    RecyclerView unscheduledQuests;

    @Bind(R.id.calendar)
    CalendarDayView calendarDayView;

    @Bind(R.id.calendar_container)
    CalendarLayout calendarContainer;


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
        unscheduledQuests.setLayoutManager(layoutManager);

        calendarContainer.setCalendarListener(this);

        Quest q = new Quest("Go for a run", Status.PLANNED.name(), new Date());
        Quest.setContext(q, QuestContext.WELLNESS);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        q.setStartTime(calendar.getTime());
        q.setDuration(30);

        Quest qq = new Quest("Read a book", Status.PLANNED.name(), new Date());
        Quest.setContext(qq, QuestContext.LEARNING);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 30);
        qq.setStartTime(calendar.getTime());
        qq.setDuration(60);

        Quest qqq = new Quest("Call Mom", Status.PLANNED.name(), new Date());
        Quest.setContext(qqq, QuestContext.PERSONAL);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 15);
        qqq.setStartTime(calendar.getTime());
        qqq.setDuration(15);

        Quest qqqq = new Quest("Work on presentation", Status.PLANNED.name(), new Date());
        Quest.setContext(qqqq, QuestContext.WORK);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        qqqq.setStartTime(calendar.getTime());
        qqqq.setDuration(120);

        Quest qqqqq = new Quest("Watch Star Wars", Status.PLANNED.name(), new Date());
        Quest.setContext(qqqqq, QuestContext.FUN);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 0);
        qqqqq.setStartTime(calendar.getTime());
        qqqqq.setDuration(180);

        List<Quest> quests = new ArrayList<>();
        quests.add(q);
        quests.add(qq);
        quests.add(qqq);

        adapter = new UnscheduledQuestsAdapter(getContext(), quests, eventBus, this);
        setUnscheduledQuestsHeight();

        unscheduledQuests.setAdapter(adapter);
        unscheduledQuests.setNestedScrollingEnabled(false);

        calendarDayView.scrollToNow();

        ArrayList<QuestCalendarEvent> calendarEvents = new ArrayList<>();
        calendarEvents.add(new QuestCalendarEvent(q));
        calendarEvents.add(new QuestCalendarEvent(qq));
        calendarEvents.add(new QuestCalendarEvent(qqq));
        calendarEvents.add(new QuestCalendarEvent(qqqq));
        calendarEvents.add(new QuestCalendarEvent(qqqqq));
        calendarAdapter = new QuestCalendarAdapter(calendarEvents);
        calendarDayView.setAdapter(calendarAdapter);
        return v;
    }

    private void setUnscheduledQuestsHeight() {
        int unscheduledQuestsToShow = Math.min(adapter.getItemCount(), Constants.MAX_UNSCHEDULED_QUEST_VISIBLE_COUNT);

        int itemHeight = getResources().getDimensionPixelSize(R.dimen.unscheduled_quest_item_height);

        ViewGroup.LayoutParams layoutParams = unscheduledQuests.getLayoutParams();
        layoutParams.height = unscheduledQuestsToShow * itemHeight;
        unscheduledQuests.setLayoutParams(layoutParams);
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
