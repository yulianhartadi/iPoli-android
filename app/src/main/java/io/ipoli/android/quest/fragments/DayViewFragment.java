package io.ipoli.android.quest.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.StartQuickAddEvent;
import io.ipoli.android.app.scheduling.DiscreteDistribution;
import io.ipoli.android.app.scheduling.PosteriorEstimator;
import io.ipoli.android.app.scheduling.ProbabilisticTaskScheduler;
import io.ipoli.android.app.scheduling.Task;
import io.ipoli.android.app.scheduling.TimeBlock;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.app.ui.calendar.CalendarLayout;
import io.ipoli.android.app.ui.calendar.CalendarListener;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.adapters.QuestCalendarAdapter;
import io.ipoli.android.quest.adapters.UnscheduledQuestsAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.QuestDraggedEvent;
import io.ipoli.android.quest.events.RescheduleQuestEvent;
import io.ipoli.android.quest.events.ScrollToTimeEvent;
import io.ipoli.android.quest.events.SuggestionAcceptedEvent;
import io.ipoli.android.quest.events.UnscheduledQuestDraggedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.quest.ui.events.EditCalendarEventEvent;
import io.ipoli.android.quest.viewmodels.QuestCalendarViewModel;
import io.ipoli.android.quest.viewmodels.UnscheduledQuestViewModel;

public class DayViewFragment extends BaseFragment implements CalendarListener<QuestCalendarViewModel>, CalendarDayView.OnHourCellLongClickListener {

    public static final String CURRENT_DATE = "current_date";

    @Inject
    protected Bus eventBus;

    @Inject
    LocalStorage localStorage;

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
    private PosteriorEstimator posteriorEstimator;

