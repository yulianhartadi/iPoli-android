package io.ipoli.android.quest.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.app.ui.calendar.CalendarLayout;
import io.ipoli.android.app.ui.calendar.CalendarListener;
import io.ipoli.android.app.ui.events.HideLoaderEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.adapters.QuestCalendarAdapter;
import io.ipoli.android.quest.adapters.UnscheduledQuestsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CompletePlaceholderRequestEvent;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.events.QuestDraggedEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.UndoQuestForThePast;
import io.ipoli.android.quest.events.UnscheduledQuestDraggedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.schedulers.PersistentRepeatingQuestScheduler;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.quest.ui.events.EditCalendarEventEvent;
import io.ipoli.android.quest.viewmodels.QuestCalendarViewModel;
import io.ipoli.android.quest.viewmodels.UnscheduledQuestViewModel;

public class DayViewFragment extends BaseFragment implements CalendarListener<QuestCalendarViewModel> {

    public static final String CURRENT_DATE = "current_date";

    @Inject
    protected Bus eventBus;

    @BindView(R.id.unscheduled_quests)
    RecyclerView unscheduledQuestList;

    @BindView(R.id.calendar)
    CalendarDayView calendarDayView;

    @BindView(R.id.calendar_container)
    CalendarLayout calendarContainer;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    RepeatingQuestScheduler repeatingQuestScheduler;

    @Inject
    PersistentRepeatingQuestScheduler persistentRepeatingQuestScheduler;

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

    private LocalDate currentDate;
    private Unbinder unbinder;

    List<Quest> futureQuests = new ArrayList<>();
    List<Quest> futurePlaceholderQuests = new ArrayList<>();

    public static DayViewFragment newInstance(LocalDate date) {
        DayViewFragment fragment = new DayViewFragment();
        Bundle args = new Bundle();
        args.putLong(CURRENT_DATE, date.toDate().getTime());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            currentDate = new LocalDate(getArguments().getLong(CURRENT_DATE));
        } else {
            currentDate = new LocalDate();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_day_view, container, false);

        App.getAppComponent(getContext()).inject(this);
        unbinder = ButterKnife.bind(this, view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        unscheduledQuestList.setLayoutManager(layoutManager);

        calendarContainer.setCalendarListener(this);

        unscheduledQuestsAdapter = new UnscheduledQuestsAdapter(getContext(), new ArrayList<>(), eventBus);

        unscheduledQuestList.setAdapter(unscheduledQuestsAdapter);
        unscheduledQuestList.setNestedScrollingEnabled(false);

        calendarAdapter = new QuestCalendarAdapter(new ArrayList<>(), eventBus);
        calendarDayView.setAdapter(calendarAdapter);
        calendarDayView.scrollToNow();

        if (!currentDate.isEqual(new LocalDate())) {
            calendarDayView.hideTimeLine();
        }

        if (currentDateIsInThePast()) {
            questPersistenceService.listenForAllNonAllDayCompletedForDate(currentDate, this::questsForPastUpdated);
        } else if (currentDateIsInTheFuture()) {

            repeatingQuestPersistenceService.listenForNonFlexibleNonAllDayActiveRepeatingQuests(repeatingQuests -> {
                getPlaceholderQuestsFromRepeatingQuests(repeatingQuests, quests -> {
                    futurePlaceholderQuests = quests;
                    questsForFutureUpdated();
                });
            });

            questPersistenceService.listenForAllNonAllDayIncompleteForDate(currentDate, quests -> {
                futureQuests = quests;
                questsForFutureUpdated();
            });
        } else {
            questPersistenceService.listenForAllNonAllDayForDate(currentDate, this::questsForPresentUpdated);
        }

        return view;
    }

    private boolean currentDateIsInThePast() {
        return currentDate.isBefore(new LocalDate());
    }

