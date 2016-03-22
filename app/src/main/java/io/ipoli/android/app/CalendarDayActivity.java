package io.ipoli.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.BottomBarUtil;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.ui.calendar.CalendarDayView;
import io.ipoli.android.app.ui.calendar.CalendarEvent;
import io.ipoli.android.app.ui.calendar.CalendarLayout;
import io.ipoli.android.app.ui.calendar.CalendarListener;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.Difficulty;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestCalendarAdapter;
import io.ipoli.android.quest.UnscheduledQuestsAdapter;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.activities.QuestCompleteActivity;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.QuestAddedToCalendarEvent;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.ui.QuestCalendarEvent;
import io.ipoli.android.quest.ui.events.EditCalendarEventEvent;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/16/16.
 */
public class CalendarDayActivity extends BaseActivity implements CalendarListener<QuestCalendarEvent> {

    @Inject
    QuestPersistenceService questPersistenceService;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.unscheduled_quests)
    RecyclerView unscheduledQuestList;

    @Bind(R.id.calendar)
    CalendarDayView calendarDayView;

    @Bind(R.id.calendar_container)
    CalendarLayout calendarContainer;

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

    private BottomBar bottomBar;

    class QuestDTO {
        private String name;
    }

    interface APIService {

        String API_ENDPOINT = "http://10.0.3.2:8080/v1/";

        @GET("schedules/{date}")
        Observable<List<QuestDTO>> getSchedule(@Path("date") String date, @Query("user_id") String userId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_day);
        bottomBar = BottomBarUtil.getBottomBar(this, savedInstanceState, BottomBarUtil.CALENDAR_TAB_INDEX);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(new SimpleDateFormat(getString(R.string.today_date_format), Locale.getDefault()).format(new Date()));
        }

        appComponent().inject(this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        unscheduledQuestList.setLayoutManager(layoutManager);

        calendarContainer.setCalendarListener(this);

        unscheduledQuestsAdapter = new UnscheduledQuestsAdapter(this, new ArrayList<Quest>(), eventBus);

        unscheduledQuestList.setAdapter(unscheduledQuestsAdapter);
        unscheduledQuestList.setNestedScrollingEnabled(false);

        calendarDayView.scrollToNow();

        calendarAdapter = new QuestCalendarAdapter(new ArrayList<QuestCalendarEvent>(), eventBus);
        calendarDayView.setAdapter(calendarAdapter);

//        Tutorial.getInstance(this).addItem(
//                new TutorialItem.Builder(this)
//                        .setState(Tutorial.State.TUTORIAL_START_OVERVIEW)
//                        .setTarget(((ViewGroup) tabLayout.getChildAt(0)).getChildAt(1))
//                        .enableDotAnimation(false)
//                        .setFocusType(Focus.MINIMUM)
//                        .build());
//
//        Tutorial.getInstance(this).addItem(
//                new TutorialItem.Builder(this)
//                        .setState(Tutorial.State.TUTORIAL_START_ADD_QUEST)
//                        .setTarget(findViewById(R.id.add_quest))
//                        .enableDotAnimation(true)
//                        .performClick(true)
//                        .setFocusType(Focus.MINIMUM)
//                        .build());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIService.API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        APIService apiService = retrofit.create(APIService.class);
        apiService.getSchedule("2016-03-22", "123").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(questDTOs -> {
            Log.d("OnNext", questDTOs.toString());
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        bottomBar.onSaveInstanceState(outState);
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

    @Subscribe
    public void onShowQuestEvent(ShowQuestEvent e) {
        Intent i = new Intent(this, QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivity(i);
    }

    @Subscribe
    public void onQuestCompleteRequest(CompleteQuestRequestEvent e) {
        Intent i = new Intent(this, QuestCompleteActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivityForResult(i, Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE);
    }

    @Subscribe
    public void onUndoCompletedQuestRequest(UndoCompletedQuestRequestEvent e) {
        Quest quest = e.quest;
        quest.setLog("");
        quest.setDifficulty(Difficulty.UNKNOWN.name());
        quest.setActualStartDateTime(null);
        quest.setMeasuredDuration(0);
        quest.setCompletedAtDateTime(null);
        quest = questPersistenceService.save(quest);
        eventBus.post(new UndoCompletedQuestEvent(quest));
        Toast.makeText(this, "Quest undone", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        updateSchedule();
    }

    private void updateSchedule() {
        Schedule schedule = new CalendarScheduler().schedule();
        unscheduledQuestsAdapter.updateQuests(schedule.getUnscheduledQuests());
        calendarAdapter.updateEvents(schedule.getCalendarEvents());
        setUnscheduledQuestsHeight();
        calendarDayView.onMinuteChanged();
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        unregisterReceiver(tickReceiver);
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
    public void onEditCalendarEvent(EditCalendarEventEvent e) {
        calendarContainer.editView(e.calendarEventView);
    }

    @Subscribe
    public void onQuestAddedToCalendar(QuestAddedToCalendarEvent e) {
        QuestCalendarEvent qce = e.questCalendarEvent;
        Quest q = qce.getQuest();
        q.setStartTime(qce.getStartTime());
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

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        Quest q = e.quest;
        if (!DateUtils.isToday(q.getDue())) {
            return;
        }
        Time startTime = Quest.getStartTime(e.quest);
        if (startTime == null) {
            return;
        }
        calendarDayView.smoothScrollToTime(startTime);
    }

    private class CalendarScheduler {

        public Schedule schedule() {
            List<QuestCalendarEvent> calendarEvents = new ArrayList<>();

            List<Quest> completedTodayQuests = questPersistenceService.findAllCompletedToday();

            // completed events should be added first since we don't want them to intercept clicks
            // for incomplete events
            for (Quest q : completedTodayQuests) {
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

        private boolean hasNoStartTime(Quest q) {
            return q.getStartTime() == null;
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