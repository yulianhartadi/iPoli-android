package io.ipoli.android.app;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.facebook.FacebookSdk;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDate;

import java.math.BigInteger;
import java.util.ArrayList;
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
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ForceServerSyncRequestEvent;
import io.ipoli.android.app.events.ScheduleRepeatingQuestsEvent;
import io.ipoli.android.app.events.ServerSyncRequestEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.events.VersionUpdatedEvent;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.modules.RestAPIModule;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.AppJobService;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListReader;
import io.ipoli.android.app.services.readers.AndroidCalendarRepeatingQuestListReader;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.activities.ChallengeCompleteActivity;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.events.ChallengeCompletedEvent;
import io.ipoli.android.challenge.events.DailyChallengeCompleteEvent;
import io.ipoli.android.challenge.events.NewChallengeEvent;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.persistence.RealmChallengePersistenceService;
import io.ipoli.android.challenge.receivers.ScheduleDailyChallengeReminderReceiver;
import io.ipoli.android.challenge.ui.events.CompleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.DeleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.UpdateChallengeEvent;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.activities.LevelUpActivity;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.LevelUpEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.player.persistence.RealmPlayerPersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteRepeatingQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.RepeatingQuestSavedEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.events.UpdateQuestEvent;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.generators.RewardProvider;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmRepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.events.QuestDeletedEvent;
import io.ipoli.android.quest.persistence.events.QuestSavedEvent;
import io.ipoli.android.quest.persistence.events.RepeatingQuestDeletedEvent;
import io.ipoli.android.quest.receivers.ScheduleNextRemindersReceiver;
import io.ipoli.android.quest.reminders.persistence.RealmReminderPersistenceService;
import io.ipoli.android.quest.schedulers.PersistentRepeatingQuestScheduler;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.quest.ui.events.UpdateRepeatingQuestEvent;
import io.ipoli.android.quest.widgets.AgendaWidgetProvider;
import io.ipoli.android.settings.events.DailyChallengeStartTimeChangedEvent;
import io.ipoli.android.tutorial.events.TutorialDoneEvent;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
    RepeatingQuestScheduler repeatingQuestScheduler;

    @Inject
    AnalyticsService analyticsService;

    private QuestPersistenceService questPersistenceService;

    private RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    private ChallengePersistenceService challengePersistenceService;

    private PlayerPersistenceService playerPersistenceService;

    private RealmReminderPersistenceService reminderPersistenceService;

    BroadcastReceiver dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleQuestsFor2WeeksAhead().compose(applyAndroidSchedulers()).subscribe();
            eventBus.post(new CurrentDayChangedEvent(new LocalDate(), CurrentDayChangedEvent.Source.CALENDAR));
            moveIncompleteQuestsToInbox();
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
                .initialData(realm -> {
                    Player player = new Player(String.valueOf(Constants.DEFAULT_PLAYER_XP), Constants.DEFAULT_PLAYER_LEVEL, Constants.DEFAULT_PLAYER_AVATAR);
                    player.setCoins(Constants.DEFAULT_PLAYER_COINS);
                    realm.copyToRealm(player);
                })
