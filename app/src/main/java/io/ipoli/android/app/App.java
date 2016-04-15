package io.ipoli.android.app;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewRecurrentQuestEvent;
import io.ipoli.android.quest.events.RecurrentQuestSavedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.persistence.events.QuestsSavedEvent;
import io.ipoli.android.quest.persistence.events.RecurrentQuestDeletedEvent;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class App extends MultiDexApplication {

    private static final int SYNC_JOB_ID = 1;
    private static final int DAILY_SYNC_JOB_ID = 2;

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

        JodaTimeAndroid.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .schemaVersion(0)
                .build();
        Realm.setDefaultConfiguration(config);

        getAppComponent(this).inject(this);
        resetEndDateForIncompleteQuests();
        registerServices();
        initPlanDayReminder();
        initReviewDayReminder();
        sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));

        LocalStorage localStorage = LocalStorage.of(getApplicationContext());

        int runCount = localStorage.readInt(Constants.KEY_APP_RUN_COUNT, 0);
        if (runCount == 0) {
            localStorage.saveStringSet(Constants.KEY_REMOVED_QUESTS, new HashSet<>());
            localStorage.saveStringSet(Constants.KEY_REMOVED_RECURRENT_QUESTS, new HashSet<>());
            saveInitialQuests();
            eventBus.post(new ForceSyncRequestEvent());
        } else if (runCount == 1) {
            scheduleJob(dailySyncJob());
        } else {
            eventBus.post(new SyncRequestEvent());
        }
        localStorage.increment(Constants.KEY_APP_RUN_COUNT);
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
        Quest welcomeQuest = new Quest("Get to know iPoli", DateUtils.now());
        Quest.setContext(welcomeQuest, QuestContext.FUN);
        Quest.setStartTime(welcomeQuest, Time.minutesAgo(15));
        initialQuests.add(welcomeQuest);

        Quest readQuest = new Quest("Read a book", DateUtils.now());
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

    private void resetEndDateForIncompleteQuests() {
        questPersistenceService.findAllIncompleteBefore(new LocalDate()).flatMapIterable(q -> q)
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
        eventBus.post(new SyncRequestEvent());
        scheduleNextReminder();
    }

    @Subscribe
    public void onQuestsSaved(QuestsSavedEvent e) {
        eventBus.post(new SyncRequestEvent());
        scheduleNextReminder();
    }

    @Subscribe
    public void onQuestDeleted(QuestDeletedEvent e) {
        QuestNotificationScheduler.stopAll(e.id, this);
        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        Set<String> removedQuests = localStorage.readStringSet(Constants.KEY_REMOVED_QUESTS);
        removedQuests.add(e.id);
        localStorage.saveStringSet(Constants.KEY_REMOVED_QUESTS, removedQuests);
        eventBus.post(new SyncRequestEvent());
        scheduleNextReminder();
    }

    @Subscribe
    public void onRecurrentQuestDeleted(RecurrentQuestDeletedEvent e) {
        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        Set<String> removedQuests = localStorage.readStringSet(Constants.KEY_REMOVED_RECURRENT_QUESTS);
        removedQuests.add(e.id);
        localStorage.saveStringSet(Constants.KEY_REMOVED_RECURRENT_QUESTS, removedQuests);
        eventBus.post(new SyncRequestEvent());
        scheduleNextReminder();
    }

    @Subscribe
    public void onSyncRequest(SyncRequestEvent e) {
        scheduleJob(defaultSyncJob()
                .build());
    }

    private void scheduleJob(JobInfo job) {
        getJobScheduler().schedule(job);
    }

    private JobScheduler getJobScheduler() {
        return (JobScheduler)
                getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Subscribe
    public void onForceSyncRequest(ForceSyncRequestEvent e) {
        scheduleJob(defaultSyncJob().setOverrideDeadline(1).build());
    }

    private JobInfo.Builder defaultSyncJob() {
        return createJobBuilder(SYNC_JOB_ID).setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
    }

    private JobInfo dailySyncJob() {
        return createJobBuilder(DAILY_SYNC_JOB_ID).setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(TimeUnit.HOURS.toMillis(24)).build();
    }

    @NonNull
    private JobInfo.Builder createJobBuilder(int dailySyncJobId) {
        return new JobInfo.Builder(dailySyncJobId,
                new ComponentName(getPackageName(),
                        AppJobService.class.getName()));
    }

    private void scheduleNextReminder() {
        sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }
}