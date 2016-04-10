package io.ipoli.android.app;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.events.ForceSyncRequestEvent;
import io.ipoli.android.app.events.SyncRequestEvent;
import io.ipoli.android.app.jobs.RemindPlanDayJob;
import io.ipoli.android.app.jobs.RemindReviewDayJob;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.RestAPIModule;
import io.ipoli.android.app.net.APIService;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.AppJobService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewRecurrentQuestEvent;
import io.ipoli.android.quest.events.RecurrentQuestSavedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.persistence.events.QuestsSavedEvent;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class App extends Application {

    public static final int SYNC_JOB_ID = 1;
    private static AppComponent appComponent;

    @Inject
    Bus eventBus;

    @Inject
    AnalyticsService analyticsService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .schemaVersion(0)
                .build();
        Realm.setDefaultConfiguration(config);

        getAppComponent(this).inject(this);
        resetDueDateForIncompleteQuests();
        registerServices();
        initPlanDayReminder();
        initReviewDayReminder();
        sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int runCount = prefs.getInt(Constants.KEY_APP_RUN_COUNT, 0);
        if (runCount == 0) {
            saveInitialQuests();
        }
        SharedPreferences.Editor e = prefs.edit();
        e.putInt(Constants.KEY_APP_RUN_COUNT, runCount + 1);
        e.apply();

        eventBus.post(new ForceSyncRequestEvent());
    }

    private void saveInitialQuests() {
        List<Quest> quests = new ArrayList<>();

        addTomorrowQuests(quests);
        addTodayUnscheduledQuests(quests);
        addTodayScheduledQuests(quests);
        addInboxQuests(quests);

        questPersistenceService.saveAll(quests);
    }

    private void addTodayUnscheduledQuests(List<Quest> initialQuests) {
        Quest trashQuest = new Quest("Throw away the trash", new Date());
        Quest.setContext(trashQuest, QuestContext.CHORES);
        initialQuests.add(trashQuest);
    }

    private void addTomorrowQuests(List<Quest> initialQuests) {
        Date tomorrow = DateUtils.getTomorrow();

        Quest gymQuest = new Quest("Go to the gym", tomorrow);
        Quest.setContext(gymQuest, QuestContext.WELLNESS);
        Quest.setStartTime(gymQuest, Time.now());
        gymQuest.setDuration(60);
        initialQuests.add(gymQuest);

        Quest brushQuest = new Quest("Brush your teeth", tomorrow);
        Quest.setContext(brushQuest, QuestContext.WELLNESS);
        Quest.setStartTime(brushQuest, Time.atHours(10));

        initialQuests.add(brushQuest);
    }

    private void addInboxQuests(List<Quest> initialQuests) {
        Quest defrostQuest = new Quest("Defrost the freezer");
        Quest.setContext(defrostQuest, QuestContext.CHORES);
        initialQuests.add(defrostQuest);

        Quest dentistQuest = new Quest("Go to the dentist");
        Quest.setContext(dentistQuest, QuestContext.PERSONAL);
        initialQuests.add(dentistQuest);
    }

    private void addTodayScheduledQuests(List<Quest> initialQuests) {
        Quest welcomeQuest = new Quest("Get to know iPoli", DateUtils.getNow());
        Quest.setContext(welcomeQuest, QuestContext.FUN);
        Quest.setStartTime(welcomeQuest, Time.minutesAgo(15));
        initialQuests.add(welcomeQuest);

        Quest readQuest = new Quest("Read a book", DateUtils.getNow());
        Quest.setContext(readQuest, QuestContext.LEARNING);
        readQuest.setDuration(60);
        Quest.setStartTime(readQuest, Time.afterHours(2));
        initialQuests.add(readQuest);

        Quest callQuest = new Quest("Call mom & dad", new Date());
        Quest.setContext(callQuest, QuestContext.PERSONAL);
        Quest.setStartTime(callQuest, Time.at(19, 30));
        callQuest.setDuration(15);
        initialQuests.add(callQuest);
    }

    private void resetDueDateForIncompleteQuests() {
        questPersistenceService.findAllUncompleted().flatMapIterable(q -> q)
                .filter(q -> q.getEndDate() != null && DateUtils.isBeforeToday(q.getEndDate()))
                .flatMap(q -> {
                    q.setEndDate(null);
                    return questPersistenceService.save(q);
                });
    }

    private void registerServices() {
        eventBus.register(analyticsService);
        eventBus.register(this);
    }

    private void initPlanDayReminder() {
        Time time = Time.at(Constants.DEFAULT_PLAN_DAY_TIME);
        new RemindPlanDayJob(this, time).schedule();
    }

    private void initReviewDayReminder() {
        Time time = Time.at(Constants.DEFAULT_REVIEW_DAY_TIME);
        new RemindReviewDayJob(this, time).schedule();
    }

    public static AppComponent getAppComponent(Context context) {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(context))
                    .restAPIModule(new RestAPIModule(APIService.API_ENDPOINT))
                    .build();
        }
        return appComponent;
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        questPersistenceService.save(e.quest);
    }

    @Subscribe
    public void onNewRecurrentQuest(NewRecurrentQuestEvent e) {
        recurrentQuestPersistenceService.save(e.recurrentQuest);
    }

    @Subscribe
    public void onRecurrentQuestSaved(RecurrentQuestSavedEvent e) {
        eventBus.post(new ForceSyncRequestEvent());
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        scheduleNextReminder();
    }

    @Subscribe
    public void onQuestsSaved(QuestsSavedEvent e) {
        scheduleNextReminder();
    }

    @Subscribe
    public void onQuestDeleted(QuestDeletedEvent e) {
        scheduleNextReminder();
    }

    @Subscribe
    public void onSyncRequest(SyncRequestEvent e) {
        scheduleJob(defaultSyncJob()
                .build());
    }

    private void scheduleJob(JobInfo job) {
        JobScheduler jobScheduler = (JobScheduler)
                getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(SYNC_JOB_ID);
        jobScheduler.schedule(job);
    }

    @Subscribe
    public void onForceSyncRequest(ForceSyncRequestEvent e) {
        scheduleJob(defaultSyncJob().setOverrideDeadline(1).build());
    }

    private JobInfo.Builder defaultSyncJob() {
        JobInfo.Builder builder = new JobInfo.Builder(SYNC_JOB_ID,
                new ComponentName(getPackageName(),
                        AppJobService.class.getName()));
        return builder.setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
    }

    private void scheduleNextReminder() {
        sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }
}