//                .migration((realm, oldVersion, newVersion) -> {
//
//                })
                .build();
        Realm.setDefaultConfiguration(config);

        getAppComponent(this).inject(this);

        Realm realm = Realm.getDefaultInstance();
        questPersistenceService = new RealmQuestPersistenceService(eventBus, realm);
        repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, realm);
        challengePersistenceService = new RealmChallengePersistenceService(eventBus, realm);
        playerPersistenceService = new RealmPlayerPersistenceService(realm);

        moveIncompleteQuestsToInbox();
        registerServices();
        scheduleNextReminder();

        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        int versionCode = localStorage.readInt(Constants.KEY_APP_VERSION_CODE);
        if (versionCode != BuildConfig.VERSION_CODE) {
            scheduleDailyChallenge();
            scheduleJob(dailySyncJob());
            localStorage.saveInt(Constants.KEY_APP_VERSION_CODE, BuildConfig.VERSION_CODE);
            FlurryAgent.onStartSession(this);
            eventBus.post(new VersionUpdatedEvent(versionCode, BuildConfig.VERSION_CODE));
            FlurryAgent.onEndSession(this);
        }
        scheduleQuestsFor2WeeksAhead().compose(applyAndroidSchedulers()).subscribe(aVoid -> {
        }, Throwable::printStackTrace, () -> {
            if (localStorage.readInt(Constants.KEY_APP_RUN_COUNT) != 0) {
                eventBus.post(new ForceServerSyncRequestEvent());
            }
            localStorage.increment(Constants.KEY_APP_RUN_COUNT);
        });

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

    private void scheduleDailyChallenge() {
        sendBroadcast(new Intent(ScheduleDailyChallengeReminderReceiver.ACTION_SCHEDULE_DAILY_CHALLENGE_REMINDER));
    }

    @Subscribe
    public void onDailyChallengeStartTimeChanged(DailyChallengeStartTimeChangedEvent e) {
        scheduleDailyChallenge();
    }

    private Observable<Void> scheduleQuestsFor2WeeksAhead() {
        return Observable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            RepeatingQuestPersistenceService repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, realm);
            List<RepeatingQuest> repeatingQuests = repeatingQuestPersistenceService.findAllNonAllDayActiveRepeatingQuests();
            QuestPersistenceService questPersistenceService = new RealmQuestPersistenceService(eventBus, realm);
            scheduleRepeatingQuest(repeatingQuests, questPersistenceService);
            realm.close();
            return Observable.empty();
        });
    }

    private void moveIncompleteQuestsToInbox() {
        List<Quest> quests = questPersistenceService.findAllIncompleteToDosBefore(new LocalDate());
        for (Quest q : quests) {
            if (q.isStarted()) {
                q.setEndDateFromLocal(new Date());
                q.setStartMinute(0);
            } else {
                q.setEndDate(null);
            }
            if (q.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
                q.setPriority(null);
            }
            questPersistenceService.save(q).subscribe();
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
    public void onScheduleRepeatingQuests(ScheduleRepeatingQuestsEvent e) {
        scheduleQuestsFor2WeeksAhead().compose(applyAndroidSchedulers()).subscribe(quests -> {
        }, Throwable::printStackTrace, () ->
                eventBus.post(new SyncCompleteEvent()));
    }

    @Subscribe
    public void onCompleteQuestRequest(CompleteQuestRequestEvent e) {
        Quest q = e.quest;
        QuestNotificationScheduler.stopAll(q.getId(), this);
        q.setCompletedAt(new Date());
        q.setCompletedAtMinute(Time.now().toMinutesAfterMidnight());
        questPersistenceService.save(q).subscribe(quest -> {
            onQuestComplete(quest, e.source);
            checkForDailyChallengeCompletion(quest, e.source);
        });
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

        if (quest.isScheduledForThePast()) {
            quest.setEndDate(null);
            quest.setStartDate(null);
        }
        questPersistenceService.save(quest).subscribe(q -> {
            Player player = playerPersistenceService.find();
            player.removeExperience(q.getExperience());
            if (shouldDecreaseLevel(player)) {
                player.setLevel(Math.max(Constants.DEFAULT_PLAYER_LEVEL, player.getLevel() - 1));
                while (shouldDecreaseLevel(player)) {
                    player.setLevel(Math.max(Constants.DEFAULT_PLAYER_LEVEL, player.getLevel() - 1));
                }
                eventBus.post(new LevelDownEvent(player.getLevel()));
            }
            player.removeCoins(q.getCoins());
            playerPersistenceService.saveSync(player);
            eventBus.post(new UndoCompletedQuestEvent(q));
        });
    }

    private boolean shouldIncreaseLevel(Player player) {
        return new BigInteger(player.getExperience()).compareTo(ExperienceForLevelGenerator.forLevel(player.getLevel() + 1)) >= 0;
    }

    private boolean shouldDecreaseLevel(Player player) {
        return new BigInteger(player.getExperience()).compareTo(ExperienceForLevelGenerator.forLevel(player.getLevel())) < 0;
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        questPersistenceService.saveReminders(e.quest, e.reminders);
        questPersistenceService.save(e.quest).subscribe(quest -> {
            if (Quest.isCompleted(quest)) {
                onQuestComplete(quest, e.source);
            }
        });
    }

    @Subscribe
    public void onUpdateQuest(UpdateQuestEvent e) {
        questPersistenceService.saveReminders(e.quest, e.reminders);
        questPersistenceService.save(e.quest).subscribe(quest -> {
            if (Quest.isCompleted(quest)) {
                onQuestComplete(quest, e.source);
            }
        });
    }

    @Subscribe
    public void onUpdateRepeatingQuest(UpdateRepeatingQuestEvent e) {
        List<Quest> questsToRemove = questPersistenceService.findAllUpcomingForRepeatingQuest(new LocalDate(), e.repeatingQuest);
        LocalStorage localStorage = LocalStorage.of(getApplicationContext());
        Set<String> removedQuests = localStorage.readStringSet(Constants.KEY_REMOVED_QUESTS);
        for (Quest quest : questsToRemove) {
            QuestNotificationScheduler.stopAll(quest.getId(), this);
            if (!TextUtils.isEmpty(quest.getRemoteId())) {
                removedQuests.add(quest.getRemoteId());
            }
        }
        localStorage.saveStringSet(Constants.KEY_REMOVED_QUESTS, removedQuests);
        questPersistenceService.delete(questsToRemove).subscribe(ignored -> {
        }, Throwable::printStackTrace, () -> {
            repeatingQuestPersistenceService.saveReminders(e.repeatingQuest, e.reminders);
            repeatingQuestPersistenceService.save(e.repeatingQuest).subscribe();
        });
    }

    @Subscribe
    public void onDeleteQuestRequest(DeleteQuestRequestEvent e) {
        e.quest.markDeleted();
        RealmList<Reminder> reminders = e.quest.getReminders();
        if (reminders != null && !reminders.isEmpty()) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            for (Reminder reminder : reminders) {
                notificationManagerCompat.cancel(reminder.getNotificationId());
            }
        }
        questPersistenceService.save(e.quest).subscribe();
    }

    private void onQuestComplete(Quest quest, EventSource source) {
        updatePlayer(quest);
        eventBus.post(new QuestCompletedEvent(quest, source));
    }

    private void checkForDailyChallengeCompletion(Quest quest, EventSource source) {
        if (quest.getPriority() != Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
            return;
        }
        LocalStorage localStorage = LocalStorage.of(this);
        Date todayUtc = DateUtils.toStartOfDayUTC(LocalDate.now());
        Date lastCompleted = new Date(localStorage.readLong(Constants.KEY_DAILY_CHALLENGE_LAST_COMPLETED));
        boolean isCompletedForToday = todayUtc.equals(lastCompleted);
        if (isCompletedForToday) {
            return;
        }
        Set<Integer> challengeDays = localStorage.readIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, Constants.DEFAULT_DAILY_CHALLENGE_DAYS);
        int currentDayOfWeek = LocalDate.now().getDayOfWeek();
        if (!challengeDays.contains(currentDayOfWeek)) {
            return;
        }
        long questCount = questPersistenceService.countAllCompletedWithPriorityForDate(Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY, LocalDate.now());
        if (questCount != Constants.DAILY_CHALLENGE_QUEST_COUNT) {
            return;
        }
        localStorage.saveLong(Constants.KEY_DAILY_CHALLENGE_LAST_COMPLETED, todayUtc.getTime());

        long xp = new ExperienceRewardGenerator().generateForDailyChallenge();
        long coins = new CoinsRewardGenerator().generateForDailyChallenge();
        Challenge dailyChallenge = new Challenge();
        dailyChallenge.setExperience(xp);
        dailyChallenge.setCoins(coins);
        updatePlayer(dailyChallenge);
        showChallengeCompleteDialog(getString(R.string.daily_challenge_complete_dialog_title), xp, coins);
        eventBus.post(new DailyChallengeCompleteEvent());
    }

    private void showChallengeCompleteDialog(String title, long xp, long coins) {
        Intent intent = new Intent(this, ChallengeCompleteActivity.class);
        intent.putExtra(ChallengeCompleteActivity.TITLE, title);
        intent.putExtra(ChallengeCompleteActivity.EXPERIENCE, xp);
        intent.putExtra(ChallengeCompleteActivity.COINS, coins);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updatePlayer(RewardProvider rewardProvider) {
        Player player = playerPersistenceService.find();
        player.addExperience(rewardProvider.getExperience());
        increasePlayerLevelIfNeeded(player);
        player.addCoins(rewardProvider.getCoins());
        playerPersistenceService.saveSync(player);
    }

    private void increasePlayerLevelIfNeeded(Player player) {
        if (shouldIncreaseLevel(player)) {
            player.setLevel(player.getLevel() + 1);
            while (shouldIncreaseLevel(player)) {
                player.setLevel(player.getLevel() + 1);
            }
            Intent intent = new Intent(this, LevelUpActivity.class);
            intent.putExtra(LevelUpActivity.LEVEL, player.getLevel());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            eventBus.post(new LevelUpEvent(player.getLevel()));
        }
    }

    @Subscribe
    public void onNewRepeatingQuest(NewRepeatingQuestEvent e) {
        repeatingQuestPersistenceService.saveReminders(e.repeatingQuest, e.reminders);
        repeatingQuestPersistenceService.save(e.repeatingQuest).subscribe();
    }

    @Subscribe
    public void onRepeatingQuestSaved(RepeatingQuestSavedEvent e) {
        RepeatingQuest rq = e.repeatingQuest;

        Recurrence recurrence = rq.getRecurrence();
        if (TextUtils.isEmpty(recurrence.getRrule())) {
            List<Quest> questsToCreate = new ArrayList<>();
            for (int i = 0; i < recurrence.getTimesADay(); i++) {
                questsToCreate.add(repeatingQuestScheduler.createQuestFromRepeating(rq, recurrence.getDtstart()));
            }
            questPersistenceService.saveRemoteObjects(questsToCreate).subscribe(quests -> {
            }, Throwable::printStackTrace, () -> {
                onQuestChanged();
                eventBus.post(new ServerSyncRequestEvent());
            });
        } else {

            Observable.defer(() -> {
                Realm realm = Realm.getDefaultInstance();
                QuestPersistenceService questPersistenceService = new RealmQuestPersistenceService(eventBus, realm);
                scheduleRepeatingQuest(rq, questPersistenceService);
                realm.close();
                return Observable.empty();
            }).compose(applyAndroidSchedulers()).subscribe(quests -> {
            }, Throwable::printStackTrace, () -> {
                onQuestChanged();
                eventBus.post(new ServerSyncRequestEvent());
            });
        }
    }

    @Subscribe
    public void onDeleteRepeatingQuestRequest(final DeleteRepeatingQuestRequestEvent e) {
        final RepeatingQuest repeatingQuest = e.repeatingQuest;
        repeatingQuest.markDeleted();
        markQuestsDeleted(repeatingQuest).flatMap(ignored ->
                repeatingQuestPersistenceService.saveRemoteObject(repeatingQuest)).subscribe();
    }

    private Observable<List<Quest>> markQuestsDeleted(RepeatingQuest repeatingQuest) {
        List<Quest> quests = questPersistenceService.findAllForRepeatingQuest(repeatingQuest);
        for (Quest q : quests) {
            if (!Quest.isCompleted(q)) {
                q.markDeleted();
            }
        }
        return questPersistenceService.saveRemoteObjects(quests);
    }

    private void scheduleRepeatingQuest(RepeatingQuest repeatingQuest, QuestPersistenceService questPersistenceService) {
        List<RepeatingQuest> repeatingQuests = new ArrayList<>();
        repeatingQuests.add(repeatingQuest);
        scheduleRepeatingQuest(repeatingQuests, questPersistenceService);
    }

    private void scheduleRepeatingQuest(List<RepeatingQuest> repeatingQuests, QuestPersistenceService questPersistenceService) {
        new PersistentRepeatingQuestScheduler(repeatingQuestScheduler, questPersistenceService).schedule(repeatingQuests, DateUtils.toStartOfDayUTC(LocalDate.now()));
    }

    @Subscribe
    public void onQuestSaved(QuestSavedEvent e) {
        eventBus.post(new ServerSyncRequestEvent());
        onQuestChanged();
    }

    private void scheduleNextReminder() {
        sendBroadcast(new Intent(ScheduleNextRemindersReceiver.ACTION_SCHEDULE_REMINDERS));
    }

    @Subscribe
    public void onDeleteChallengeRequest(DeleteChallengeRequestEvent e) {
        e.challenge.markDeleted();
        challengePersistenceService.save(e.challenge).subscribe();
        List<Quest> quests = questPersistenceService.findAllForChallenge(e.challenge);
        List<RepeatingQuest> repeatingQuests = repeatingQuestPersistenceService.findAllForChallenge(e.challenge);

        for (Quest quest : quests) {
            quest.setChallenge(null);
        }

        for (RepeatingQuest repeatingQuest : repeatingQuests) {
            repeatingQuest.setChallenge(null);
        }

        questPersistenceService.save(quests).subscribe();
        repeatingQuestPersistenceService.save(repeatingQuests).subscribe();
    }

    @Subscribe
    public void onCompleteChallengeRequest(CompleteChallengeRequestEvent e) {
        Challenge challenge = e.challenge;
        challenge.setCompletedAt(new Date());
        challengePersistenceService.save(challenge).subscribe(quest -> {
            onChallengeComplete(challenge, e.source);
        });
    }

    private void onChallengeComplete(Challenge challenge, EventSource source) {
        updatePlayer(challenge);
        showChallengeCompleteDialog(getString(R.string.challenge_complete, challenge.getName()), challenge.getExperience(), challenge.getCoins());
        eventBus.post(new ChallengeCompletedEvent(challenge, source));
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
        eventBus.post(new ServerSyncRequestEvent());
        onQuestChanged();
    }

    @Subscribe
    public void onNewChallenge(NewChallengeEvent e) {
        challengePersistenceService.save(e.challenge).subscribe();
    }

    @Subscribe
    public void onUpdateChallenge(UpdateChallengeEvent e) {
        challengePersistenceService.save(e.challenge).subscribe();
    }

    @Subscribe
    public void onQuestDeleted(QuestDeletedEvent e) {
        QuestNotificationScheduler.stopAll(e.id, this);
        eventBus.post(new ServerSyncRequestEvent());
        onQuestChanged();
    }

    @Subscribe
    public void onSyncRequest(ServerSyncRequestEvent e) {
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
    public void onForceSyncRequest(ForceServerSyncRequestEvent e) {
        scheduleJob(createJobBuilder(SYNC_JOB_ID).setPersisted(true)
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

    @Subscribe
    public void onTutorialDone(TutorialDoneEvent e) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            eventBus.post(new ForceServerSyncRequestEvent());
            return;
        }

        syncCalendars().subscribe(o -> {
        }, Throwable::printStackTrace, () -> {
            eventBus.post(new SyncCompleteEvent());
            eventBus.post(new ForceServerSyncRequestEvent());
        });

    }

    @Subscribe
    public void onSyncWithCalendarRequest(SyncCalendarRequestEvent e) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        syncCalendars().subscribe(o -> {
        }, Throwable::printStackTrace, () ->
                eventBus.post(new SyncCompleteEvent()));
    }

    private Observable<Object> syncCalendars() {
        return Observable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            QuestPersistenceService questPersistenceService = new RealmQuestPersistenceService(eventBus, realm);
            RepeatingQuestPersistenceService repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, realm);
            AndroidCalendarQuestListReader questReader = new AndroidCalendarQuestListReader(questPersistenceService, repeatingQuestPersistenceService);
            AndroidCalendarRepeatingQuestListReader repeatingQuestReader = new AndroidCalendarRepeatingQuestListReader(repeatingQuestPersistenceService);
            CalendarProvider provider = new CalendarProvider(this);
            List<Calendar> calendars = provider.getCalendars().getList();
            LocalStorage localStorage = LocalStorage.of(this);
            Set<String> calendarIds = new HashSet<>();
            List<Event> repeating = new ArrayList<>();
            List<Event> nonRepeating = new ArrayList<>();
            for (Calendar c : calendars) {
                if (!c.visible) {
                    continue;
                }
                calendarIds.add(String.valueOf(c.id));
                List<Event> events = provider.getEvents(c.id).getList();
                for (Event event : events) {
                    if (isRepeatingAndroidCalendarEvent(event)) {
                        repeating.add(event);
                    } else {
                        nonRepeating.add(event);
                    }
                }
            }
            localStorage.saveStringSet(Constants.KEY_SELECTED_ANDROID_CALENDARS, calendarIds);
            List<Quest> quests = questReader.read(nonRepeating);
            questPersistenceService.saveSync(quests);
            List<RepeatingQuest> repeatingQuests = repeatingQuestReader.read(repeating);
            repeatingQuestPersistenceService.saveSync(repeatingQuests);
            scheduleRepeatingQuest(repeatingQuests, questPersistenceService);
            realm.close();
            return Observable.empty();
        }).compose(applyAndroidSchedulers());
    }

    private <T> Observable.Transformer<T, T> applyAndroidSchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private boolean isRepeatingAndroidCalendarEvent(Event e) {
        return !TextUtils.isEmpty(e.rRule) || !TextUtils.isEmpty(e.rDate);
    }

    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        onQuestChanged();
    }
}