    private void getPlaceholderQuestsFromRepeatingQuests(List<RepeatingQuest> repeatingQuests, PlaceholderQuestsListener listener) {
        List<Quest> res = new ArrayList<>();
        for (int i = 0; i < repeatingQuests.size(); i++) {
            RepeatingQuest rq = repeatingQuests.get(i);
            boolean isLast = i == repeatingQuests.size() - 1;
            questPersistenceService.countAllForRepeatingQuest(rq, currentDate, currentDate, createdQuestsCount -> {
                if (createdQuestsCount == 0) {
                    List<Quest> questsToCreate = repeatingQuestScheduler.scheduleForDateRange(rq,
                            DateUtils.toStartOfDayUTC(currentDate),
                            DateUtils.toStartOfDayUTC(currentDate));
                    res.addAll(questsToCreate);
                }
                if (isLast) {
                    for (Quest q : res) {
                        q.setPlaceholder(true);
                    }
                    listener.onPlaceholderQuestsCreated(res);
                }
            });
        }
    }

    private void questsForFutureUpdated() {
        List<Quest> quests = new ArrayList<>();
        quests.addAll(futureQuests);
        quests.addAll(futurePlaceholderQuests);
        List<QuestCalendarViewModel> calendarEvents = new ArrayList<>();
        List<Quest> unscheduledQuests = new ArrayList<>();
        for (Quest q : quests) {
            if (hasNoStartTime(q)) {
                unscheduledQuests.add(q);
            } else {
                QuestCalendarViewModel event = new QuestCalendarViewModel(q);
                calendarEvents.add(event);
            }
        }
        updateSchedule(new Schedule(unscheduledQuests, calendarEvents));
    }

    private void questsForPresentUpdated(List<Quest> quests) {
        List<QuestCalendarViewModel> calendarEvents = new ArrayList<>();
        List<Quest> unscheduledQuests = new ArrayList<>();
        List<QuestCalendarViewModel> completedEvents = new ArrayList<>();
        for (Quest q : quests) {
            // completed events should be added first since we don't want them to intercept clicks
            // for incomplete events

            if (q.getCompletedAtDate() != null) {
                QuestCalendarViewModel event = new QuestCalendarViewModel(q);
                if (hasNoStartTime(q) || new LocalDate().isBefore(new LocalDate(q.getEndDate()))) {
                    event.setStartMinute(getStartTimeForUnscheduledQuest(q).toMinutesAfterMidnight());
                }

                completedEvents.add(event);
            } else if (hasNoStartTime(q)) {
                unscheduledQuests.add(q);
            } else {
                calendarEvents.add(new QuestCalendarViewModel(q));
            }
        }
        calendarEvents.addAll(0, completedEvents);
        updateSchedule(new Schedule(unscheduledQuests, calendarEvents));
    }

    private void questsForPastUpdated(List<Quest> quests) {
        List<QuestCalendarViewModel> calendarEvents = new ArrayList<>();
        for (Quest q : quests) {
            QuestCalendarViewModel event = new QuestCalendarViewModel(q);
            if (hasNoStartTime(q)) {
                event.setStartMinute(getStartTimeForUnscheduledQuest(q).toMinutesAfterMidnight());
            }
            calendarEvents.add(event);
            updateSchedule(new Schedule(new ArrayList<>(), calendarEvents));
        }
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        questPersistenceService.removeAllListeners();
        repeatingQuestPersistenceService.removeAllListeners();
        super.onDestroyView();
    }

    @Subscribe
    public void onCompletePlaceholderRequest(CompletePlaceholderRequestEvent e) {
        Quest quest = savePlaceholderQuest(e.quest);
        eventBus.post(new CompleteQuestRequestEvent(quest, e.source));
    }

