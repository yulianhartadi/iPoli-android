package io.ipoli.android.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.jobs.RemindPlanDayJob;
import io.ipoli.android.app.jobs.RemindReviewDayJob;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.assistant.AssistantService;
import io.ipoli.android.player.LevelUpActivity;
import io.ipoli.android.player.PlayerService;
import io.ipoli.android.player.events.PlayerLevelUpEvent;
import io.ipoli.android.quest.events.ScheduleNextQuestReminderEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

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
        registerServices();
        initPlanDayReminder();
        initReviewDayReminder();
        eventBus.post(new ScheduleNextQuestReminderEvent());

//        if (questPersistenceService.findAllPlannedForToday().isEmpty()) {
//            Quest q = new Quest("Read a book", Status.PLANNED.name(), new Date());
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(new Date());
//            calendar.add(Calendar.HOUR, 5);
//            q.setStartTime(calendar.getTime());
//            q.setDuration(90);
//            questPersistenceService.save(q);
//
//            q = new Quest("Workout", Status.PLANNED.name(), new Date());
//            calendar = Calendar.getInstance();
//            calendar.setTime(new Date());
//            calendar.add(Calendar.HOUR, 5);
//            q.setStartTime(calendar.getTime());
//            q.setDuration(90);
//            questPersistenceService.save(q);
//
//            q = new Quest("Call grandma & grandpa", Status.PLANNED.name(), new Date());
//            calendar = Calendar.getInstance();
//            calendar.setTime(new Date());
//            calendar.add(Calendar.HOUR, 5);
//            q.setStartTime(calendar.getTime());
//            q.setDuration(90);
//            questPersistenceService.save(q);
//
//            q = new Quest("Work on iPoli", Status.PLANNED.name(), new Date());
//            calendar = Calendar.getInstance();
//            calendar.setTime(new Date());
//            calendar.add(Calendar.HOUR, 5);
//            q.setStartTime(calendar.getTime());
//            q.setDuration(90);
//            questPersistenceService.save(q);
//
//            q = new Quest("Go for a walk", Status.PLANNED.name(), new Date());
//            calendar = Calendar.getInstance();
//            calendar.setTime(new Date());
//            calendar.add(Calendar.HOUR, 5);
//            q.setStartTime(calendar.getTime());
//            q.setDuration(90);
//            questPersistenceService.save(q);
//
//            q = new Quest("Watch ML lectures", Status.PLANNED.name(), new Date());
//            calendar = Calendar.getInstance();
//            calendar.setTime(new Date());
//            calendar.add(Calendar.HOUR, 5);
//            q.setStartTime(calendar.getTime());
//            q.setDuration(90);
//            questPersistenceService.save(q);
//        }
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
