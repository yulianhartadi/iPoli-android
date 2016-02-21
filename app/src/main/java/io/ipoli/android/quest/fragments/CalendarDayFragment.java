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
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.UnscheduledQuestsAdapter;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.app.ui.calendar.CalendarLayout;
import io.ipoli.android.app.ui.calendar.CalendarListener;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestCalendarAdapter;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.QuestCalendarEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class CalendarDayFragment extends Fragment implements CalendarListener<QuestCalendarEvent> {

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
    private UnscheduledQuestsAdapter unscheduledQuestsAdapter;
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

        Schedule schedule = new CalendarScheduler().schedule();
        unscheduledQuestsAdapter = new UnscheduledQuestsAdapter(getContext(), schedule.getUnscheduledQuests(), eventBus);
        setUnscheduledQuestsHeight();

        unscheduledQuestList.setAdapter(unscheduledQuestsAdapter);
        unscheduledQuestList.setNestedScrollingEnabled(false);

        calendarDayView.scrollToNow();

        calendarAdapter = new QuestCalendarAdapter(schedule.getCalendarEvents(), eventBus);
        calendarDayView.setAdapter(calendarAdapter);
        return v;
    }

    @Subscribe
    public void onCompleteUnscheduledQuestRequest(CompleteUnscheduledQuestRequestEvent e) {
        eventBus.post(new CompleteQuestRequestEvent(e.quest));
        unscheduledQuestsAdapter.removeQuest(e.quest);
        setUnscheduledQuestsHeight();
    }

    private void setUnscheduledQuestsHeight() {
        int unscheduledQuestsToShow = Math.min(unscheduledQuestsAdapter.getItemCount(), Constants.MAX_UNSCHEDULED_QUEST_VISIBLE_COUNT);

        int itemHeight = getResources().getDimensionPixelSize(R.dimen.unscheduled_quest_item_height);

        ViewGroup.LayoutParams layoutParams = unscheduledQuestList.getLayoutParams();
        layoutParams.height = unscheduledQuestsToShow * itemHeight;
        unscheduledQuestList.setLayoutParams(layoutParams);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed() && isVisibleToUser) {
            updateSchedule();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        getContext().registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        updateSchedule();
    }

    private void updateSchedule() {
        Schedule schedule = new CalendarScheduler().schedule();
        unscheduledQuestsAdapter.updateQuests(schedule.getUnscheduledQuests());
        calendarAdapter.updateEvents(schedule.getCalendarEvents());
        setUnscheduledQuestsHeight();
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        getContext().unregisterReceiver(tickReceiver);
        super.onPause();
    }

    @Subscribe
    public void onMoveQuestToCalendarRequest(MoveQuestToCalendarRequestEvent e) {
        movingQuestPosition = unscheduledQuestsAdapter.indexOf(e.quest);
        movingQuest = e.quest;
        CalendarEvent calendarEvent = new QuestCalendarEvent(e.quest);
        calendarContainer.acceptNewEvent(calendarEvent);
        unscheduledQuestsAdapter.removeQuest(e.quest);
    }

    @Subscribe
    public void onQuestAddedToCalendar(QuestAddedToCalendarEvent e) {
        QuestCalendarEvent qce = e.questCalendarEvent;
        Quest q = qce.getQuest();
        q.setStartTime(qce.getStartTime());
        questPersistenceService.save(q);
    }

    @Override
    public void onUnableToAcceptNewEvent(QuestCalendarEvent calendarEvent) {
        unscheduledQuestsAdapter.addQuest(movingQuestPosition, movingQuest);
        setUnscheduledQuestsHeight();
    }

    @Override
    public void onAcceptEvent(QuestCalendarEvent calendarEvent) {
        if (calendarAdapter.canAddEvent(calendarEvent)) {
            eventBus.post(new QuestAddedToCalendarEvent(calendarEvent));
            calendarAdapter.addEvent(calendarEvent);
        } else {
            unscheduledQuestsAdapter.addQuest(movingQuestPosition, movingQuest);
        }
        setUnscheduledQuestsHeight();
    }

    private class CalendarScheduler {

        public Schedule schedule() {
            List<QuestCalendarEvent> calendarEvents = new ArrayList<>();

            List<Quest> completedTodayQuests = questPersistenceService.findAllCompletedToday();

            // completed events should be added first since we don't want them to intercept clicks
            // for incomplete events
            for (Quest q : completedTodayQuests) {
                QuestCalendarEvent event = new QuestCalendarEvent(q);
                if (isNotScheduledForToday(q)) {
                    Calendar c = Calendar.getInstance();
                    Date completedAt = q.getCompletedAtDateTime();
                    c.setTime(completedAt);
                    c.add(Calendar.MINUTE, -event.getDuration());
                    // actual start time was yesterday, so yeah we do not include multi-day events
                    if (!DateUtils.isToday(c.getTime())) {
                        continue;
                    }
                    event.setStartTime(c.getTime());
                }
                calendarEvents.add(event);
            }

            List<Quest> unscheduledQuests = new ArrayList<>();
            List<Quest> plannedQuests = questPersistenceService.findAllPlannedAndStartedToday();
            for (Quest q : plannedQuests) {
                if (q.getStartTime() == null) {
                    unscheduledQuests.add(q);
                } else {
                    calendarEvents.add(new QuestCalendarEvent(q));
                }
            }

            return new Schedule(unscheduledQuests, calendarEvents);
        }

        private boolean isNotScheduledForToday(Quest q) {
            return !DateUtils.isToday(q.getDue());
        }
    }

    private class Schedule {
        private List<Quest> unscheduledQuests;
        private List<QuestCalendarEvent> calendarEvents;

        private Schedule(List<Quest> unscheduledQuests, List<QuestCalendarEvent> calendarEvents) {
            this.unscheduledQuests = unscheduledQuests;
            this.calendarEvents = calendarEvents;
        }

        public List<Quest> getUnscheduledQuests() {
            return unscheduledQuests;
        }

        public List<QuestCalendarEvent> getCalendarEvents() {
            return calendarEvents;
        }
    }
}
