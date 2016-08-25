package io.ipoli.android.app;

import android.Manifest;
import android.app.PendingIntent;
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
import android.support.v7.app.NotificationCompat;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.QuickAddActivity;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.events.ScheduleRepeatingQuestsEvent;
import io.ipoli.android.app.events.StartQuickAddEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.events.VersionUpdatedEvent;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListPersistenceService;
import io.ipoli.android.app.services.readers.AndroidCalendarRepeatingQuestListPersistenceService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.avatar.Avatar;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
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
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.pet.persistence.PetPersistenceService;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.activities.LevelUpActivity;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.LevelUpEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.activities.QuestActivity;
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
import io.ipoli.android.quest.persistence.OnDataChangedListener;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.receivers.ScheduleNextRemindersReceiver;
import io.ipoli.android.quest.schedulers.PersistentRepeatingQuestScheduler;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.quest.ui.events.UpdateRepeatingQuestEvent;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.widgets.AgendaWidgetProvider;
import io.ipoli.android.reminders.data.Reminder;
import io.ipoli.android.settings.events.DailyChallengeStartTimeChangedEvent;
import io.ipoli.android.settings.events.OngoingNotificationChangeEvent;
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
    AvatarPersistenceService avatarPersistenceService;

    @Inject
    PetPersistenceService petPersistenceService;

    @Inject
    AndroidCalendarQuestListPersistenceService androidCalendarQuestService;

    @Inject
    AndroidCalendarRepeatingQuestListPersistenceService androidCalendarRepeatingQuestService;

    @Inject
    ExperienceRewardGenerator experienceRewardGenerator;

    @Inject
    CoinsRewardGenerator coinsRewardGenerator;

    BroadcastReceiver dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            decreasePetHealth();
            scheduleQuestsFor4WeeksAhead();
            eventBus.post(new CurrentDayChangedEvent(new LocalDate(), CurrentDayChangedEvent.Source.CALENDAR));
            moveIncompleteQuestsToInbox();
            listenForChanges();
        }
    };

    private OnDataChangedListener<List<Quest>> dailyQuestsChangedListener = quests -> {
        if (quests.isEmpty()) {
            updateOngoingNotification(null, 0, 0);
            return;
        }

        List<Quest> uncompletedQuests = new ArrayList<>();
        for (Quest q : quests) {
            if (!Quest.isCompleted(q)) {
                uncompletedQuests.add(q);
            }
        }

        if (uncompletedQuests.isEmpty()) {
            updateOngoingNotification(null, quests.size(), quests.size());
            return;
        }

        Collections.sort(uncompletedQuests, (q1, q2) -> {
            if (q1.getStartMinute() > -1 && q2.getStartMinute() > -1) {
                return Integer.compare(q1.getStartMinute(), q2.getStartMinute());
            }

            return q1.getStartMinute() >= q2.getStartMinute() ? -1 : 1;
        });

        Quest quest = uncompletedQuests.get(0);
        updateOngoingNotification(quest, quests.size() - uncompletedQuests.size(), quests.size());
    };
    public static final double MAX_PENALTY_COEFFICIENT = 0.5;
    public static final double IMPORTANT_QUEST_PENALTY_PERCENT = 5;

    private void listenForChanges() {
        questPersistenceService.removeAllListeners();
        repeatingQuestPersistenceService.removeAllListeners();
        listenForWidgetQuestsChange();
        listenForRepeatingQuestChange();
        listenForReminderChange();
        if (localStorage.readBool(Constants.KEY_ONGOING_NOTIFICATION_ENABLED, Constants.DEFAULT_ONGOING_NOTIFICATION_ENABLED)) {
            listenForDailyQuestsChange();
        }
    }

    private void decreasePetHealth() {
        questPersistenceService.findAllNonAllDayForDate(LocalDate.now().minusDays(1), quests ->
                petPersistenceService.find(pet -> updatePet(pet, pet.getHealthPointsPercentage() - getDecreasePercentage(quests)))
        );
    }

    private void updatePet(Pet pet, int newHealthPointsPercentage) {
        pet.setHealthPointsPercentage(newHealthPointsPercentage);
        pet.setExperienceBonusPercentage((int) Math.floor(newHealthPointsPercentage * Constants.XP_BONUS_PERCENTAGE_OF_HP / 100.0));
        pet.setCoinsBonusPercentage((int) Math.floor(newHealthPointsPercentage * Constants.COINS_BONUS_PERCENTAGE_OF_HP / 100.0));

        localStorage.saveInt(Constants.KEY_XP_BONUS_PERCENTAGE, pet.getExperienceBonusPercentage());
        localStorage.saveInt(Constants.KEY_COINS_BONUS_PERCENTAGE, pet.getCoinsBonusPercentage());
        petPersistenceService.save(pet);
    }

    private int getDecreasePercentage(List<Quest> quests) {
        if (quests.isEmpty()) {
            return (int) (MAX_PENALTY_COEFFICIENT * 100);
        }

        int decreasePercentage = 0;
        if (quests.size() == 1) {
            decreasePercentage += 30;
        }

        if (quests.size() == 2) {
            decreasePercentage += 20;
        }

        Set<Quest> uncompletedQuests = new HashSet<>();
        int uncompletedImportantQuestCount = 0;
        for (Quest q : quests) {
            if (!Quest.isCompleted(q)) {
                uncompletedQuests.add(q);
                if (q.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
                    uncompletedImportantQuestCount++;
                }
            }
        }

        double uncompletedRatio = uncompletedQuests.size() / quests.size();

        int randomNoise = new Random().nextInt(21) - 10;
        decreasePercentage += (int) (uncompletedRatio * MAX_PENALTY_COEFFICIENT + (uncompletedImportantQuestCount * IMPORTANT_QUEST_PENALTY_PERCENT) + randomNoise);
        decreasePercentage = (int) Math.min(decreasePercentage, MAX_PENALTY_COEFFICIENT * 100);
        return decreasePercentage;
    }

    private void listenForDailyQuestsChange() {
        questPersistenceService.listenForAllNonAllDayForDate(LocalDate.now(), dailyQuestsChangedListener);
    }

    @Subscribe
    public void onOngoingNotificationChange(OngoingNotificationChangeEvent e) {
        if (e.isEnabled) {
            listenForDailyQuestsChange();
        } else {
            NotificationManagerCompat.from(this).cancel(Constants.ONGOING_NOTIFICATION_ID);
            questPersistenceService.removeDataChangedListener(dailyQuestsChangedListener);
        }
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

    private void updateOngoingNotification(Quest quest, int completedCount, int totalCount) {
        String title = "";
        if (quest != null) {
            title = quest.getName();
        } else if (totalCount == 0) {
            title = getString(R.string.ongoing_notification_no_quests_title);
        } else {
            title = getString(R.string.ongoing_notification_done_title);
        }

        String text = totalCount == 0 ? getString(R.string.ongoing_notification_no_quests_text) : getString(R.string.ongoing_notification_progress_text, completedCount, totalCount);
        boolean showWhen = quest != null && quest.getStartMinute() > -1;
        long when = showWhen ? Quest.getStartDateTime(quest).getTime() : 0;
        String contentInfo = quest == null ? "" : "for " + DurationFormatter.format(this, quest.getDuration());
        int smallIcon = quest == null ? R.drawable.ic_notification_small : Quest.getCategory(quest).whiteImage;
        int iconColor = quest == null ? R.color.md_grey_500 : Quest.getCategory(quest).color500;

        Intent startAppIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent addIntent = new Intent(this, QuickAddActivity.class);
        addIntent.putExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT, " " + getString(R.string.today).toLowerCase());

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setShowWhen(showWhen)
                .setWhen(when)
                .setContentInfo(contentInfo)
                .setSmallIcon(smallIcon)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .addAction(R.drawable.ic_add_white_24dp, getString(R.string.add), PendingIntent.getActivity(this, 0, addIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setColor(ContextCompat.getColor(this, iconColor))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if (quest != null) {
            if (quest.isStarted()) {
                builder.addAction(R.drawable.ic_clear_24dp, getString(R.string.cancel).toUpperCase(), getCancelPendingIntent(quest.getId()));
            } else {
                builder.addAction(R.drawable.ic_play_arrow_black_24dp, getString(R.string.start).toUpperCase(), getStartPendingIntent(quest.getId()));
            }
            builder.addAction(R.drawable.ic_done_24dp, getString(R.string.done), getDonePendingIntent(quest.getId()));
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.ONGOING_NOTIFICATION_ID, builder.build());
    }

    private PendingIntent getStartPendingIntent(String questId) {
        Intent intent = new Intent(this, QuestActivity.class);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        intent.setAction(QuestActivity.ACTION_START_QUEST);

        return ActivityIntentFactory.createWithParentStack(QuestActivity.class, intent, this, new Random().nextInt());
    }

    private PendingIntent getCancelPendingIntent(String questId) {
        Intent intent = new Intent(this, QuestActivity.class);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        intent.setAction(QuestActivity.ACTION_QUEST_CANCELED);

        return ActivityIntentFactory.createWithParentStack(QuestActivity.class, intent, this);
    }

    private PendingIntent getDonePendingIntent(String questId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        intent.setAction(MainActivity.ACTION_QUEST_COMPLETE);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
    public void onStartQuickAddEvent(StartQuickAddEvent e) {
        Intent intent = new Intent(this, QuickAddActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT, e.additionalText);
        startActivity(intent);
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
    }

    @Subscribe
    public void onCompleteQuestRequest(CompleteQuestRequestEvent e) {
        Quest q = e.quest;
        QuestNotificationScheduler.stopAll(q.getId(), this);
        q.setCompletedAtDate(new Date());
        q.setCompletedAtMinute(Time.now().toMinutesAfterMidnight());
        q.setExperience(experienceRewardGenerator.generate(q));
        q.setCoins(coinsRewardGenerator.generate(q));
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
            avatarPersistenceService.find(avatar -> {
                avatar.removeExperience(quest.getExperience());
                if (shouldDecreaseLevel(avatar)) {
                    avatar.setLevel(Math.max(Constants.DEFAULT_AVATAR_LEVEL, avatar.getLevel() - 1));
                    while (shouldDecreaseLevel(avatar)) {
                        avatar.setLevel(Math.max(Constants.DEFAULT_AVATAR_LEVEL, avatar.getLevel() - 1));
                    }
                    eventBus.post(new LevelDownEvent(avatar.getLevel()));
                }
                avatar.removeCoins(quest.getCoins());
                avatarPersistenceService.save(avatar);
                eventBus.post(new UndoCompletedQuestEvent(quest));
            });
        });
    }

    private boolean shouldIncreaseLevel(Avatar avatar) {
        return new BigInteger(avatar.getExperience()).compareTo(ExperienceForLevelGenerator.forLevel(avatar.getLevel() + 1)) >= 0;
    }

    private boolean shouldDecreaseLevel(Avatar avatar) {
        return new BigInteger(avatar.getExperience()).compareTo(ExperienceForLevelGenerator.forLevel(avatar.getLevel())) < 0;
    }

    @Subscribe
    public void onNewQuest(NewQuestEvent e) {
        Quest quest = e.quest;
        quest.setDuration(Math.max(quest.getDuration(), Constants.QUEST_MIN_DURATION));
        quest.setReminders(e.reminders);
        if (Quest.isCompleted(quest)) {
            quest.setExperience(experienceRewardGenerator.generate(quest));
            quest.setCoins(coinsRewardGenerator.generate(quest));
        }
        questPersistenceService.save(quest, () -> {
            if (Quest.isCompleted(quest)) {
                onQuestComplete(quest, e.source);
            }
        });
    }

    @Subscribe
    public void onUpdateQuest(UpdateQuestEvent e) {
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
        updateAvatar(quest);
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

            long xp = experienceRewardGenerator.generateForDailyChallenge();
            long coins = coinsRewardGenerator.generateForDailyChallenge();
            Challenge dailyChallenge = new Challenge();
            dailyChallenge.setExperience(xp);
            dailyChallenge.setCoins(coins);
            updateAvatar(dailyChallenge);
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

    private void updateAvatar(RewardProvider rewardProvider) {
        avatarPersistenceService.find(avatar -> {
            avatar.addExperience(rewardProvider.getExperience());
            increaseAvatarLevelIfNeeded(avatar);
            avatar.addCoins(rewardProvider.getCoins());
            avatarPersistenceService.save(avatar);
        });
    }

    private void increaseAvatarLevelIfNeeded(Avatar avatar) {
        if (shouldIncreaseLevel(avatar)) {
            avatar.setLevel(avatar.getLevel() + 1);
            while (shouldIncreaseLevel(avatar)) {
                avatar.setLevel(avatar.getLevel() + 1);
            }
            Intent intent = new Intent(this, LevelUpActivity.class);
            intent.putExtra(LevelUpActivity.LEVEL, avatar.getLevel());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            eventBus.post(new LevelUpEvent(avatar.getLevel()));
        }
    }

    @Subscribe
    public void onNewRepeatingQuest(NewRepeatingQuestEvent e) {
        RepeatingQuest repeatingQuest = e.repeatingQuest;
        repeatingQuest.setDuration(Math.max(repeatingQuest.getDuration(), Constants.QUEST_MIN_DURATION));
        repeatingQuest.setReminders(e.reminders);
        repeatingQuestPersistenceService.save(repeatingQuest);
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
        updateAvatar(challenge);
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
        syncCalendars().subscribe();
    }

    @Subscribe
    public void onSyncWithCalendarRequest(SyncCalendarRequestEvent e) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        syncCalendars().subscribe();
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