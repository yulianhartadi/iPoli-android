package io.ipoli.android.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.facebook.FacebookSdk;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.QuickAddActivity;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.events.DateChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.events.ScheduleRepeatingQuestsEvent;
import io.ipoli.android.app.events.StartQuickAddEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.events.VersionUpdatedEvent;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.receivers.DateChangedReceiver;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.settings.events.DailyChallengeStartTimeChangedEvent;
import io.ipoli.android.app.settings.events.OngoingNotificationChangeEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.avatar.Avatar;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.challenge.activities.ChallengeCompleteActivity;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.data.Difficulty;
import io.ipoli.android.challenge.data.PredefinedChallenge;
import io.ipoli.android.challenge.events.ChallengeCompletedEvent;
import io.ipoli.android.challenge.events.DailyChallengeCompleteEvent;
import io.ipoli.android.challenge.events.NewChallengeEvent;
import io.ipoli.android.challenge.events.RemoveBaseQuestFromChallengeEvent;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.receivers.ScheduleDailyChallengeReminderReceiver;
import io.ipoli.android.challenge.ui.events.CompleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.DeleteChallengeRequestEvent;
import io.ipoli.android.challenge.ui.events.UpdateChallengeEvent;
import io.ipoli.android.pet.PetActivity;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.pet.persistence.PetPersistenceService;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.activities.LevelUpActivity;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.LevelUpEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.quest.data.Category;
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
import io.ipoli.android.quest.receivers.CompleteQuestReceiver;
import io.ipoli.android.quest.receivers.ScheduleNextRemindersReceiver;
import io.ipoli.android.quest.receivers.StartQuestReceiver;
import io.ipoli.android.quest.receivers.StopQuestReceiver;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.quest.ui.events.UpdateRepeatingQuestEvent;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import io.ipoli.android.quest.widgets.AgendaWidgetProvider;

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
    RepeatingQuestScheduler repeatingQuestScheduler;

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
    ExperienceRewardGenerator experienceRewardGenerator;

    @Inject
    CoinsRewardGenerator coinsRewardGenerator;

    private OnDataChangedListener<List<Quest>> dailyQuestsChangedListener = quests -> {
        if (quests.isEmpty()) {
            updateOngoingNotification(null, 0, 0);
            return;
        }

        List<Quest> uncompletedQuests = new ArrayList<>();
        for (Quest q : quests) {
            if (!q.isCompleted()) {
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
        listenForReminderChange();
        if (localStorage.readBool(Constants.KEY_ONGOING_NOTIFICATION_ENABLED, Constants.DEFAULT_ONGOING_NOTIFICATION_ENABLED)) {
            listenForDailyQuestsChange();
        }
    }

    private void updatePet(int healthPoints) {
        petPersistenceService.find(pet -> {

            if (pet.getState() == Pet.PetState.DEAD) {
                return;
            }

            Pet.PetState initialState = pet.getState();
            pet.addHealthPoints(healthPoints);

            Pet.PetState currentState = pet.getState();

            if (healthPoints < 0 && initialState != currentState && (currentState == Pet.PetState.DEAD || currentState == Pet.PetState.SAD)) {
                notifyPetStateChanged(pet);
            }
            petPersistenceService.save(pet);
        });
    }

    private void notifyPetStateChanged(Pet pet) {
        String title = pet.getState() == Pet.PetState.DEAD ? pet.getName() + " has died" : "I am so sad, don't let me die";
        String text = pet.getState() == Pet.PetState.DEAD ? "Revive " + pet.getName() + " to help you with your quests!" :
                "Complete your quests to make me happy!";

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), ResourceUtils.extractDrawableResource(this, pet.getPicture() + "_head"));
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, PetActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.PET_STATE_CHANGED_NOTIFICATION_ID, builder.build());
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
            if (!q.isCompleted()) {
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

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        getAppComponent(this).inject(this);
        registerServices();
        if (StringUtils.isEmpty(localStorage.readString(Constants.KEY_PLAYER_ID))) {
            return;
        }
        playerId = localStorage.readString(Constants.KEY_PLAYER_ID);

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
                builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop).toUpperCase(), getStopPendingIntent(quest.getId()));
            } else {
                builder.addAction(R.drawable.ic_play_arrow_black_24dp, getString(R.string.start).toUpperCase(), getStartPendingIntent(quest.getId()));
            }
            builder.addAction(R.drawable.ic_done_24dp, getString(R.string.done), getDonePendingIntent(quest.getId()));
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.ONGOING_NOTIFICATION_ID, builder.build());
    }

    private PendingIntent getStartPendingIntent(String questId) {
        Intent intent = new Intent(StartQuestReceiver.ACTION_START_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(this, intent);
    }

    private PendingIntent getStopPendingIntent(String questId) {
        Intent intent = new Intent(StopQuestReceiver.ACTION_STOP_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(this, intent);
    }

    private PendingIntent getDonePendingIntent(String questId) {
        Intent intent = new Intent(CompleteQuestReceiver.ACTION_COMPLETE_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(this, intent);
    }

    private void initAppStart() {
        int versionCode = localStorage.readInt(Constants.KEY_APP_VERSION_CODE);
        if (versionCode != BuildConfig.VERSION_CODE) {
            scheduleDailyChallenge();
            localStorage.saveInt(Constants.KEY_APP_VERSION_CODE, BuildConfig.VERSION_CODE);
            if (versionCode > 0) {
                eventBus.post(new VersionUpdatedEvent(versionCode, BuildConfig.VERSION_CODE));
            }
        }

        scheduleQuestsFor4WeeksAhead();
        moveIncompleteQuestsToInbox();
        scheduleNextReminder();
        listenForChanges();
        scheduleDateChanged();
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
            }
            questPersistenceService.updateNewQuests(quests);
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
        QuestNotificationScheduler.cancelAll(q, this);
        q.setCompletedAtDate(new Date());
        q.setEndDateFromLocal(new Date());
        q.setCompletedAtMinute(Time.now().toMinutesAfterMidnight());
        q.setExperience(experienceRewardGenerator.generate(q));
        q.setCoins(coinsRewardGenerator.generate(q));
        questPersistenceService.updateNewQuest(q);
        onQuestComplete(q, e.source);
    }

    @Subscribe
    public void onRemoveBaseQuestFromChallenge(RemoveBaseQuestFromChallengeEvent e) {
        BaseQuest bq = e.baseQuest;
        if (bq instanceof Quest) {
            Quest q = (Quest) bq;
            q.setChallengeId(null);
            questPersistenceService.updateNewQuest(q);
        } else {
            RepeatingQuest rq = (RepeatingQuest) bq;
            rq.setChallengeId(null);
            repeatingQuestPersistenceService.updateNewRepeatingQuest(rq);
        }
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
        Long xp = quest.getExperience();
        Long coins = quest.getCoins();
        quest.setExperience(null);
        quest.setCoins(null);
        questPersistenceService.updateNewQuest(quest);
        avatarPersistenceService.find(avatar -> {
            avatar.removeExperience(xp);
            if (shouldDecreaseLevel(avatar)) {
                avatar.setLevel(Math.max(Constants.DEFAULT_AVATAR_LEVEL, avatar.getLevel() - 1));
                while (shouldDecreaseLevel(avatar)) {
                    avatar.setLevel(Math.max(Constants.DEFAULT_AVATAR_LEVEL, avatar.getLevel() - 1));
                }
                eventBus.post(new LevelDownEvent(avatar.getLevel()));
            }
            avatar.removeCoins(coins);
            avatarPersistenceService.save(avatar);
        });

        updatePet((int) -Math.floor(xp / Constants.XP_TO_PET_HP_RATIO));
        eventBus.post(new UndoCompletedQuestEvent(quest, xp, coins));
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
        if (quest.isScheduledForThePast()) {
            setQuestCompletedAt(quest);
        }
        if (quest.isCompleted()) {
            quest.setExperience(experienceRewardGenerator.generate(quest));
            quest.setCoins(coinsRewardGenerator.generate(quest));
        }
        questPersistenceService.saveNewQuest(quest);
        if (quest.isCompleted()) {
            onQuestComplete(quest, e.source);
        }
    }

    @Subscribe
    public void onUpdateQuest(UpdateQuestEvent e) {
        Quest quest = e.quest;
        if (quest.isScheduledForThePast() && !quest.isCompleted()) {
            setQuestCompletedAt(quest);
        }
        if (quest.isCompleted()) {
            quest.setExperience(experienceRewardGenerator.generate(quest));
            quest.setCoins(coinsRewardGenerator.generate(quest));
        }
        questPersistenceService.updateNewQuest(quest);
        if (quest.isCompleted()) {
            onQuestComplete(quest, e.source);
        }
    }

    private void setQuestCompletedAt(Quest quest) {
        Date completedAt = new LocalDate(quest.getEndDate(), DateTimeZone.UTC).toDate();
        Calendar c = Calendar.getInstance();
        c.setTime(completedAt);

        int completedAtMinute = Time.now().toMinutesAfterMidnight();
        if (quest.hasStartTime()) {
            completedAtMinute = quest.getStartMinute();
        }
        c.add(Calendar.MINUTE, completedAtMinute);
        quest.setCompletedAtDate(c.getTime());
        quest.setCompletedAtMinute(completedAtMinute);
    }

    @Subscribe
    public void onUpdateRepeatingQuest(UpdateRepeatingQuestEvent e) {
        RepeatingQuest repeatingQuest = e.repeatingQuest;
        questPersistenceService.findAllUpcomingForRepeatingQuest(new LocalDate(), repeatingQuest.getId(), questsToRemove -> {
            for (Quest quest : questsToRemove) {
                QuestNotificationScheduler.cancelAll(quest, this);
            }

            long todayStartOfDay = DateUtils.toStartOfDayUTC(LocalDate.now()).getTime();
            List<String> periodsToDelete = new ArrayList<>();
            for (String periodEnd : repeatingQuest.getScheduledPeriodEndDates().keySet()) {
                if (Long.valueOf(periodEnd) >= todayStartOfDay) {
                    periodsToDelete.add(periodEnd);
                }
            }
            repeatingQuest.getScheduledPeriodEndDates().keySet().removeAll(periodsToDelete);
            List<Quest> questsToCreate = repeatingQuestScheduler.scheduleAhead(repeatingQuest, DateUtils.toStartOfDayUTC(LocalDate.now()));
            repeatingQuestPersistenceService.updateNewRepeatingQuest(repeatingQuest, questsToRemove, questsToCreate);
        });
    }

    @Subscribe
    public void onDeleteQuestRequest(DeleteQuestRequestEvent e) {
        QuestNotificationScheduler.cancelAll(e.quest, this);
        questPersistenceService.deleteNewQuest(e.quest);
    }

    private void onQuestComplete(Quest quest, EventSource source) {
        checkForDailyChallengeCompletion(quest);
        updateAvatar(quest);
        updatePet((int) (Math.floor(quest.getExperience() / Constants.XP_TO_PET_HP_RATIO)));
        eventBus.post(new QuestCompletedEvent(quest, source));
    }

    private void checkForDailyChallengeCompletion(Quest quest) {
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
            Long experience = rewardProvider.getExperience();
            avatar.addExperience(experience);
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

        List<Quest> quests = repeatingQuestScheduler.scheduleAhead(repeatingQuest, DateUtils.toStartOfDayUTC(LocalDate.now()));

        repeatingQuestPersistenceService.saveNewRepeatingQuest(repeatingQuest, quests);
    }

    @Subscribe
    public void onDeleteRepeatingQuestRequest(final DeleteRepeatingQuestRequestEvent e) {
        final RepeatingQuest repeatingQuest = e.repeatingQuest;
        questPersistenceService.findAllNotCompletedForRepeatingQuest(repeatingQuest.getId(), quests -> {
            repeatingQuestPersistenceService.deleteNewRepeatingQuest(repeatingQuest, quests);
        });
    }

    private void scheduleRepeatingQuests(List<RepeatingQuest> repeatingQuests) {
        Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests = new HashMap<>();
        for (RepeatingQuest repeatingQuest : repeatingQuests) {
            List<Quest> quests = repeatingQuestScheduler.scheduleAhead(repeatingQuest, DateUtils.toStartOfDayUTC(LocalDate.now()));
            repeatingQuestToScheduledQuests.put(repeatingQuest, quests);
        }
        repeatingQuestPersistenceService.saveScheduledRepeatingQuests(repeatingQuestToScheduledQuests);
    }

    private void scheduleNextReminder() {
        sendBroadcast(new Intent(ScheduleNextRemindersReceiver.ACTION_SCHEDULE_REMINDERS));
    }

    @Subscribe
    public void onDeleteChallengeRequest(DeleteChallengeRequestEvent e) {
        challengePersistenceService.deleteNewChallenge(e.challenge);
    }

    @Subscribe
    public void onCompleteChallengeRequest(CompleteChallengeRequestEvent e) {
        Challenge challenge = e.challenge;
        challenge.setCompletedAtDate(new Date());
        challenge.setExperience(experienceRewardGenerator.generate(challenge));
        challenge.setCoins(coinsRewardGenerator.generate(challenge));
        challengePersistenceService.save(challenge);
        onChallengeComplete(challenge, e.source);
    }

    private void onChallengeComplete(Challenge challenge, EventSource source) {
        updateAvatar(challenge);
        updatePet((int) (Math.floor(challenge.getExperience() / Constants.XP_TO_PET_HP_RATIO)));
        showChallengeCompleteDialog(getString(R.string.challenge_complete, challenge.getName()), challenge.getExperience(), challenge.getCoins());
        eventBus.post(new ChallengeCompletedEvent(challenge, source));
    }

    private void listenForWidgetQuestsChange() {
        questPersistenceService.listenForDayQuestChange(LocalDate.now(), new OnChangeListener<Void>() {
            @Override
            public void onNew(Void data) {
                requestWidgetUpdate();
            }

            @Override
            public void onChanged(Void data) {
                requestWidgetUpdate();
            }

            @Override
            public void onDeleted() {
                requestWidgetUpdate();
            }
        });
    }

    private void requestWidgetUpdate() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, AgendaWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_agenda_list);
    }

    @Subscribe
    public void onNewChallenge(NewChallengeEvent e) {
        challengePersistenceService.save(e.challenge);
    }

    @Subscribe
    public void onUpdateChallenge(UpdateChallengeEvent e) {
        challengePersistenceService.save(e.challenge);
    }

    private void scheduleDateChanged() {
        Intent i = new Intent(DateChangedReceiver.ACTION_DATE_CHANGED);
        PendingIntent pendingIntent = IntentUtils.getBroadcastPendingIntent(this, i);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        long notificationTime = DateUtils.toStartOfDayUTC(LocalDate.now().plusDays(1)).getTime() + 5000L;
        if (Build.VERSION.SDK_INT > 22) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }
    }

    @Subscribe
    public void onDateChanged(DateChangedEvent e) {
        questPersistenceService.findAllNonAllDayForDate(LocalDate.now().minusDays(1), quests -> {
            updatePet(-getDecreasePercentage(quests));
            scheduleQuestsFor4WeeksAhead();
            eventBus.post(new CalendarDayChangedEvent(new LocalDate(), CalendarDayChangedEvent.Source.CALENDAR));
            moveIncompleteQuestsToInbox();
            listenForChanges();
        });

    }

    public static String getPlayerId() {
        return playerId;
    }

    public static List<PredefinedChallenge> getPredefinedChallenges() {
        List<PredefinedChallenge> challenges = new ArrayList<>();

        Challenge c = new Challenge("Stress-Free Mind");
        c.setCategoryType(Category.WELLNESS);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusWeeks(2)));
        c.setReason1("Be more focused");
        c.setReason2("Be relaxed");
        c.setReason3("Be healthy");
        c.setExpectedResult1("Concentrate for at least 30 min at a time");
        c.setExpectedResult2("Completely stop anxiety");
        c.setExpectedResult3("Lose 5 pounds");
        challenges.add(new PredefinedChallenge(c, "Be mindful and stay in the flow longer", R.drawable.challenge_02, R.drawable.challenge_expanded_02));

        c = new Challenge("Weight Cutter");
        c.setCategoryType(Category.WELLNESS);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusWeeks(2)));
        c.setReason1("Feel great");
        c.setReason2("Become more confident");
        c.setReason3("Become healthier");
        c.setExpectedResult1("Lose 10 pounds");
        c.setExpectedResult2("Reduce wear size");
        c.setExpectedResult3("Love working out");
        challenges.add(new PredefinedChallenge(c, "Start shedding some weight and feel great", R.drawable.challenge_01, R.drawable.challenge_expanded_01));


        c = new Challenge("Healthy & Fit");
        c.setCategoryType(Category.WELLNESS);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusWeeks(2)));
        c.setReason1("Be healthier");
        c.setReason2("Stay fit");
        c.setReason3("Feel great");
        c.setExpectedResult1("Lose 5 pounds");
        c.setExpectedResult2("Concentrate for at least 30 min at a time");
        c.setExpectedResult3("Workout 3 times a week");
        challenges.add(new PredefinedChallenge(c, "Keep working out and live healthier life", R.drawable.challenge_03, R.drawable.challenge_expanded_03));

        c = new Challenge("English Jedi");
        c.setCategoryType(Category.LEARNING);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusWeeks(2)));
        c.setReason1("Learn to read great books");
        c.setReason2("Participate in conversations");
        c.setReason3("Meet & speak with new people");
        c.setExpectedResult1("Read a book");
        c.setExpectedResult2("Understand movies");
        c.setExpectedResult3("Write an essey in English");
        challenges.add(new PredefinedChallenge(c, "Advance your English skills", R.drawable.challenge_04, R.drawable.challenge_expanded_04));

        c = new Challenge("Programming Ninja");
        c.setCategoryType(Category.LEARNING);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusWeeks(2)));
        c.setReason1("Learn to command my computer");
        c.setReason2("Understand technologies better");
        c.setReason3("Find new job");
        c.setExpectedResult1("Write simple webpage");
        c.setExpectedResult2("Understand what code editors are");
        c.setExpectedResult3("Understand how Internet works");
        challenges.add(new PredefinedChallenge(c, "Learn the fundamentals of computer programming", R.drawable.challenge_05, R.drawable.challenge_expanded_05));

        c = new Challenge("Master Presenter");
        c.setCategoryType(Category.WORK);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusWeeks(2)));
        c.setReason1("Better present my ideas");
        c.setReason2("Become more confident");
        c.setReason3("Explain better");
        c.setExpectedResult1("Prepare a presentation");
        c.setExpectedResult2("Present in front of an audience");
        c.setExpectedResult3("Upload my presentation on the Internet");
        challenges.add(new PredefinedChallenge(c, "Learn how to create and present effectively", R.drawable.challenge_06, R.drawable.challenge_expanded_06));

        c = new Challenge("Famous writer");
        c.setCategoryType(Category.WORK);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusWeeks(2)));
        c.setReason1("Better present my ideas");
        c.setReason2("Become more confident");
        c.setReason3("Meet new people");
        c.setExpectedResult1("Have a blog");
        c.setExpectedResult2("100 readers per month");
        c.setExpectedResult3("Write 5 blog posts");
        challenges.add(new PredefinedChallenge(c, "Learn how to become great writer & blogger", R.drawable.challenge_07, R.drawable.challenge_expanded_07));

        c = new Challenge("Friends & Family time");
        c.setCategoryType(Category.PERSONAL);
        c.setDifficultyType(Difficulty.NORMAL);
        c.setEndDate(DateUtils.toStartOfDayUTC(LocalDate.now().plusWeeks(2)));
        c.setReason1("Feel more connected to others");
        c.setReason2("Have more fun");
        c.setReason3("Stay close with family");
        c.setExpectedResult1("Spend more time with family");
        c.setExpectedResult2("Go out more often");
        c.setExpectedResult3("See friends more often");
        challenges.add(new PredefinedChallenge(c, "Connect with your friends and family", R.drawable.challenge_08, R.drawable.challenge_expanded_08));

        return challenges;
    }
}