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
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
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
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.scheduling.SchedulingAPIService;
import io.ipoli.android.app.scheduling.dto.FindSlotsRequest;
import io.ipoli.android.app.scheduling.dto.Slot;
import io.ipoli.android.app.scheduling.dto.Task;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.app.ui.calendar.CalendarLayout;
import io.ipoli.android.app.ui.calendar.CalendarListener;
import io.ipoli.android.app.ui.events.HideLoaderEvent;
import io.ipoli.android.app.ui.events.ShowLoaderEvent;
import io.ipoli.android.app.ui.events.SuggestionsUnavailableEvent;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.adapters.QuestCalendarAdapter;
import io.ipoli.android.quest.adapters.UnscheduledQuestsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.events.QuestDraggedEvent;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.events.RescheduleQuestEvent;
import io.ipoli.android.quest.events.ScheduleQuestRequestEvent;
import io.ipoli.android.quest.events.SuggestionAcceptedEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.events.UnscheduledQuestDraggedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.ui.events.EditCalendarEventEvent;
import io.ipoli.android.quest.viewmodels.QuestCalendarViewModel;
import io.ipoli.android.quest.viewmodels.UnscheduledQuestViewModel;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class DayViewFragment extends Fragment implements CalendarListener<QuestCalendarViewModel> {

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
    SchedulingAPIService schedulingAPIService;

    private int movingQuestPosition;

    private UnscheduledQuestViewModel movingViewModel;
    private UnscheduledQuestsAdapter unscheduledQuestsAdapter;
    private QuestCalendarAdapter calendarAdapter;

    private CompositeSubscription findSlotsSubscriptions;

    BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            calendarDayView.onMinuteChanged();
        }
    };
    private LocalDate currentDate;
    private Unbinder unbinder;

    public static DayViewFragment newInstance(LocalDate date) {
        DayViewFragment fragment = new DayViewFragment();
        Bundle args = new Bundle();
        args.putLong(CURRENT_DATE, date.toDate().getTime());
        fragment.setArguments(args);
        return fragment;
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

        View view = inflater.inflate(R.layout.fragment_day_view, container, false);

        App.getAppComponent(getContext()).inject(this);
        unbinder = ButterKnife.bind(this, view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        unscheduledQuestList.setLayoutManager(layoutManager);

        calendarContainer.setCalendarListener(this);

        Time.RelativeTime relativeTime = Time.RelativeTime.PRESENT;
        if (currentDate.isBefore(new LocalDate())) {
            relativeTime = Time.RelativeTime.PAST;
        } else if (currentDate.isAfter(new LocalDate())) {
            relativeTime = Time.RelativeTime.FUTURE;
        }

        unscheduledQuestsAdapter = new UnscheduledQuestsAdapter(getContext(), new ArrayList<>(), eventBus, relativeTime);

        unscheduledQuestList.setAdapter(unscheduledQuestsAdapter);
        unscheduledQuestList.setNestedScrollingEnabled(false);

        calendarAdapter = new QuestCalendarAdapter(new ArrayList<>(), eventBus, relativeTime);
        calendarDayView.setAdapter(calendarAdapter);
        calendarDayView.scrollToNow();

        if (relativeTime == Time.RelativeTime.PRESENT) {
            calendarDayView.showTimeLine();
        } else {
            calendarDayView.hideTimeLine();
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Subscribe
    public void onCompleteUnscheduledQuestRequest(CompleteUnscheduledQuestRequestEvent e) {
        eventBus.post(new CompleteQuestRequestEvent(e.viewModel.getQuest(), EventSource.CALENDAR_UNSCHEDULED_SECTION));
        calendarDayView.smoothScrollToTime(Time.now());
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
        quest.setDifficulty(null);
        quest.setActualStart(null);
        quest.setCompletedAt(null);
        quest.setCompletedAtMinute(null);
        questPersistenceService.save(quest).subscribe(q -> {
            eventBus.post(new UndoCompletedQuestEvent(q));
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isResumed()) {
            return;
        }

        if (isVisibleToUser) {
            eventBus.register(this);
        } else {
            eventBus.unregister(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            eventBus.register(this);
        }
        getContext().registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        findSlotsSubscriptions = new CompositeSubscription();
        updateSchedule();
    }

    private void updateSchedule() {
        if (calendarContainer.isInEditMode()) {
            return;
        }
        new CalendarScheduler().schedule().subscribe(schedule -> {

            List<UnscheduledQuestViewModel> unscheduledViewModels = new ArrayList<>();

            Map<String, List<Quest>> map = new HashMap<>();
            for (Quest q : schedule.getUnscheduledQuests()) {
                if (q.getHabit() == null) {
                    unscheduledViewModels.add(new UnscheduledQuestViewModel(q, 1));
                    continue;
                }
                String key = q.getHabit().getId();
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
        findSlotsSubscriptions.unsubscribe();
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
        QuestCalendarViewModel qce = e.questCalendarViewModel;
        Quest q = qce.getQuest();
        q.setStartMinute(qce.getStartMinute());
        questPersistenceService.save(q).subscribe();
    }

    @Subscribe
    public void onQuestSnoozed(QuestSnoozedEvent e) {
        updateSchedule();
    }

    @Override
    public void onUnableToAcceptNewEvent(QuestCalendarViewModel calendarEvent) {
        unscheduledQuestsAdapter.addQuest(movingQuestPosition, movingViewModel);
        setUnscheduledQuestsHeight();
    }

    @Subscribe
    public void onScheduleQuestRequest(ScheduleQuestRequestEvent e) {
        UnscheduledQuestViewModel vm = e.viewModel;
        Quest q = vm.getQuest();
        List<Task> scheduledTasks = new ArrayList<>();
        for (QuestCalendarViewModel cvm : calendarAdapter.getEvents()) {
            Quest cq = cvm.getQuest();
            scheduledTasks.add(new Task(cq.getStartMinute(), cq.getDuration(), cq.getContext()));
        }
        Task taskToSchedule = new Task(Math.max(q.getDuration(), 15), q.getContext());
        FindSlotsRequest request = new FindSlotsRequest(scheduledTasks, taskToSchedule);

        eventBus.post(new ShowLoaderEvent(getString(R.string.find_slots_loading_message)));
        Subscription subscription = schedulingAPIService.findSlots(request, Constants.SUGGESTED_SLOTS_COUNT).compose(applyAPISchedulers()).subscribe(slots -> {
            if (slots.isEmpty()) {
                Toast.makeText(getContext(), "No slots available", Toast.LENGTH_SHORT);
                return;
            }
            unscheduledQuestsAdapter.removeQuest(vm);
            setUnscheduledQuestsHeight();
            Slot slot = slots.get(0);
            calendarDayView.smoothScrollToTime(Time.of(slot.startMinute));
            QuestCalendarViewModel event = new QuestCalendarViewModel(q, slots);
            event.setStartMinute(slot.startMinute);
            event.setDuration(Math.max(15, q.getDuration()));
            calendarAdapter.addEvent(event);
        }, (throwable) -> {
            eventBus.post(new SuggestionsUnavailableEvent(q));
            eventBus.post(new HideLoaderEvent());
            Toast.makeText(getContext(), "Unable to find slots, try later", Toast.LENGTH_SHORT).show();
        }, () -> eventBus.post(new HideLoaderEvent()));
        findSlotsSubscriptions.add(subscription);
    }

    @Subscribe
    public void onRescheduleQuest(RescheduleQuestEvent e) {
        QuestCalendarViewModel viewModel = e.calendarEvent;
        Slot slot = viewModel.nextSlot();
        calendarDayView.smoothScrollToTime(Time.of(slot.startMinute));
        viewModel.setStartMinute(slot.startMinute);
        calendarAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onSuggestionAccepted(SuggestionAcceptedEvent e) {
        QuestCalendarViewModel viewModel = e.calendarEvent;
        Quest q = viewModel.getQuest();
        q.setStartMinute(viewModel.getStartMinute());
        questPersistenceService.save(q).subscribe(quest -> {
            Toast.makeText(getContext(), "Suggestion accepted", Toast.LENGTH_SHORT).show();
            updateSchedule();
        });
    }

    private <T> Observable.Transformer<T, T> applyAPISchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void onAcceptEvent(QuestCalendarViewModel calendarEvent) {
        if (calendarAdapter.canAddEvent(calendarEvent)) {
            eventBus.post(new QuestAddedToCalendarEvent(calendarEvent));
        } else {
            unscheduledQuestsAdapter.addQuest(movingQuestPosition, movingViewModel);
        }
        setUnscheduledQuestsHeight();
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        Quest q = e.quest;
        if (!q.isScheduledFor(currentDate)) {
            return;
        }
        updateSchedule();
    }

    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        updateSchedule();
    }

    private Time getStartTimeForUnscheduledQuest(Quest q) {
        int duration = q.isIndicator() ? 3 : Math.max(q.getDuration(), Constants.QUEST_CALENDAR_EVENT_MIN_DURATION);
        return Time.of(Math.max(q.getCompletedAtMinute() - duration, 0));
    }

    private class CalendarScheduler {

        public Observable<Schedule> schedule() {

            if (currentDate.isBefore(new LocalDate())) {
                return questPersistenceService.findAllNonAllDayCompletedForDate(currentDate).flatMap(quests -> {
                    List<QuestCalendarViewModel> calendarEvents = new ArrayList<>();
                    for (Quest q : quests) {
                        QuestCalendarViewModel event = new QuestCalendarViewModel(q);
                        if (hasNoStartTime(q)) {
                            event.setStartMinute(getStartTimeForUnscheduledQuest(q).toMinutesAfterMidnight());
                        }
                        calendarEvents.add(event);
                    }
                    return Observable.just(new Schedule(new ArrayList<>(), calendarEvents));
                });
            }

            if (currentDate.isAfter(new LocalDate())) {
                return questPersistenceService.findAllNonAllDayIncompleteForDate(currentDate).flatMap(quests -> {
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
                    return Observable.just(new Schedule(unscheduledQuests, calendarEvents));
                });
            }

            return questPersistenceService.findAllNonAllDayForDate(currentDate).flatMap(quests -> {
                List<QuestCalendarViewModel> calendarEvents = new ArrayList<>();
                List<Quest> unscheduledQuests = new ArrayList<>();
                List<QuestCalendarViewModel> completedEvents = new ArrayList<>();
                for (Quest q : quests) {
                    // completed events should be added first since we don't want them to intercept clicks
                    // for incomplete events

                    if (q.getCompletedAt() != null) {
                        QuestCalendarViewModel event = new QuestCalendarViewModel(q);
                        if (hasNoStartTime(q) || new Date().before(q.getEndDate())) {
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
                return Observable.just(new Schedule(unscheduledQuests, calendarEvents));
            });
        }

        private boolean hasNoStartTime(Quest q) {
            return q.getStartMinute() < 0;
        }
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
}