    public static DayViewFragment newInstance(LocalDate date) {
        DayViewFragment fragment = new DayViewFragment();
        Bundle args = new Bundle();
        args.putLong(CURRENT_DATE, DateUtils.toMillis(date));
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
            currentDate = DateUtils.fromMillis(getArguments().getLong(CURRENT_DATE));
        } else {
            currentDate = LocalDate.now();
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

        Player player = getPlayer();

        calendarContainer.setCalendarListener(this);
        calendarContainer.setTimeFormat(player.getUse24HourFormat());

        unscheduledQuestsAdapter = new UnscheduledQuestsAdapter(getContext(), new ArrayList<>(), eventBus);

        unscheduledQuestList.setAdapter(unscheduledQuestsAdapter);
        unscheduledQuestList.setNestedScrollingEnabled(false);

        calendarAdapter = new QuestCalendarAdapter(new ArrayList<>(), player.getUse24HourFormat(), eventBus);
        calendarDayView.setTimeFormat(player.getUse24HourFormat());
        calendarDayView.setAdapter(calendarAdapter);
        calendarDayView.setOnHourCellLongClickListener(this);
        calendarDayView.scrollToNow();

        PosteriorEstimator.PosteriorSettings posteriorSettings = PosteriorEstimator.PosteriorSettings.create()
                .setWorkDays(player.getWorkDays())
                .setSleepStartMinute(player.getSleepStartMinute())
                .setSleepEndMinute(player.getSleepEndMinute())
                .setWorkStartMinute(player.getWorkStartMinute())
                .setWorkEndMinute(player.getWorkEndMinute())
                .setMostProductiveTimesOfDay(player.getMostProductiveTimesOfDay());

        posteriorEstimator = new PosteriorEstimator(posteriorSettings, currentDate, new Random(Constants.RANDOM_SEED));

        if (!currentDate.isEqual(LocalDate.now())) {
            calendarDayView.hideTimeLine();
        }

        if (currentDateIsInThePast()) {
            questPersistenceService.listenForAllNonAllDayCompletedForDate(currentDate, this::questsForPastUpdated);
        } else if (currentDateIsInTheFuture()) {

            repeatingQuestPersistenceService.listenForNonFlexibleNonAllDayActiveRepeatingQuests(repeatingQuests -> {
                futurePlaceholderQuests = getPlaceholderQuestsFromRepeatingQuests(repeatingQuests);
                questsForFutureUpdated();
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
        return currentDate.isBefore(LocalDate.now());
    }

    private List<Quest> getPlaceholderQuestsFromRepeatingQuests(List<RepeatingQuest> repeatingQuests) {
        List<Quest> res = new ArrayList<>();
        for (RepeatingQuest rq : repeatingQuests) {
            if (!rq.isScheduledForDate(currentDate)) {
                List<Quest> placeholderQuests = repeatingQuestScheduler.scheduleForDay(rq, currentDate);
                for (Quest quest : placeholderQuests) {
                    quest.setPlaceholder(true);
                }
                res.addAll(placeholderQuests);
            }
        }
        return res;
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
                if (hasNoStartTime(q) || LocalDate.now().isBefore(q.getScheduledDate())) {
                    event.setStartMinute(getStartTimeForUnscheduledQuest(q).toMinuteOfDay());
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
                event.setStartMinute(getStartTimeForUnscheduledQuest(q).toMinuteOfDay());
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
    public void onCompleteUnscheduledQuestRequest(CompleteUnscheduledQuestRequestEvent e) {
        eventBus.post(new CompleteQuestRequestEvent(e.viewModel.getQuest(), EventSource.CALENDAR_UNSCHEDULED_SECTION));
        if (e.viewModel.getQuest().isCompleted()) {
            calendarDayView.smoothScrollToTime(Time.now());
        }
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
        Collections.sort(schedule.getUnscheduledQuests(), (q1, q2) ->
                -Integer.compare(q1.getDuration(), q2.getDuration()));
        List<UnscheduledQuestViewModel> unscheduledViewModels = new ArrayList<>();
        List<QuestCalendarViewModel> scheduledEvents = schedule.getCalendarEvents();

        List<Task> tasks = new ArrayList<>();
        for (QuestCalendarViewModel vm : schedule.getCalendarEvents()) {
            tasks.add(new Task(vm.getStartMinute(), vm.getDuration()));
        }

        ProbabilisticTaskScheduler probabilisticTaskScheduler = new ProbabilisticTaskScheduler(0, 24, tasks, new Random(Constants.RANDOM_SEED));

        List<QuestCalendarViewModel> proposedEvents = new ArrayList<>();
        for (Quest q : schedule.getUnscheduledQuests()) {
            unscheduledViewModels.add(new UnscheduledQuestViewModel(q));
            if (!q.shouldBeDoneMultipleTimesPerDay()) {
                proposeSlotForQuest(scheduledEvents, probabilisticTaskScheduler, proposedEvents, q);
            }
        }

        unscheduledQuestsAdapter.updateQuests(unscheduledViewModels);
        calendarAdapter.updateEvents(scheduledEvents);

        setUnscheduledQuestsHeight();
        calendarDayView.onMinuteChanged();
    }

    private void proposeSlotForQuest(List<QuestCalendarViewModel> scheduledEvents, ProbabilisticTaskScheduler probabilisticTaskScheduler, List<QuestCalendarViewModel> proposedEvents, Quest q) {
        DiscreteDistribution posterior = posteriorEstimator.posteriorFor(q);

        List<TimeBlock> timeBlocks = probabilisticTaskScheduler.chooseSlotsFor(new Task(q.getDuration()), 15, Time.now(), posterior);

        TimeBlock timeBlock = chooseNonOverlappingTimeBlock(proposedEvents, timeBlocks);

        if (timeBlock != null) {
            timeBlocks.remove(0);
            QuestCalendarViewModel vm = QuestCalendarViewModel.createWithProposedTime(q, timeBlock.getStartMinute(), timeBlocks);
            scheduledEvents.add(vm);
            proposedEvents.add(vm);
        }
    }

    @Nullable
    private TimeBlock chooseNonOverlappingTimeBlock(List<QuestCalendarViewModel> proposedEvents, List<TimeBlock> timeBlocks) {
        for (TimeBlock tb : timeBlocks) {
            if (!doOverlap(proposedEvents, tb)) {
                return tb;
            }
        }
        return null;
    }

    private boolean doOverlap(List<QuestCalendarViewModel> proposedEvents, TimeBlock tb) {
        for (QuestCalendarViewModel vm : proposedEvents) {
            int sm = vm.getStartMinute();
            int em = vm.getStartMinute() + vm.getDuration() - 1;

            if (tb.doOverlap(sm, em)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {
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
        q.setStartMinute(qvm.getStartMinute());
        saveQuest(q);
    }

    @Subscribe
    public void onSuggestionAccepted(SuggestionAcceptedEvent e) {
        Quest quest = e.quest;
        quest.setStartMinute(e.startMinute);
        saveQuest(quest);
        Toast.makeText(getContext(), "Suggestion accepted", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onRescheduleQuest(RescheduleQuestEvent e) {
        if (e.calendarEvent.useNextSlot(calendarAdapter.getEventsWithProposedSlots())) {
            calendarAdapter.notifyDataSetChanged();
            calendarDayView.smoothScrollToTime(Time.of(e.calendarEvent.getStartMinute()));
        } else {
            Toast.makeText(getContext(), "No more suggestions", Toast.LENGTH_SHORT).show();
        }
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
    public void onScrollToTime(ScrollToTimeEvent e) {
        calendarDayView.smoothScrollToTime(e.time);
    }

    private Time getStartTimeForUnscheduledQuest(Quest q) {
        int duration = Math.max(q.getActualDuration(), Constants.CALENDAR_EVENT_MIN_DURATION);
        return Time.of(Math.max(q.getCompletedAtMinute() - duration, 0));
    }

    private boolean currentDateIsInTheFuture() {
        return currentDate.isAfter(LocalDate.now());
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        Quest quest = e.quest;
        int completeMinute = quest.hasStartTime() ? quest.getStartMinute() : quest.getCompletedAtMinute();
        calendarDayView.smoothScrollToTime(Time.of(completeMinute));
    }

    private boolean hasNoStartTime(Quest q) {
        return q.getStartMinute() == null;
    }

    @Override
    public void onLongClickHourCell(Time atTime) {
        if (currentDateIsInThePast()) {
            return;
        }

        String dateText;
        if (currentDate.isEqual(LocalDate.now())) {
            dateText = "today";
        } else if (currentDate.isEqual(LocalDate.now().plusDays(1))) {
            dateText = "tomorrow";
        } else {
            dateText = "on " + DateFormatter.formatWithoutYear(currentDate);
        }

        eventBus.post(new StartQuickAddEvent(" at " + atTime.toString() + " " + dateText));
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