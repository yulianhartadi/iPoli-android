package io.ipoli.android.quest.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.app.ui.calendar.CalendarLayout;
import io.ipoli.android.app.ui.calendar.CalendarListener;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.adapters.QuestCalendarAdapter;
import io.ipoli.android.quest.adapters.UnscheduledQuestsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.ui.QuestCalendarEvent;
import io.ipoli.android.quest.ui.events.EditCalendarEventEvent;
import io.ipoli.android.quest.viewmodels.UnscheduledQuestViewModel;
import rx.Observable;

public class CalendarDayFragment extends Fragment implements CalendarListener<QuestCalendarEvent> {
    @Inject
    protected Bus eventBus;

    @Bind(R.id.unscheduled_quests)
    RecyclerView unscheduledQuestList;

    @Bind(R.id.calendar)
    CalendarDayView calendarDayView;

    @Bind(R.id.calendar_container)
    CalendarLayout calendarContainer;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    private int movingQuestPosition;

    private UnscheduledQuestViewModel movingViewModel;
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
        View view = inflater.inflate(R.layout.fragment_calendar_day, container, false);
        ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(new SimpleDateFormat(getString(R.string.today_date_format), Locale.getDefault()).format(new Date()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        unscheduledQuestList.setLayoutManager(layoutManager);

        calendarContainer.setCalendarListener(this);

        unscheduledQuestsAdapter = new UnscheduledQuestsAdapter(getContext(), new ArrayList<>(), eventBus);

        unscheduledQuestList.setAdapter(unscheduledQuestsAdapter);
        unscheduledQuestList.setNestedScrollingEnabled(false);

        calendarDayView.scrollToNow();

        calendarAdapter = new QuestCalendarAdapter(new ArrayList<>(), eventBus);
        calendarDayView.setAdapter(calendarAdapter);


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Subscribe
    public void onCompleteUnscheduledQuestRequest(CompleteUnscheduledQuestRequestEvent e) {
        eventBus.post(new CompleteQuestRequestEvent(e.viewModel.getQuest()));
        unscheduledQuestsAdapter.removeQuest(e.viewModel);
        setUnscheduledQuestsHeight();
    }

    private void setUnscheduledQuestsHeight() {
        int unscheduledQuestsToShow = Math.min(unscheduledQuestsAdapter.getItemCount(), Constants.MAX_UNSCHEDULED_QUEST_VISIBLE_COUNT);

        int itemHeight = getResources().getDimensionPixelSize(R.dimen.unscheduled_quest_item_height);

        ViewGroup.LayoutParams layoutParams = unscheduledQuestList.getLayoutParams();
        layoutParams.height = unscheduledQuestsToShow * itemHeight;
        unscheduledQuestList.setLayoutParams(layoutParams);
    }

    @Subscribe
    public void onUndoCompletedQuestRequest(UndoCompletedQuestRequestEvent e) {
        Quest quest = e.quest;
        // @TODO remove old logs
        quest.getLogs().clear();
        quest.setDifficulty(0);
        quest.setActualStartDateTime(null);
        quest.setActualDuration(0);
        quest.setCompletedAtDateTime(null);
        questPersistenceService.save(quest);
        eventBus.post(new UndoCompletedQuestEvent(quest));
        Toast.makeText(getContext(), "Quest undone", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        getContext().registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        updateSchedule();
    }

    private void updateSchedule() {
        new CalendarScheduler().schedule().subscribe(schedule -> {

            List<UnscheduledQuestViewModel> unscheduledViewModels = new ArrayList<>();

            Map<String, List<Quest>> map = new HashMap<>();
            for (Quest q : schedule.getUnscheduledQuests()) {
                if (q.getRecurrentQuest() == null) {
                    unscheduledViewModels.add(new UnscheduledQuestViewModel(q, 1));
                    continue;
                }
                String key = q.getRecurrentQuest().getId();
                if (map.get(key) == null) {
                    map.put(key, new ArrayList<>());
                }
                map.get(key).add(q);
            }

            for (String key : map.keySet()) {
                Quest q = map.get(key).get(0);
                int remainingCount = map.get(key).size();
                unscheduledViewModels.add(new UnscheduledQuestViewModel(q, remainingCount));
            }


            unscheduledQuestsAdapter.updateQuests(unscheduledViewModels);
            calendarAdapter.updateEvents(schedule.getCalendarEvents());
            setUnscheduledQuestsHeight();
            calendarDayView.onMinuteChanged();
        });
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        getContext().unregisterReceiver(tickReceiver);
        super.onPause();
    }

    @Subscribe
    public void onMoveQuestToCalendarRequest(MoveQuestToCalendarRequestEvent e) {
        movingQuestPosition = e.position;
        movingViewModel = e.viewModel;
        CalendarEvent calendarEvent = new QuestCalendarEvent(e.viewModel.getQuest());
        calendarContainer.acceptNewEvent(calendarEvent);
        unscheduledQuestsAdapter.removeQuest(e.viewModel);
    }

    @Subscribe
    public void onEditCalendarEvent(EditCalendarEventEvent e) {
        calendarContainer.editView(e.calendarEventView);
    }

    @Subscribe
    public void onQuestAddedToCalendar(QuestAddedToCalendarEvent e) {
        QuestCalendarEvent qce = e.questCalendarEvent;
        Quest q = qce.getQuest();
        q.setStartMinute(qce.getStartMinute());
        questPersistenceService.save(q);
    }

    @Subscribe
    public void onUndoCompletedQuest(UndoCompletedQuestEvent e) {
        updateSchedule();
    }

    @Subscribe
    public void onQuestSnoozed(QuestSnoozedEvent e) {
        updateSchedule();
    }

    @Override
    public void onUnableToAcceptNewEvent(QuestCalendarEvent calendarEvent) {
        unscheduledQuestsAdapter.addQuest(movingQuestPosition, movingViewModel);
        setUnscheduledQuestsHeight();
    }

    @Override
    public void onAcceptEvent(QuestCalendarEvent calendarEvent) {
        if (calendarAdapter.canAddEvent(calendarEvent)) {
            eventBus.post(new QuestAddedToCalendarEvent(calendarEvent));
            calendarAdapter.addEvent(calendarEvent);
        } else {
            unscheduledQuestsAdapter.addQuest(movingQuestPosition, movingViewModel);
        }
        setUnscheduledQuestsHeight();
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        Quest q = e.quest;
        if (!DateUtils.isToday(q.getEndDate())) {
            return;
        }
        Time startTime = Quest.getStartTime(e.quest);
        if (startTime == null) {
            return;
        }
        calendarDayView.smoothScrollToTime(startTime);
    }

    private class CalendarScheduler {

        public Observable<Schedule> schedule() {

            return questPersistenceService.findAllForToday().flatMap(quests -> {
                List<QuestCalendarEvent> calendarEvents = new ArrayList<>();
                List<Quest> unscheduledQuests = new ArrayList<>();
                List<QuestCalendarEvent> completedEvents = new ArrayList<>();
                for (Quest q : quests) {

                    // completed events should be added first since we don't want them to intercept clicks
                    // for incomplete events

                    if (q.getCompletedAtDateTime() != null) {
                        QuestCalendarEvent event = new QuestCalendarEvent(q);
                        if (isNotScheduledForToday(q) || hasNoStartTime(q)) {
                            Calendar c = Calendar.getInstance();
                            Date completedAt = q.getCompletedAtDateTime();
                            c.setTime(completedAt);
                            c.add(Calendar.MINUTE, -event.getDuration());
                            // actual start time was yesterday, so yeah we do not include multi-day events
                            if (!DateUtils.isToday(c.getTime())) {
                                continue;
                            }
                            event.setStartMinute(Time.of(c.getTime()).toMinutesAfterMidnight());
                        }
                        completedEvents.add(event);
                    } else {
                        if (hasNoStartTime(q)) {
                            unscheduledQuests.add(q);
                        } else {
                            calendarEvents.add(new QuestCalendarEvent(q));
                        }
                    }
                }
                calendarEvents.addAll(0, completedEvents);
                return Observable.just(new Schedule(unscheduledQuests, calendarEvents));
            });
        }

        private boolean hasNoStartTime(Quest q) {
            return q.getStartMinute() < 0;
        }

        private boolean isNotScheduledForToday(Quest q) {
            return !DateUtils.isToday(q.getEndDate());
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
