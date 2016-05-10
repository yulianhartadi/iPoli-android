package io.ipoli.android.app;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.facebook.FacebookSdk;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Dur;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.ipoli.android.APIConstants;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.ForceSyncRequestEvent;
import io.ipoli.android.app.events.SyncRequestEvent;
import io.ipoli.android.app.events.VersionUpdatedEvent;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.RestAPIModule;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.AppJobService;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestAddedEvent;
import io.ipoli.android.quest.events.NewRecurrentQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
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
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import me.everything.providers.core.Data;

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

    BroadcastReceiver dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            eventBus.post(new CurrentDayChangedEvent(new LocalDate(), CurrentDayChangedEvent.Source.CALENDAR));
            resetEndDateForIncompleteQuests();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
        FacebookSdk.sdkInitialize(getApplicationContext());

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .schemaVersion(BuildConfig.VERSION_CODE)
                .migration((realm, oldVersion, newVersion) -> {

                })
                .build();
        Realm.setDefaultConfiguration(config);

        getAppComponent(this).inject(this);

        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        resetEndDateForIncompleteQuests();
        registerServices();
        sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));

        int versionCode = localStorage.readInt(Constants.KEY_APP_VERSION_CODE);
        if (versionCode != BuildConfig.VERSION_CODE) {
            scheduleJob(dailySyncJob());
            localStorage.saveInt(Constants.KEY_APP_VERSION_CODE, BuildConfig.VERSION_CODE);
            eventBus.post(new VersionUpdatedEvent(versionCode, BuildConfig.VERSION_CODE));
        }
        eventBus.post(new ForceSyncRequestEvent());

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

        syncCalendar();
    }

    private void syncCalendar() {
        CalendarProvider provider = new CalendarProvider(getApplicationContext());
        List<Calendar> calendars = provider.getCalendars().getList();

        String[] columns = new String[]{
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DURATION,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.RRULE,
                CalendarContract.Events.RDATE,
                CalendarContract.Events.EVENT_TIMEZONE,
                CalendarContract.Events.EVENT_END_TIMEZONE,
                CalendarContract.Events.ORIGINAL_ID,
                CalendarContract.Events.ORIGINAL_SYNC_ID
        };

//        for (Calendar calendar : calendars) {
        Data<Event> events = provider.getEvents(1);
        Cursor cursor = events.getCursor();
//        cursor.moveToNext();
//        Event e = events.fromCursor(cursor);
//        Data<Instance> instances = provider.getInstances(e.id, 0, System.currentTimeMillis());

//            events.fromCursor(events.getCursor(), columns);


        List<Quest> quests = new ArrayList<>();
        List<RecurrentQuest> habits = new ArrayList<>();
        List<String> exceptionQuests = new ArrayList<>();

        while (cursor.moveToNext()) {
            Event e = events.fromCursor(cursor, columns);
            if (TextUtils.isEmpty(e.rRule) && TextUtils.isEmpty(e.rDate)) {
                Quest q = new Quest(e.title);
                DateTime startDateTime = new DateTime(e.dTStart, DateTimeZone.forID(e.eventTimeZone));
                DateTime endDateTime = new DateTime(e.dTend, DateTimeZone.forID(e.eventTimeZone));
                q.setId(String.valueOf(e.id));//???
                q.setDuration(Minutes.minutesBetween(startDateTime, endDateTime).getMinutes());
                q.setStartMinute(startDateTime.getMinuteOfDay());
                q.setEndDate(startDateTime.toLocalDate().toDate());
                q.setSource("google-calendar");
                if (e.originalId != null) {
                    exceptionQuests.add(String.valueOf(e.id));
                } else {
                    quests.add(q);
                }

            } else {
                RecurrentQuest recurrentQuest = new RecurrentQuest(e.title);
                recurrentQuest.setName(e.title);
                recurrentQuest.setSource("google-calendar");
                DateTime startDateTime = new DateTime(e.dTStart, DateTimeZone.forID(e.eventTimeZone));
                recurrentQuest.setStartMinute(startDateTime.getMinuteOfDay());
                Dur dur = new Dur(e.duration);
                recurrentQuest.setDuration((int) TimeUnit.MILLISECONDS.toMinutes(dur.getTime(new Date(0)).getTime()));

                Recurrence recurrence = new Recurrence();
                recurrence.setRrule(e.rRule);
                recurrence.setRdate(e.rDate);
                //TODO handle dtstart & dtend
                habits.add(recurrentQuest);
            }

        }
    }

    private void resetEndDateForIncompleteQuests() {
        List<Quest> quests = questPersistenceService.findAllIncompleteToDosBefore(new LocalDate()).toBlocking().first();
        for (Quest q : quests) {
            if (q.isStarted()) {
                q.setEndDate(new Date());
                q.setStartMinute(0);
            } else {
                q.setEndDate(null);
            }
            questPersistenceService.save(q);
        }
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
    public void onNewQuest(NewQuestAddedEvent e) {
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
}