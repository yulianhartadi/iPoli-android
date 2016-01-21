package io.ipoli.android.app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.jobs.RemindPlanDayJob;
import io.ipoli.android.app.jobs.RemindReviewDayJob;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.ReminderIntentService;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.assistant.AssistantService;
import io.ipoli.android.quest.Quest;
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

    @Override
    public void onCreate() {
        super.onCreate();
        getAppComponent(this).inject(this);
        registerServices();
        initPlanDayReminder();
        initReviewDayReminder();
        Quest q = new Quest("Tadaaa", Quest.Status.PLANNED.name(), new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, 15);
        q.setStartTime(calendar.getTime());
        questPersistenceService.save(q);
        scheduleNextQuestReminder();
    }

    private void registerServices() {
        eventBus.register(analyticsService);
        eventBus.register(assistantService);
        eventBus.register(this);
    }

    private void initPlanDayReminder() {
        Time time = Time.of(Constants.DEFAULT_PLAN_DAY_TIME);
        new RemindPlanDayJob(this, time).schedule();
    }

    private void initReviewDayReminder() {
        Time time = Time.of(Constants.DEFAULT_REVIEW_DAY_TIME);
        new RemindReviewDayJob(this, time).schedule();
    }

    @Subscribe
    public void onScheduleNextQuestReminder(ScheduleNextQuestReminderEvent e) {
        scheduleNextQuestReminder();
    }

    private void scheduleNextQuestReminder() {
        Quest q = questPersistenceService.findQuestStartingAfter(new Date());
        if (q == null) {
            return;
        }
        Intent intent = new Intent(this, ReminderIntentService.class);
        intent.setAction(ReminderIntentService.ACTION_REMIND_START_QUEST);
        intent.putExtra("id", q.getId());
        PendingIntent pendingIntent = PendingIntent.getService(this, Constants.REMIND_QUEST_START_REQUEST_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setExact(AlarmManager.RTC_WAKEUP, q.getStartTime().getTime(), pendingIntent);
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
