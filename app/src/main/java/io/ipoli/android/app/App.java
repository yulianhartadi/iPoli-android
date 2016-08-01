package io.ipoli.android.app;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.facebook.FacebookSdk;
import com.flurry.android.FlurryAgent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
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

import javax.inject.Inject;

import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.events.ScheduleRepeatingQuestsEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.events.VersionUpdatedEvent;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListPersistenceService;
import io.ipoli.android.app.services.readers.AndroidCalendarRepeatingQuestListPersistenceService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.activities.ChallengeCompleteActivity;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.events.ChallengeCompletedEvent;
import io.ipoli.android.challenge.events.DailyChallengeCompleteEvent;
import io.ipoli.android.challenge.events.NewChallengeEvent;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
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
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteRepeatingQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.events.UpdateQuestEvent;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.generators.RewardProvider;
import io.ipoli.android.quest.persistence.OnChangeListener;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.receivers.ScheduleNextRemindersReceiver;
import io.ipoli.android.quest.schedulers.PersistentRepeatingQuestScheduler;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.quest.ui.events.UpdateRepeatingQuestEvent;
import io.ipoli.android.quest.widgets.AgendaWidgetProvider;
import io.ipoli.android.reminders.data.Reminder;
import io.ipoli.android.settings.events.DailyChallengeStartTimeChangedEvent;
import io.ipoli.android.tutorial.events.TutorialDoneEvent;
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

    private static AppComponent appComponent;

    private static String playerId;

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @Inject
    Gson gson;

    @Inject
    RepeatingQuestScheduler repeatingQuestScheduler;

    @Inject
    PersistentRepeatingQuestScheduler persistentRepeatingQuestScheduler;

    @Inject
    AnalyticsService analyticsService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    AndroidCalendarQuestListPersistenceService androidCalendarQuestService;

    @Inject
    AndroidCalendarRepeatingQuestListPersistenceService androidCalendarRepeatingQuestService;

    BroadcastReceiver dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleQuestsFor4WeeksAhead();
            eventBus.post(new CurrentDayChangedEvent(new LocalDate(), CurrentDayChangedEvent.Source.CALENDAR));
            moveIncompleteQuestsToInbox();
            listenForChanges();
        }
    };

    private void listenForChanges() {
        questPersistenceService.removeAllListeners();
        repeatingQuestPersistenceService.removeAllListeners();
        listenForWidgetQuestsChange();
        listenForRepeatingQuestChange();
        listenForReminderChange();
    }

    private void listenForReminderChange() {
        questPersistenceService.listenForReminderChange(new OnChangeListener<Void>() {
            @Override
            public void onNew(Void data) {
                scheduleNextReminder();
            }

            @Override
            public void onChanged(Void data) {
                scheduleNextReminder();
            }

            @Override
            public void onDeleted() {
                scheduleNextReminder();
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
        FacebookSdk.sdkInitialize(getApplicationContext());

//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        getAppComponent(this).inject(this);
        registerServices();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        } else {
            playerId = localStorage.readString(Constants.KEY_PLAYER_ID);
        }

        initAppStart();
    }

    private void initAppStart() {
        int versionCode = localStorage.readInt(Constants.KEY_APP_VERSION_CODE);
        if (versionCode != BuildConfig.VERSION_CODE) {
            scheduleDailyChallenge();
            localStorage.saveInt(Constants.KEY_APP_VERSION_CODE, BuildConfig.VERSION_CODE);
            FlurryAgent.onStartSession(this);
            eventBus.post(new VersionUpdatedEvent(versionCode, BuildConfig.VERSION_CODE));
            FlurryAgent.onEndSession(this);
        }
        localStorage.increment(Constants.KEY_APP_RUN_COUNT);
        scheduleQuestsFor4WeeksAhead();
        moveIncompleteQuestsToInbox();
        scheduleNextReminder();
        getApplicationContext().registerReceiver(dateChangedReceiver, new IntentFilter(Intent.ACTION_DATE_CHANGED));
        listenForChanges();
    }

    private void listenForRepeatingQuestChange() {
        repeatingQuestPersistenceService.listenForChange(new OnChangeListener<List<RepeatingQuest>>() {
            @Override
            public void onNew(List<RepeatingQuest> data) {
                Observable.defer(() -> {
                    for (RepeatingQuest rq : data) {
                        onRepeatingQuestSaved(rq);
                    }
                    return Observable.empty();
                }).compose(applyAndroidSchedulers()).subscribe();
            }

            @Override
            public void onChanged(List<RepeatingQuest> data) {

            }

            @Override
            public void onDeleted() {

            }
        });
    }

    private void scheduleDailyChallenge() {
        sendBroadcast(new Intent(ScheduleDailyChallengeReminderReceiver.ACTION_SCHEDULE_DAILY_CHALLENGE_REMINDER));
    }

    @Subscribe
    public void onDailyChallengeStartTimeChanged(DailyChallengeStartTimeChangedEvent e) {
        scheduleDailyChallenge();
    }

    private void scheduleQuestsFor4WeeksAhead() {
        repeatingQuestPersistenceService.findAllNonAllDayActiveRepeatingQuests(this::scheduleRepeatingQuests);
    }

    private void moveIncompleteQuestsToInbox() {
        questPersistenceService.findAllIncompleteToDosBefore(new LocalDate(), quests -> {
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
                questPersistenceService.save(q);
            }
        });
    }

    private void registerServices() {
        eventBus.register(analyticsService);
        eventBus.register(this);
    }

    public static AppComponent getAppComponent(Context context) {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(context))
                    .build();
        }
        return appComponent;
    }

    @Subscribe
    public void onPlayerCreated(PlayerCreatedEvent e) {
        playerId = e.playerId;
        initAppStart();
    }

    @Subscribe
    public void onScheduleRepeatingQuests(ScheduleRepeatingQuestsEvent e) {
        scheduleQuestsFor4WeeksAhead();
        eventBus.post(new SyncCompleteEvent());
    }

    @Subscribe
    public void onCompleteQuestRequest(CompleteQuestRequestEvent e) {
        Quest q = e.quest;
        QuestNotificationScheduler.stopAll(q.getId(), this);
        q.setCompletedAtDate(new Date());
        q.setCompletedAtMinute(Time.now().toMinutesAfterMidnight());
        questPersistenceService.save(q, () -> {
            onQuestComplete(q, e.source);
        });
    }

    @Subscribe
    public void onUndoCompletedQuestRequest(UndoCompletedQuestRequestEvent e) {
        Quest quest = e.quest;
        quest.setDifficulty(null);
        quest.setActualStartDate(null);
        quest.setCompletedAtDate(null);
        quest.setCompletedAtMinute(null);

        if (quest.isScheduledForThePast()) {
            quest.setEndDate(null);
            quest.setStartDate(null);
        }
        questPersistenceService.save(quest, () -> {
            playerPersistenceService.find(player -> {
                player.removeExperience(quest.getExperience());
                if (shouldDecreaseLevel(player)) {
                    player.setLevel(Math.max(Constants.DEFAULT_PLAYER_LEVEL, player.getLevel() - 1));
                    while (shouldDecreaseLevel(player)) {
                        player.setLevel(Math.max(Constants.DEFAULT_PLAYER_LEVEL, player.getLevel() - 1));
                    }
                    eventBus.post(new LevelDownEvent(player.getLevel()));
                }
                player.removeCoins(quest.getCoins());
                playerPersistenceService.save(player);
                eventBus.post(new UndoCompletedQuestEvent(quest));
            });
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
        Quest quest = e.quest;
        quest.setReminders(e.reminders);
        questPersistenceService.save(quest, () -> {
            if (Quest.isCompleted(quest)) {
                onQuestComplete(quest, e.source);
            }
        });
    }

    @Subscribe
    public void onUpdateQuest(UpdateQuestEvent e) {
        e.quest.setSubQuests(e.subQuests);
        questPersistenceService.saveWithNewReminders(e.quest, e.reminders, () -> {
            if (Quest.isCompleted(e.quest)) {
                onQuestComplete(e.quest, e.source);
            }
        });
    }

    @Subscribe
    public void onUpdateRepeatingQuest(UpdateRepeatingQuestEvent e) {
        Observable.defer(() -> {
            RepeatingQuest rq = e.repeatingQuest;
            questPersistenceService.findAllUpcomingForRepeatingQuest(new LocalDate(), rq.getId(), questsToRemove -> {
                for (Quest quest : questsToRemove) {
                    QuestNotificationScheduler.stopAll(quest.getId(), this);
                }
                questPersistenceService.delete(questsToRemove);
                rq.setReminders(e.reminders);

                long todayStartOfDay = DateUtils.toStartOfDayUTC(LocalDate.now()).getTime();
                List<String> periodsToDelete = new ArrayList<>();
                for (String periodEnd : rq.getScheduledPeriodEndDates().keySet()) {
                    if (Long.valueOf(periodEnd) >= todayStartOfDay) {
                        periodsToDelete.add(periodEnd);
                    }
                }
                rq.getScheduledPeriodEndDates().keySet().removeAll(periodsToDelete);
                scheduleRepeatingQuest(rq);
            });
            return Observable.empty();
        }).compose(applyAndroidSchedulers()).subscribe();
    }

    @Subscribe
    public void onDeleteQuestRequest(DeleteQuestRequestEvent e) {
        List<Reminder> reminders = e.quest.getReminders();
        if (reminders != null && !reminders.isEmpty()) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            for (Reminder reminder : reminders) {
                notificationManagerCompat.cancel(reminder.getNotificationId());
            }
        }
        questPersistenceService.delete(e.quest);
    }

    private void onQuestComplete(Quest quest, EventSource source) {
        checkForDailyChallengeCompletion(quest, source);
        updatePlayer(quest);
        eventBus.post(new QuestCompletedEvent(quest, source));
    }

    private void checkForDailyChallengeCompletion(Quest quest, EventSource source) {
        if (quest.getPriority() != Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
            return;
        }
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
        questPersistenceService.countAllCompletedWithPriorityForDate(Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY, LocalDate.now(), questCount -> {
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
        });
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
        playerPersistenceService.find(player -> {
            player.addExperience(rewardProvider.getExperience());
            increasePlayerLevelIfNeeded(player);
            player.addCoins(rewardProvider.getCoins());
            playerPersistenceService.save(player);
        });
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
        e.repeatingQuest.setReminders(e.reminders);
        repeatingQuestPersistenceService.save(e.repeatingQuest);
    }

    public void onRepeatingQuestSaved(RepeatingQuest repeatingQuest) {
        scheduleRepeatingQuest(repeatingQuest);
    }

    @Subscribe
    public void onDeleteRepeatingQuestRequest(final DeleteRepeatingQuestRequestEvent e) {
        final RepeatingQuest repeatingQuest = e.repeatingQuest;
        deleteQuestsForRepeating(repeatingQuest);
        repeatingQuestPersistenceService.delete(repeatingQuest);
    }

    private void deleteQuestsForRepeating(RepeatingQuest repeatingQuest) {
        questPersistenceService.findAllNotCompletedForRepeatingQuest(repeatingQuest.getId(), quests -> {
            questPersistenceService.delete(quests);
        });
    }

    private void scheduleRepeatingQuest(RepeatingQuest repeatingQuest) {
        List<RepeatingQuest> repeatingQuests = new ArrayList<>();
        repeatingQuests.add(repeatingQuest);
        scheduleRepeatingQuests(repeatingQuests);
    }

    private void scheduleRepeatingQuests(List<RepeatingQuest> repeatingQuests) {
        persistentRepeatingQuestScheduler.schedule(repeatingQuests, DateUtils.toStartOfDayUTC(LocalDate.now()));
    }

    private void scheduleNextReminder() {
        sendBroadcast(new Intent(ScheduleNextRemindersReceiver.ACTION_SCHEDULE_REMINDERS));
    }

    @Subscribe
    public void onDeleteChallengeRequest(DeleteChallengeRequestEvent e) {
        questPersistenceService.findAllForChallenge(e.challenge.getId(), quests -> {
            for (Quest quest : quests) {
                quest.setChallengeId(null);
            }

            questPersistenceService.save(quests);

            repeatingQuestPersistenceService.findAllForChallenge(e.challenge, repeatingQuests -> {
                for (RepeatingQuest repeatingQuest : repeatingQuests) {
                    repeatingQuest.setChallengeId(null);
                }
                repeatingQuestPersistenceService.save(repeatingQuests);
            });

            challengePersistenceService.delete(e.challenge);
        });
    }

    @Subscribe
    public void onCompleteChallengeRequest(CompleteChallengeRequestEvent e) {
        Challenge challenge = e.challenge;
        challenge.setCompletedAtDate(new Date());
        challengePersistenceService.save(challenge);
        onChallengeComplete(challenge, e.source);
    }

    private void onChallengeComplete(Challenge challenge, EventSource source) {
        updatePlayer(challenge);
        showChallengeCompleteDialog(getString(R.string.challenge_complete, challenge.getName()), challenge.getExperience(), challenge.getCoins());
        eventBus.post(new ChallengeCompletedEvent(challenge, source));
    }

    private void listenForWidgetQuestsChange() {
        questPersistenceService.listenForAllNonAllDayIncompleteForDate(new LocalDate(), quests -> {
            localStorage.saveString(Constants.WIDGET_AGENDA_QUESTS, gson.toJson(quests));
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                    new ComponentName(this, AgendaWidgetProvider.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_agenda_list);
        });
    }

    @Subscribe
    public void onNewChallenge(NewChallengeEvent e) {
        challengePersistenceService.save(e.challenge);
    }

    @Subscribe
    public void onUpdateChallenge(UpdateChallengeEvent e) {
        challengePersistenceService.save(e.challenge);
    }

    @Subscribe
    public void onTutorialDone(TutorialDoneEvent e) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        syncCalendars().subscribe(o -> {
        }, Throwable::printStackTrace, () -> {
            eventBus.post(new SyncCompleteEvent());
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
            CalendarProvider provider = new CalendarProvider(this);
            List<Calendar> calendars = provider.getCalendars().getList();
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
            androidCalendarQuestService.save(nonRepeating);
            androidCalendarRepeatingQuestService.save(repeating);
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

    public static String getPlayerId() {
        return playerId;
    }
}