    @Subscribe
    public void onCompleteUnscheduledQuestRequest(CompleteUnscheduledQuestRequestEvent e) {
        Quest quest = e.viewModel.getQuest();
        if (quest.isPlaceholder()) {
            quest = savePlaceholderQuest(quest);
        }
        eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.CALENDAR_UNSCHEDULED_SECTION));
        calendarDayView.smoothScrollToTime(Time.now());
    }

    @Subscribe
    public void onUndoQuestForThePast(UndoQuestForThePast e) {
        Toast.makeText(getContext(), "Quest moved to Inbox", Toast.LENGTH_LONG).show();
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
        if (!isResumed()) {
            return;
        }

        try {
            if (isVisibleToUser) {
                eventBus.register(this);
            } else {
                eventBus.unregister(this);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            try {
                eventBus.register(this);
            } catch (Exception ignored) {
            }
        }
        calendarDayView.onMinuteChanged();
        getContext().registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    void updateSchedule(Schedule schedule) {
        if (calendarContainer == null || calendarContainer.isInEditMode()) {
            return;
        }
        List<UnscheduledQuestViewModel> unscheduledViewModels = new ArrayList<>();

        Map<String, List<Quest>> map = new HashMap<>();
        for (Quest q : schedule.getUnscheduledQuests()) {
            if (q.getRepeatingQuest() == null) {
                unscheduledViewModels.add(new UnscheduledQuestViewModel(q, 1));
                continue;
            }
            String key = q.getRepeatingQuest().getId();
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
    }

    @Override
    public void onPause() {
        eventBus.post(new HideLoaderEvent());
        getContext().unregisterReceiver(tickReceiver);
        if (getUserVisibleHint()) {
            eventBus.unregister(this);
        }
        super.onPause();
    }

    @Subscribe
    public void onMoveQuestToCalendarRequest(MoveQuestToCalendarRequestEvent e) {
        eventBus.post(new UnscheduledQuestDraggedEvent(e.viewModel.getQuest()));
        movingQuestPosition = e.position;
        movingViewModel = e.viewModel;
        CalendarEvent calendarEvent = new QuestCalendarViewModel(e.viewModel.getQuest());
        calendarContainer.acceptNewEvent(calendarEvent);
        unscheduledQuestsAdapter.removeQuest(e.viewModel);
    }

    @Subscribe
    public void onEditCalendarEvent(EditCalendarEventEvent e) {
        eventBus.post(new QuestDraggedEvent(e.quest));
        calendarContainer.editView(e.calendarEventView);
    }

    @Subscribe
    public void onQuestAddedToCalendar(QuestAddedToCalendarEvent e) {
        QuestCalendarViewModel qvm = e.questCalendarViewModel;
        Quest q = qvm.getQuest();
        if (q.isPlaceholder()) {
            q.setId(savePlaceholderQuest(q).getId());
        }

        q.setStartMinute(qvm.getStartMinute());
        saveQuest(q);
    }

    @Override
    public void onUnableToAcceptNewEvent(QuestCalendarViewModel calendarEvent) {
        unscheduledQuestsAdapter.addQuest(movingQuestPosition, movingViewModel);
        setUnscheduledQuestsHeight();
    }

    private void saveQuest(Quest q) {
        questPersistenceService.save(q);
    }

    @Override
    public void onAcceptEvent(QuestCalendarViewModel calendarEvent) {
        eventBus.post(new QuestAddedToCalendarEvent(calendarEvent));
        setUnscheduledQuestsHeight();
    }

    @Subscribe
    public void onShowQuestEvent(ShowQuestEvent e) {
        Quest quest = e.quest;
        if (quest.isPlaceholder()) {
            quest = savePlaceholderQuest(quest);
        }
        Intent i = new Intent(getActivity(), QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, quest.getId());
        startActivity(i);
    }

    private Quest savePlaceholderQuest(Quest quest) {
        LocalDate startOfWeek = currentDate.dayOfWeek().withMinimumValue();
        return persistentRepeatingQuestScheduler.schedulePlaceholderQuest(quest, quest.getRepeatingQuest(), startOfWeek);
    }

    private Time getStartTimeForUnscheduledQuest(Quest q) {
        int duration = q.isIndicator() ? 3 : Math.max(q.getActualDuration(), Constants.CALENDAR_EVENT_MIN_DURATION);
        return Time.of(Math.max(q.getCompletedAtMinute() - duration, 0));
    }

    private boolean currentDateIsInTheFuture() {
        return currentDate.isAfter(new LocalDate());
    }

    private boolean hasNoStartTime(Quest q) {
        return q.getStartMinute() < 0;
    }

    private class Schedule {
        private List<Quest> unscheduledQuests;
        private List<QuestCalendarViewModel> calendarEvents;

        private Schedule(List<Quest> unscheduledQuests, List<QuestCalendarViewModel> calendarEvents) {
            this.unscheduledQuests = unscheduledQuests;
            this.calendarEvents = calendarEvents;
        }

        public List<Quest> getUnscheduledQuests() {
            return unscheduledQuests;
        }

        public List<QuestCalendarViewModel> getCalendarEvents() {
            return calendarEvents;
        }
    }

    private interface PlaceholderQuestsListener {
        void onPlaceholderQuestsCreated(List<Quest> quests);
    }
}