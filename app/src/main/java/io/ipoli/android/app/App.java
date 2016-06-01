package io.ipoli.android.app;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.ipoli.android.APIConstants;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.ForceSyncRequestEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.events.SyncRequestEvent;
import io.ipoli.android.app.events.VersionUpdatedEvent;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.RestAPIModule;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.AppJobService;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.RepeatingQuestSavedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.persistence.events.RepeatingQuestDeletedEvent;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;
import io.ipoli.android.quest.widgets.AgendaWidgetProvider;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;

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
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    BroadcastReceiver dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            eventBus.post(new CurrentDayChangedEvent(new LocalDate(), CurrentDayChangedEvent.Source.CALENDAR));
            resetEndDateForIncompleteQuests();
            updateWidgets();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
        FacebookSdk.sdkInitialize(getApplicationContext());

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .schemaVersion(BuildConfig.VERSION_CODE)
                .deleteRealmIfMigrationNeeded()
//                .migration((realm, oldVersion, newVersion) -> {
//
//                })
                .build();
        Realm.setDefaultConfiguration(config);

        getAppComponent(this).inject(this);

        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        if (localStorage.readInt(Constants.KEY_APP_RUN_COUNT) == 0) {
            Player player = new Player(String.valueOf(Constants.DEFAULT_PLAYER_XP), Constants.DEFAULT_PLAYER_LEVEL, Constants.DEFAULT_PLAYER_AVATAR);
            player.setExperienceForNextLevel(ExperienceForLevelGenerator.forLevel(player.getLevel() + 1).toString());
            player.setCoins(Constants.DEFAULT_PLAYER_COINS);
            playerPersistenceService.saveSync(player);
        }

        resetEndDateForIncompleteQuests();
        registerServices();
        scheduleNextReminder();

        int versionCode = localStorage.readInt(Constants.KEY_APP_VERSION_CODE);
        if (versionCode != BuildConfig.VERSION_CODE) {
            scheduleJob(dailySyncJob());
            localStorage.saveInt(Constants.KEY_APP_VERSION_CODE, BuildConfig.VERSION_CODE);
            eventBus.post(new VersionUpdatedEvent(versionCode, BuildConfig.VERSION_CODE));
        }
        if (localStorage.readInt(Constants.KEY_APP_RUN_COUNT) != 0) {
            eventBus.post(new ForceSyncRequestEvent());
        }
        localStorage.increment(Constants.KEY_APP_RUN_COUNT);

        getApplicationContext().registerReceiver(dateChangedReceiver, new IntentFilter(Intent.ACTION_DATE_CHANGED));

//        if (BuildConfig.DEBUG) {
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()   // or .detectAll() for all detectable problems
//                    .penaltyLog()
//                    .build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    .penaltyDeath()
//                    .build());
//        }
    }

    private void resetEndDateForIncompleteQuests() {
        questPersistenceService.findAllIncompleteToDosBefore(new LocalDate()).subscribe(quests -> {
            for (Quest q : quests) {
                if (q.isStarted()) {
                    q.setEndDate(new Date());
                    q.setStartMinute(0);
                } else {
                    q.setEndDate(null);
                }
                questPersistenceService.save(q).subscribe();
            }
        });
    }

    private void registerServices() {
        eventBus.register(analyticsService);
        eventBus.register(this);
    }

    public static AppComponent getAppComponent(Context context) {
        if (appComponent == null) {
            String ipoliApiBaseUrl = BuildConfig.DEBUG ? APIConstants.DEV_IPOLI_ENDPOINT : APIConstants.PROD_IPOLI_ENDPOINT;
            String schedulingApiBaseUrl = BuildConfig.DEBUG ? APIConstants.DEV_SCHEDULING_ENDPOINT : APIConstants.PROD_SCHEDULING_ENDPOINT;

            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(context))
                    .restAPIModule(new RestAPIModule(ipoliApiBaseUrl, schedulingApiBaseUrl))
                    .build();
        }
        return appComponent;
    }

    @Subscribe
    public void onQuestCompleteRequest(CompleteQuestRequestEvent e) {
        Quest q = e.quest;
        QuestNotificationScheduler.stopAll(q.getId(), this);
        q.setCompletedAt(new Date());
        q.setCompletedAtMinute(Time.now().toMinutesAfterMidnight());
        questPersistenceService.save(q).subscribe(quest -> {
            eventBus.post(new QuestCompletedEvent(quest, e.source));
        });
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        questPersistenceService.save(e.quest).subscribe();
    }

    @Subscribe
    public void onNewRepeatingQuest(NewRepeatingQuestEvent e) {
        repeatingQuestPersistenceService.save(e.repeatingQuest).subscribe();
    }

    @Subscribe
    public void onRepeatingQuestSaved(RepeatingQuestSavedEvent e) {
        eventBus.post(new ForceSyncRequestEvent());
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        eventBus.post(new SyncRequestEvent());
        onQuestChanged();
    }

    @Subscribe
    public void onQuestDeleted(QuestDeletedEvent e) {
        QuestNotificationScheduler.stopAll(e.id, this);
        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        Set<String> removedQuests = localStorage.readStringSet(Constants.KEY_REMOVED_QUESTS);
        removedQuests.add(e.id);
        localStorage.saveStringSet(Constants.KEY_REMOVED_QUESTS, removedQuests);
        eventBus.post(new SyncRequestEvent());
        onQuestChanged();
    }

    private void onQuestChanged() {
        scheduleNextReminder();
        updateWidgets();
    }

    private void updateWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, AgendaWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_agenda_list);
    }

    @Subscribe
    public void onRepeatingQuestDeleted(RepeatingQuestDeletedEvent e) {
        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        Set<String> removedQuests = localStorage.readStringSet(Constants.KEY_REMOVED_REPEATING_QUESTS);
        removedQuests.add(e.id);
        localStorage.saveStringSet(Constants.KEY_REMOVED_REPEATING_QUESTS, removedQuests);
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
        scheduleJob(createJobBuilder(SYNC_JOB_ID).setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_EXPONENTIAL).
                        setOverrideDeadline(1).build());
    }

    private JobInfo.Builder defaultSyncJob() {
        return createJobBuilder(SYNC_JOB_ID).setPersisted(true)
                .setMinimumLatency(TimeUnit.MINUTES.toMillis(Constants.MINIMUM_DELAY_SYNC_MINUTES))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
    }

    private JobInfo dailySyncJob() {
        return createJobBuilder(DAILY_SYNC_JOB_ID).setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(true)
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

    @Subscribe
    public void onSyncWithCalendarRequest(SyncCalendarRequestEvent e) {
        CalendarProvider provider = new CalendarProvider(this);
        List<Calendar> calendars = provider.getCalendars().getList();
        LocalStorage localStorage = LocalStorage.of(this);
        Set<String> calendarIds = new HashSet<>();
        for (Calendar c : calendars) {
            if (c.visible) {
                calendarIds.add(String.valueOf(c.id));
            }
        }
        localStorage.saveStringSet(Constants.KEY_CALENDARS_TO_SYNC, calendarIds);
        localStorage.saveStringSet(Constants.KEY_SELECTED_ANDROID_CALENDARS, calendarIds);
    }

    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        updateWidgets();
    }
}