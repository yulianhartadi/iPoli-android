package io.ipoli.android.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.jobs.RemindPlanDayJob;
import io.ipoli.android.app.jobs.RemindReviewDayJob;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.assistant.AssistantService;
import io.ipoli.android.player.LevelUpActivity;
import io.ipoli.android.player.PlayerService;
import io.ipoli.android.player.events.PlayerLevelUpEvent;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.Status;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class App extends Application {

    private static AppComponent appComponent;

    @Inject
    Bus eventBus;

    @Inject
    AnalyticsService analyticsService;

    @Inject
    AssistantService assistantService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    PlayerService playerService;

    @Override
    public void onCreate() {
        super.onCreate();
        getAppComponent(this).inject(this);
        resetDueDateForIncompleteQuests();
        registerServices();
        initPlanDayReminder();
        initReviewDayReminder();
        sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
        if (questPersistenceService.findAllPlannedForToday().isEmpty()) {

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
            quests.add(qqqq);
            quests.add(qqqqq);

            Quest tq = new Quest("Work on presentation", Status.PLANNED.name(), new Date());
            Quest.setContext(tq, QuestContext.WORK);
            calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, 13);
            calendar.set(Calendar.MINUTE, 0);
            tq.setStartTime(calendar.getTime());
            tq.setDuration(120);

            Calendar c = Calendar.getInstance();
            c.setTime(tq.getDue());
            c.add(Calendar.DAY_OF_YEAR, 1);
            tq.setDue(c.getTime());

            quests.add(tq);

            Quest uq = new Quest("Jump on a single foot", Status.PLANNED.name(), new Date());
            Quest.setContext(uq, QuestContext.WELLNESS);
            calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 15);
            uq.setStartTime(calendar.getTime());
            uq.setDuration(15);

            c = Calendar.getInstance();
            c.setTime(uq.getDue());
            c.add(Calendar.DAY_OF_YEAR, 2);
            uq.setDue(c.getTime());
            quests.add(uq);

            questPersistenceService.saveAll(quests);
        }
    }

    private void resetDueDateForIncompleteQuests() {
        List<Quest> quests = questPersistenceService.findAllUncompleted();
        for (Quest q : quests) {
            if (q.getDue() != null && DateUtils.isBeforeToday(q.getDue())) {
                q.setStatus(Status.UNPLANNED.name());
                q.setDue(null);
                questPersistenceService.save(q);
            }
        }
    }

    private void registerServices() {
        eventBus.register(analyticsService);
        eventBus.register(assistantService);
        eventBus.register(playerService);
        eventBus.register(this);
    }

    @Subscribe
    public void onPlayerLevelUp(PlayerLevelUpEvent e) {
        Intent i = new Intent(this, LevelUpActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(LevelUpActivity.LEVEL_EXTRA_KEY, e.newLevel);
        startActivity(i);
    }

    private void initPlanDayReminder() {
        Time time = Time.of(Constants.DEFAULT_PLAN_DAY_TIME);
        new RemindPlanDayJob(this, time).schedule();
    }

    private void initReviewDayReminder() {
        Time time = Time.of(Constants.DEFAULT_REVIEW_DAY_TIME);
        new RemindReviewDayJob(this, time).schedule();
    }

    public static AppComponent getAppComponent(Context context) {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(context))
                    .build();
        }

        return appComponent;
    }
}
