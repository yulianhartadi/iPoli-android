package io.ipoli.android.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.amplitude.api.Amplitude;
import com.couchbase.lite.Database;
import com.couchbase.lite.replicator.RemoteRequestResponseException;
import com.couchbase.lite.replicator.Replication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.AnalyticsConstants;
import io.ipoli.android.BuildConfig;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.achievement.Achievement;
import io.ipoli.android.achievement.AchievementUnlocker;
import io.ipoli.android.achievement.UnlockAchievementScheduler;
import io.ipoli.android.achievement.actions.AchievementAction;
import io.ipoli.android.achievement.actions.AchievementsUnlockedAction;
import io.ipoli.android.achievement.actions.CompleteChallengeAction;
import io.ipoli.android.achievement.actions.CompleteDailyChallengeAction;
import io.ipoli.android.achievement.actions.CompleteQuestAction;
import io.ipoli.android.achievement.actions.IsFollowedAction;
import io.ipoli.android.achievement.actions.LevelUpAction;
import io.ipoli.android.achievement.actions.SimpleAchievementAction;
import io.ipoli.android.achievement.events.AchievementsUnlockedEvent;
import io.ipoli.android.achievement.persistence.AchievementProgressPersistenceService;
import io.ipoli.android.app.activities.MigrationActivity;
import io.ipoli.android.app.activities.PowerUpDialogActivity;
import io.ipoli.android.app.activities.QuickAddActivity;
import io.ipoli.android.app.activities.SignInActivity;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.api.UrlProvider;
import io.ipoli.android.app.api.events.NewSessionCreatedEvent;
import io.ipoli.android.app.api.events.SessionExpiredEvent;
import io.ipoli.android.app.auth.FacebookAuthService;
import io.ipoli.android.app.auth.GoogleAuthService;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.events.DateChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FinishTutorialActivityEvent;
import io.ipoli.android.app.events.PlayerMigratedEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.StartPowerUpDialogRequestEvent;
import io.ipoli.android.app.events.StartQuickAddEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.events.VersionUpdatedEvent;
import io.ipoli.android.app.modules.AppModule;
import io.ipoli.android.app.rate.events.RateDialogFeedbackSentEvent;
import io.ipoli.android.app.receivers.AndroidCalendarEventChangedReceiver;
import io.ipoli.android.app.receivers.DateChangedReceiver;
import io.ipoli.android.app.services.AnalyticsService;
import io.ipoli.android.app.settings.events.DailyChallengeStartTimeChangedEvent;
import io.ipoli.android.app.settings.events.OngoingNotificationChangeEvent;
import io.ipoli.android.app.share.events.InviteFriendsProviderPickedEvent;
import io.ipoli.android.app.tutorial.TutorialActivity;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.app.utils.Time;
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
import io.ipoli.android.feed.events.PostAddedEvent;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.pet.PetActivity;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.AuthProvider;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.PlayerAuthenticationStatus;
import io.ipoli.android.player.activities.LevelUpActivity;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.LevelUpEvent;
import io.ipoli.android.player.events.PlayerFollowedEvent;
import io.ipoli.android.player.events.PlayerSignedInEvent;
import io.ipoli.android.player.events.ProfileCreatedEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.player.scheduling.PowerUpScheduler;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteRepeatingQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.NewRepeatingQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.events.UndoCompletedQuestRequestEvent;
import io.ipoli.android.quest.events.UpdateQuestEvent;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.generators.RewardProvider;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.receivers.CompleteQuestReceiver;
import io.ipoli.android.quest.receivers.ScheduleNextRemindersReceiver;
import io.ipoli.android.quest.receivers.StartQuestReceiver;
import io.ipoli.android.quest.receivers.StopQuestReceiver;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.quest.schedulers.QuestScheduler;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;
import io.ipoli.android.quest.ui.events.UpdateRepeatingQuestEvent;
import io.ipoli.android.quest.widgets.AgendaWidgetProvider;
import io.ipoli.android.reward.events.RewardUsedEvent;
import io.ipoli.android.store.events.AvatarChangedEvent;
import io.ipoli.android.store.events.PetChangedEvent;
import io.ipoli.android.store.events.PowerUpEnabledEvent;
import okhttp3.Cookie;

import static io.ipoli.android.feed.persistence.FirebaseFeedPersistenceService.achievementsPath;
import static io.ipoli.android.feed.persistence.FirebaseFeedPersistenceService.profilePath;

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
    ObjectMapper objectMapper;

    @Inject
    LocalStorage localStorage;

    @Inject
    Database database;

    @Inject
    Api api;

    @Inject
    RepeatingQuestScheduler repeatingQuestScheduler;

    @Inject
    QuestScheduler questScheduler;

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
    AchievementProgressPersistenceService achievementProgressPersistenceService;

    @Inject
    ExperienceRewardGenerator experienceRewardGenerator;

    @Inject
    CoinsRewardGenerator coinsRewardGenerator;

    @Inject
    UrlProvider urlProvider;

    @Inject
    AchievementUnlocker achievementUnlocker;

    @Inject
    FeedPersistenceService feedPersistenceService;

    private PlayerAuthenticationStatus playerAuthenticationStatus;

    private List<Cookie> cookies;

    private void listenForChanges() {
        questPersistenceService.removeAllListeners();
        listenForDailyQuestsChange();
    }

    private void updatePet(Pet pet, int healthPoints) {
        if (pet.getState() == Pet.PetState.DEAD) {
            return;
        }

        Pet.PetState initialState = pet.getState();
        pet.addHealthPoints(healthPoints);

        Pet.PetState currentState = pet.getState();

        if (healthPoints < 0 && initialState != currentState && (currentState == Pet.PetState.DEAD || currentState == Pet.PetState.SAD)) {
            notifyPetStateChanged(pet);
            if (currentState == Pet.PetState.DEAD) {
                checkForUnlockedAchievement(AchievementAction.Action.PET_DIED);
            }
        }
    }

    private void savePet(int healthPoints) {
        Player player = getPlayer();
        Pet pet = player.getPet();
        updatePet(pet, healthPoints);
        playerPersistenceService.save(player);
    }

    private void notifyPetStateChanged(Pet pet) {
        String title = pet.getState() == Pet.PetState.DEAD ? String.format(getString(R.string.pet_died_notification_title), pet.getName()) : getString(R.string.pet_sad_notification_title);
        String text = pet.getState() == Pet.PetState.DEAD ? String.format(getString(R.string.pet_revive_notification_message), pet.getName()) :
                getString(R.string.pet_notification_message);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), pet.getCurrentAvatar().headPicture);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, PetActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
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
            return (int) (Constants.NO_QUESTS_PENALTY_COEFFICIENT * 100);
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
        decreasePercentage += (int) (uncompletedRatio * Constants.MAX_PENALTY_COEFFICIENT + (uncompletedImportantQuestCount * Constants.IMPORTANT_QUEST_PENALTY_PERCENT) + randomNoise);
        decreasePercentage = (int) Math.min(decreasePercentage, Constants.MAX_PENALTY_COEFFICIENT * 100);
        return decreasePercentage;
    }

    private void listenForDailyQuestsChange() {
        questPersistenceService.listenForAllNonAllDayForDate(LocalDate.now(), quests -> {
            scheduleNextReminder();
            try {
                localStorage.saveString(Constants.KEY_WIDGET_AGENDA_QUEST_LIST, objectMapper.writeValueAsString(quests));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Cant save quests for Widget", e);
            }
            requestWidgetUpdate();

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

            Quest quest = uncompletedQuests.get(0);
            updateOngoingNotification(quest, quests.size() - uncompletedQuests.size(), quests.size());
        });
    }

    @Subscribe
    public void onOngoingNotificationChange(OngoingNotificationChangeEvent e) {
        if (!e.isEnabled) {
            NotificationManagerCompat.from(this).cancel(Constants.ONGOING_NOTIFICATION_ID);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);
        Amplitude.getInstance().initialize(getApplicationContext(), AnalyticsConstants.AMPLITUDE_KEY)
                .enableForegroundTracking(this)
                .trackSessionEvents(false);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.setPersistenceEnabled(true);

        getAppComponent(this).inject(this);

        registerServices();
        initNotificationChannel();

        playerId = localStorage.readString(Constants.KEY_PLAYER_ID);

        if (getPlayer() != null) {
            if (shouldMigratePlayer()) {
                startNewActivity(MigrationActivity.class);
                return;
            }
        } else {
            if (StringUtils.isNotEmpty(playerId)) {
                eventBus.post(new AppErrorEvent(new IllegalStateException("Player with id " + playerId + " has no player")));
            }
            if (localStorage.readBool(Constants.KEY_SHOULD_SHOW_TUTORIAL, true)) {
                startNewActivity(TutorialActivity.class);
            } else {
                startNewActivity(SignInActivity.class);
            }
            return;
        }

        initReplication();
        initAppStart();
    }

    private void initNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, importance);
            channel.setDescription("Notifications for reminders");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private boolean shouldMigratePlayer() {
        Player player = getPlayer();
        return player != null && player.getSchemaVersion() != Constants.SCHEMA_VERSION;
    }

    @Subscribe
    public void onFinishTutorialActivity(FinishTutorialActivityEvent e) {
        if (!hasPlayer()) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.DISPLAY_NAME_EXTRA_KEY, e.playerName);
            startNewActivity(SignInActivity.class, bundle);
        } else {
            Player player = getPlayer();
            player.setDisplayName(e.playerName);
            playerPersistenceService.save(player);
        }
    }

    private void startNewActivity(Class clazz) {
        Intent intent = new Intent(this, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startNewActivity(Class clazz, Bundle params) {
        Intent intent = new Intent(this, clazz);
        intent.putExtras(params);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateOngoingNotification(Quest quest, int completedCount, int totalCount) {
        if (!localStorage.readBool(Constants.KEY_ONGOING_NOTIFICATION_ENABLED, Constants.DEFAULT_ONGOING_NOTIFICATION_ENABLED)) {
            return;
        }

        String title = "";
        if (quest != null) {
            title = quest.getName();
        } else if (totalCount == 0) {
            title = getString(R.string.ongoing_notification_no_quests_title);
        } else {
            title = getString(R.string.ongoing_notification_done_title);
        }

        String text = totalCount == 0 ? getString(R.string.ongoing_notification_no_quests_text) : getString(R.string.ongoing_notification_progress_text, completedCount, totalCount);
        boolean showWhen = quest != null && quest.isScheduled();
        long when = showWhen ? Quest.getStartDateTimeMillis(quest) : 0;
        String contentInfo = quest == null ? "" : String.format(getString(R.string.notification_for_time), DurationFormatter.format(this, quest.getDuration()));
        int smallIcon = quest == null ? R.drawable.ic_notification_small : quest.getCategoryType().whiteImage;
        int iconColor = quest == null ? R.color.md_grey_500 : quest.getCategoryType().color500;

        Intent startAppIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent addIntent = new Intent(this, QuickAddActivity.class);
        addIntent.putExtra(Constants.QUICK_ADD_ADDITIONAL_TEXT, " " + getString(R.string.today).toLowerCase());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
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
            builder.addAction(R.drawable.ic_done_24dp, getString(R.string.done).toUpperCase(), getDonePendingIntent(quest.getId()));
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.ONGOING_NOTIFICATION_ID, builder.build());
    }

    private PendingIntent getStartPendingIntent(String questId) {
        Intent intent = new Intent(this, StartQuestReceiver.class);
        intent.setAction(StartQuestReceiver.ACTION_START_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(this, intent);
    }

    private PendingIntent getStopPendingIntent(String questId) {
        Intent intent = new Intent(this, StopQuestReceiver.class);
        intent.setAction(StopQuestReceiver.ACTION_STOP_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(this, intent);
    }

    private PendingIntent getDonePendingIntent(String questId) {
        Intent intent = new Intent(this, CompleteQuestReceiver.class);
        intent.setAction(CompleteQuestReceiver.ACTION_COMPLETE_QUEST);
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
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference("/").keepSynced(false);
        profilePath(getPlayerId()).toReference(db).keepSynced(true);
        achievementsPath().toReference(db).keepSynced(true);

        scheduleDateChanged();
        scheduleNextReminder();
        listenForChanges();

        Player player = getPlayer();
        if (player.isAuthenticated() && player.hasUsername()) {
            listenForPlayerChanges();
            feedPersistenceService.findProfile(playerId, profile -> {
                if (!profile.getFollowers().isEmpty()) {
                    checkForUnlockedAchievement(new IsFollowedAction(profile.getFollowers().size()));
                }
            });
        }
    }

    @Subscribe
    public void onProfileCreated(ProfileCreatedEvent e) {
        listenForPlayerChanges();
    }

    private void listenForPlayerChanges() {
        playerPersistenceService.listen(player ->
                feedPersistenceService.findProfile(player.getId(), profile ->
                        feedPersistenceService.updateProfile(profile, player)));
    }

    private void initReplication() {
        if (!NetworkConnectivityUtils.isConnectedToInternet(this)) {
            return;
        }

        Player player = getPlayer();
        if (!player.isAuthenticated()) {
            return;
        }
        syncData(new ArrayList<>());
    }

    private void syncData(List<Cookie> cookies) {
        try {
            Replication pull = database.createPullReplication(urlProvider.sync());
            for (Cookie cookie : cookies) {
                pull.setCookie(cookie.name(), cookie.value(), cookie.path(),
                        new Date(cookie.expiresAt()), cookie.secure(), cookie.httpOnly());
            }
            pull.setContinuous(true);
            List<String> channels = new ArrayList<>();
            channels.add(playerId);
            pull.setChannels(channels);

            Replication push = database.createPushReplication(urlProvider.sync());
            for (Cookie cookie : cookies) {
                push.setCookie(cookie.name(), cookie.value(), cookie.path(),
                        new Date(cookie.expiresAt()), cookie.secure(), cookie.httpOnly());
            }
            push.setContinuous(true);

            push.addChangeListener(changeEvent -> {
                Throwable error = changeEvent.getError();
                if (error != null) {
                    if (error instanceof RemoteRequestResponseException) {
                        RemoteRequestResponseException ex = (RemoteRequestResponseException) error;
                        if (ex.getCode() == 401) {
                            eventBus.post(new SessionExpiredEvent());
                            for (Replication replication : database.getAllReplications()) {
                                replication.stop();
                                replication.clearAuthenticationStores();
                            }
                            requestNewSession();
                        }
                    }
                }
            });

            pull.start();
            push.start();
        } catch (Exception e) {
            eventBus.post(new AppErrorEvent(e));
        }
    }

    private void requestNewSession() {
        Player player = getPlayer();
        AccessTokenListener listener = accessToken -> {
            if (StringUtils.isEmpty(accessToken)) {
                return;
            }
            api.createSession(player.getCurrentAuthProvider(), accessToken, new Api.SessionResponseListener() {
                @Override
                public void onSuccess(String username, String email, List<Cookie> cookies, String playerId, boolean isNew, boolean shouldCreatePlayer) {
                    eventBus.post(new NewSessionCreatedEvent());
                    syncData(cookies);
                }

                @Override
                public void onError(Exception e) {
                    eventBus.post(new AppErrorEvent(e));
                }
            });
        };
        AuthProvider.Provider authProvider = player.getCurrentAuthProvider().getProviderType();
        if (authProvider == AuthProvider.Provider.GOOGLE) {
            new GoogleAuthService(eventBus).getIdToken(this, listener::onAccessTokenReceived);
        } else if (authProvider == AuthProvider.Provider.FACEBOOK) {
            listener.onAccessTokenReceived(new FacebookAuthService(eventBus).getAccessToken());
        }
    }

    private void scheduleDailyChallenge() {
        Intent intent = new Intent(this, ScheduleDailyChallengeReminderReceiver.class);
        intent.setAction(ScheduleDailyChallengeReminderReceiver.ACTION_SCHEDULE_DAILY_CHALLENGE_REMINDER);
        sendBroadcast(intent);
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
    public void onPlayerSignedIn(PlayerSignedInEvent e) {
        localStorage.saveString(Constants.KEY_PLAYER_ID, e.playerId);
        playerId = e.playerId;

        playerAuthenticationStatus = e.playerAuthenticationStatus;
        cookies = e.cookies;
        if ((playerAuthenticationStatus == PlayerAuthenticationStatus.EXISTING
                || playerAuthenticationStatus == PlayerAuthenticationStatus.EXISTING_FROM_GUEST)
                && shouldMigratePlayer()) {
            startNewActivity(MigrationActivity.class);
            return;
        }

        initAppForPlayer(playerAuthenticationStatus, cookies);

        Bundle bundle = new Bundle();
        boolean isNewPlayer = playerAuthenticationStatus == PlayerAuthenticationStatus.NEW
                || playerAuthenticationStatus == PlayerAuthenticationStatus.GUEST;
        bundle.putBoolean(Constants.SHOW_TRIAL_MESSAGE_EXTRA_KEY,
                isNewPlayer);
        startNewActivity(MainActivity.class, bundle);
    }

    private void initAppForPlayer(PlayerAuthenticationStatus playerAuthenticationStatus, List<Cookie> cookies) {
        if (playerAuthenticationStatus == null) {
            initAppStart();
            initReplication();
            PowerUpScheduler.scheduleExpirationCheckJob(getApplicationContext());
            return;
        }

        if (playerAuthenticationStatus == PlayerAuthenticationStatus.NEW ||
                playerAuthenticationStatus == PlayerAuthenticationStatus.EXISTING ||
                playerAuthenticationStatus == PlayerAuthenticationStatus.GUEST) {
            initAppStart();
            PowerUpScheduler.scheduleExpirationCheckJob(getApplicationContext());
        }

        if (playerAuthenticationStatus != PlayerAuthenticationStatus.GUEST) {
            syncData(cookies);
        }

        if (playerAuthenticationStatus == PlayerAuthenticationStatus.NEW_FROM_GUEST ||
                playerAuthenticationStatus == PlayerAuthenticationStatus.EXISTING_FROM_GUEST) {
            Player player = getPlayer();
            if (player.hasUsername()) {
                listenForPlayerChanges();
            }
        }
    }

    @Subscribe
    public void onPlayerMigrated(PlayerMigratedEvent e) {
        initAppForPlayer(playerAuthenticationStatus, cookies);
        startNewActivity(MainActivity.class);
    }

    @Subscribe
    public void onShowQuestEvent(ShowQuestEvent e) {
        Intent i = new Intent(this, QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Subscribe
    public void onCompleteQuestRequest(CompleteQuestRequestEvent e) {
        Quest q = e.quest;
        q.increaseCompletedCount();
        if (q.completedAllTimesForDay()) {
            QuestNotificationScheduler.cancelAll(q, this);
            q.setScheduledDate(LocalDate.now());
            q.setCompletedAtDate(LocalDate.now());
            q.setCompletedAtMinute(Time.now().toMinuteOfDay());
            q.setExperience(experienceRewardGenerator.generate(q));
            q.setCoins(coinsRewardGenerator.generate(q));
            questPersistenceService.save(q);
            onQuestComplete(q, e.source);
        } else {
            questPersistenceService.save(q);
            Toast.makeText(this, R.string.quest_complete, Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onRemoveBaseQuestFromChallenge(RemoveBaseQuestFromChallengeEvent e) {
        BaseQuest bq = e.baseQuest;
        if (bq instanceof Quest) {
            Quest q = (Quest) bq;
            q.setChallengeId(null);
            questPersistenceService.save(q);
        } else {
            RepeatingQuest rq = (RepeatingQuest) bq;
            repeatingQuestPersistenceService.removeFromChallenge(rq);
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
            quest.setScheduledDate(null);
        }
        quest.setCompletedCount(0);
        Long xp = quest.getExperience();
        Long coins = quest.getCoins();
        quest.setExperience(null);
        quest.setCoins(null);
        questPersistenceService.save(quest);

        Player player = getPlayer();
        player.removeExperience(xp);
        if (shouldDecreaseLevel(player)) {
            player.setLevel(Math.max(Constants.DEFAULT_PLAYER_LEVEL, player.getLevel() - 1));
            while (shouldDecreaseLevel(player)) {
                player.setLevel(Math.max(Constants.DEFAULT_PLAYER_LEVEL, player.getLevel() - 1));
            }
            eventBus.post(new LevelDownEvent(player.getLevel()));
        }
        player.removeCoins(coins);
        updatePet(player.getPet(), (int) -Math.floor(xp / Constants.XP_TO_PET_HP_RATIO));
        playerPersistenceService.save(player);
        eventBus.post(new UndoCompletedQuestEvent(quest, xp, coins));
    }

    private Player getPlayer() {
        return playerPersistenceService.get();
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
        quest.setDuration(Math.max(quest.getDuration(), Constants.QUEST_MIN_DURATION));

        if (quest.getEnd() != null) {
            if (Objects.equals(quest.getEnd(), quest.getStart())) {
                quest.setScheduled(quest.getEnd());
            } else {
                Date scheduledDate = questScheduler.schedule(quest);
                quest.setScheduled(scheduledDate.getTime());
            }
            quest.setOriginalScheduled(quest.getScheduled());
        }

        if (quest.isScheduledForThePast()) {
            completeAtScheduledDate(quest);
        }

        if (quest.isCompleted()) {
            quest.setExperience(experienceRewardGenerator.generate(quest));
            quest.setCoins(coinsRewardGenerator.generate(quest));
        }
        questPersistenceService.save(quest);
        if (quest.isCompleted()) {
            onQuestComplete(quest, e.source);
        }
    }

    @Subscribe
    public void onUpdateQuest(UpdateQuestEvent e) {
        Quest quest = e.quest;
        if (quest.isScheduledForThePast() && !quest.isCompleted()) {
            completeAtScheduledDate(quest);
        }
        if (quest.isCompleted()) {
            quest.setExperience(experienceRewardGenerator.generate(quest));
            quest.setCoins(coinsRewardGenerator.generate(quest));
        }
        questPersistenceService.save(quest);
        if (quest.isCompleted()) {
            onQuestComplete(quest, e.source);
        }
    }

    private void completeAtScheduledDate(Quest quest) {
        int completedAtMinute = Time.now().toMinuteOfDay();
        if (quest.hasStartTime()) {
            completedAtMinute = quest.getStartMinute();
        }

        quest.setCompletedAt(quest.getScheduled());
        quest.setCompletedAtMinute(completedAtMinute);
        quest.increaseCompletedCount();
    }

    @Subscribe
    public void onUpdateRepeatingQuest(UpdateRepeatingQuestEvent e) {
        final RepeatingQuest repeatingQuest = e.repeatingQuest;
        LocalDate today = LocalDate.now();
        Recurrence.RepeatType repeatType = repeatingQuest.getRecurrence().getRecurrenceType();
        LocalDate periodStart;
        switch (repeatType) {
            case DAILY:
            case WEEKLY:
                periodStart = today.with(DayOfWeek.MONDAY);
                break;
            case MONTHLY:
                periodStart = today.withDayOfMonth(1);
                break;
            default:
                periodStart = today.withDayOfYear(1);
                break;
        }

        questPersistenceService.findAllUpcomingForRepeatingQuest(periodStart, repeatingQuest.getId(), questsSincePeriodStart -> {

            List<Quest> questsToRemove = new ArrayList<>();
            List<Quest> scheduledQuests = new ArrayList<>();

            for (Quest q : questsSincePeriodStart) {
                if (q.isCompleted() || q.getOriginalScheduledDate().isBefore(today)) {
                    scheduledQuests.add(q);
                } else {
                    questsToRemove.add(q);
                    QuestNotificationScheduler.cancelAll(q, this);
                }
            }

            long todayStartOfDay = DateUtils.toMillis(today);
            List<String> periodsToDelete = new ArrayList<>();
            for (String periodEnd : repeatingQuest.getScheduledPeriodEndDates().keySet()) {
                if (Long.valueOf(periodEnd) >= todayStartOfDay) {
                    periodsToDelete.add(periodEnd);
                }
            }
            repeatingQuest.getScheduledPeriodEndDates().keySet().removeAll(periodsToDelete);
            List<Quest> questsToCreate = repeatingQuestScheduler.schedule(repeatingQuest, today, scheduledQuests);
            repeatingQuestPersistenceService.update(repeatingQuest, questsToRemove, questsToCreate);
        });
    }

    @Subscribe
    public void onDeleteQuestRequest(DeleteQuestRequestEvent e) {
        QuestNotificationScheduler.cancelAll(e.quest, this);
        questPersistenceService.delete(e.quest);
        Toast.makeText(this, R.string.quest_deleted, Toast.LENGTH_SHORT).show();
    }

    private void onQuestComplete(Quest quest, EventSource source) {
        checkForDailyChallengeCompletion(quest);
        givePlayerRewardForAction(quest, new CompleteQuestAction(quest));
        savePet((int) (Math.ceil(quest.getExperience() / Constants.XP_TO_PET_HP_RATIO)));
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
        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
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
            givePlayerRewardForAction(dailyChallenge, new CompleteDailyChallengeAction(dailyChallenge));
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

    private void givePlayerRewardForAction(RewardProvider rewardProvider, AchievementAction action) {
        Player player = getPlayer();
        Long experience = rewardProvider.getExperience();
        player.addExperience(experience);
        increasePlayerLevelIfNeeded(player);
        player.addCoins(rewardProvider.getCoins());
        playerPersistenceService.save(player);
        checkForUnlockedAchievement(action);
    }

    private void increasePlayerLevelIfNeeded(Player player) {
        if (shouldIncreaseLevel(player)) {
            player.setLevel(player.getLevel() + 1);
            while (shouldIncreaseLevel(player)) {
                player.setLevel(player.getLevel() + 1);
            }
            eventBus.post(new LevelUpEvent(player.getLevel()));
        }
    }

    @Subscribe
    public void onLevelUp(LevelUpEvent e) {
        checkForUnlockedAchievement(new LevelUpAction(e.newLevel));
        Intent intent = new Intent(this, LevelUpActivity.class);
        intent.putExtra(LevelUpActivity.LEVEL, e.newLevel);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Subscribe
    public void onNewRepeatingQuest(NewRepeatingQuestEvent e) {
        RepeatingQuest repeatingQuest = e.repeatingQuest;
        repeatingQuest.setDuration(Math.max(repeatingQuest.getDuration(), Constants.QUEST_MIN_DURATION));
        List<Quest> quests = repeatingQuestScheduler.schedule(repeatingQuest, LocalDate.now());
        repeatingQuestPersistenceService.saveWithQuests(repeatingQuest, quests);
        checkForUnlockedAchievement(AchievementAction.Action.CREATE_REPEATING_QUEST);
    }

    private void checkForUnlockedAchievement(AchievementAction.Action action) {
        UnlockAchievementScheduler.scheduleFindUnlocked(getApplicationContext(), new SimpleAchievementAction(action));
    }

    private void checkForUnlockedAchievement(AchievementAction action) {
        UnlockAchievementScheduler.scheduleFindUnlocked(getApplicationContext(), action);
    }

    @Subscribe
    public void onDeleteRepeatingQuestRequest(final DeleteRepeatingQuestRequestEvent e) {
        repeatingQuestPersistenceService.delete(e.repeatingQuest);
    }

    private void scheduleRepeatingQuests(List<RepeatingQuest> repeatingQuests) {
        Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests = new HashMap<>();
        for (RepeatingQuest repeatingQuest : repeatingQuests) {
            List<Quest> quests = repeatingQuestScheduler.schedule(repeatingQuest, LocalDate.now());
            repeatingQuestToScheduledQuests.put(repeatingQuest, quests);
        }
        repeatingQuestPersistenceService.saveWithQuests(repeatingQuestToScheduledQuests);
    }

    private void scheduleNextReminder() {
        Intent intent = new Intent(this, ScheduleNextRemindersReceiver.class);
        intent.setAction(ScheduleNextRemindersReceiver.ACTION_SCHEDULE_REMINDERS);
        sendBroadcast(intent);
    }

    @Subscribe
    public void onDeleteChallengeRequest(DeleteChallengeRequestEvent e) {
        Challenge challenge = e.challenge;
        challengePersistenceService.delete(challenge, e.shouldDeleteQuests);
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
        givePlayerRewardForAction(challenge, new CompleteChallengeAction(challenge));
        savePet((int) (Math.floor(challenge.getExperience() / Constants.XP_TO_PET_HP_RATIO)));
        showChallengeCompleteDialog(getString(R.string.challenge_complete, challenge.getName()), challenge.getExperience(), challenge.getCoins());
        eventBus.post(new ChallengeCompletedEvent(challenge, source));
    }

    private void requestWidgetUpdate() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, AgendaWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_agenda_list);
    }

    @Subscribe
    public void onNewChallenge(NewChallengeEvent e) {
        checkForUnlockedAchievement(AchievementAction.Action.CREATE_CHALLENGE);
    }

    @Subscribe
    public void onUpdateChallenge(UpdateChallengeEvent e) {
        challengePersistenceService.save(e.challenge);
    }

    @Subscribe
    public void onRewardUsed(RewardUsedEvent e) {
        checkForUnlockedAchievement(AchievementAction.Action.USE_REWARD);
    }

    private void scheduleDateChanged() {
        Intent i = new Intent(this, DateChangedReceiver.class);
        i.setAction(DateChangedReceiver.ACTION_DATE_CHANGED);
        PendingIntent pendingIntent = IntentUtils.getBroadcastPendingIntent(this, i);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        long notificationTime = DateUtils.toStartOfDay(LocalDate.now().plusDays(1)).getTime() + 5000L;
        if (Build.VERSION.SDK_INT > 22) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }
    }

    @Subscribe
    public void onDateChanged(DateChangedEvent e) {
        questPersistenceService.findAllNonAllDayForDate(LocalDate.now().minusDays(1), quests -> {
            savePet(-getDecreasePercentage(quests));
            scheduleQuestsFor4WeeksAhead();
            clearIncompleteQuests();
            scheduleDateChanged();
            listenForChanges();
            eventBus.post(new CalendarDayChangedEvent(LocalDate.now(), CalendarDayChangedEvent.Source.DATE_CHANGE));
        });
    }

    private void clearIncompleteQuests() {
        questPersistenceService.findAllIncompleteNotFromRepeatingBefore(LocalDate.now(), quests -> {
            for (Quest q : quests) {
                if (q.isStarted()) {
                    q.setScheduledDate(LocalDate.now());
                    q.setStartMinute(0);
                } else {
                    q.setScheduledDate(null);
                }
                if (q.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
                    q.setPriority(null);
                }
            }
            questPersistenceService.save(quests);
        });
    }

    @Subscribe
    public void onAchievementsUnlocked(AchievementsUnlockedEvent e) {
        int xp = 0;
        for (Achievement achievement : e.achievements) {
            xp += achievement.experience;
        }
        checkForUnlockedAchievement(new AchievementsUnlockedAction(xp, getPlayer().getCoins()));
    }

    @Subscribe
    public void onPostAdded(PostAddedEvent e) {
        checkForUnlockedAchievement(AchievementAction.Action.ADD_POST);
    }

    @Subscribe
    public void onAvatarChanged(AvatarChangedEvent e) {
        checkForUnlockedAchievement(AchievementAction.Action.CHANGE_AVATAR);
    }

    @Subscribe
    public void onPetChanged(PetChangedEvent e) {
        checkForUnlockedAchievement(AchievementAction.Action.CHANGE_PET);
    }

    @Subscribe
    public void onPowerUpEnabled(PowerUpEnabledEvent e) {
        checkForUnlockedAchievement(AchievementAction.Action.BUY_POWER_UP);
    }

    @Subscribe
    public void onPlayerFollowed(PlayerFollowedEvent e) {
        checkForUnlockedAchievement(AchievementAction.Action.FOLLOW);
    }

    @Subscribe
    public void onInviteFriendProviderPicked(InviteFriendsProviderPickedEvent e) {
        checkForUnlockedAchievement(AchievementAction.Action.INVITE_FRIEND);
    }

    @Subscribe
    public void onFeedbackSent(RateDialogFeedbackSentEvent e) {
        checkForUnlockedAchievement(AchievementAction.Action.SEND_FEEDBACK);
    }

    @Subscribe
    public void onStartPowerUpDialogRequest(StartPowerUpDialogRequestEvent e) {
        Bundle bundle = new Bundle();
        bundle.putString(PowerUpDialogActivity.POWER_UP, e.powerUp.name());
        startNewActivity(PowerUpDialogActivity.class, bundle);
    }

    @Subscribe
    public void onScreenShown(ScreenShownEvent e) {
        if (e.activity != null) {
            FirebaseAnalytics.getInstance(this).setCurrentScreen(e.activity, e.source.name(), null);
        }
    }

    public static String getPlayerId() {
        return playerId;
    }

    public static boolean hasPlayer() {
        return !StringUtils.isEmpty(playerId);
    }

    public static List<PredefinedChallenge> getPredefinedChallenges(Context context) {
        List<PredefinedChallenge> challenges = new ArrayList<>();

        Challenge c = new Challenge(context.getString(R.string.challenge2_name));
        c.setCategoryType(Category.WELLNESS);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1(context.getString(R.string.challenge2_reason1));
        c.setReason2(context.getString(R.string.challenge2_reason2));
        c.setReason3(context.getString(R.string.challenge2_reason3));
        c.setExpectedResult1(context.getString(R.string.challenge2_exp_result1));
        c.setExpectedResult2(context.getString(R.string.challenge2_exp_result2));
        c.setExpectedResult3(context.getString(R.string.challenge2_exp_result3));
        challenges.add(new PredefinedChallenge(c, context.getString(R.string.challenge2_desc), R.drawable.challenge_02, R.drawable.challenge_expanded_02));

        c = new Challenge(context.getString(R.string.challenge1_name));
        c.setCategoryType(Category.WELLNESS);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1(context.getString(R.string.challenge1_reason1));
        c.setReason2(context.getString(R.string.challenge1_reason2));
        c.setReason3(context.getString(R.string.challenge1_reason3));
        c.setExpectedResult1(context.getString(R.string.challenge1_exp_result1));
        c.setExpectedResult2(context.getString(R.string.challenge1_exp_result2));
        c.setExpectedResult3(context.getString(R.string.challenge1_exp_result3));
        challenges.add(new PredefinedChallenge(c, context.getString(R.string.challenge1_desc), R.drawable.challenge_01, R.drawable.challenge_expanded_01));


        c = new Challenge(context.getString(R.string.challenge3_name));
        c.setCategoryType(Category.WELLNESS);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1(context.getString(R.string.challenge3_reason1));
        c.setReason2(context.getString(R.string.challenge3_reason2));
        c.setReason3(context.getString(R.string.challenge3_reason3));
        c.setExpectedResult1(context.getString(R.string.challenge3_exp_result1));
        c.setExpectedResult2(context.getString(R.string.challenge3_exp_result2));
        c.setExpectedResult3(context.getString(R.string.challenge3_exp_result3));
        challenges.add(new PredefinedChallenge(c, context.getString(R.string.challenge3_desc), R.drawable.challenge_03, R.drawable.challenge_expanded_03));

        c = new Challenge(context.getString(R.string.challenge4_name));
        c.setCategoryType(Category.LEARNING);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1(context.getString(R.string.challenge4_reason1));
        c.setReason2(context.getString(R.string.challenge4_reason2));
        c.setReason3(context.getString(R.string.challenge4_reason3));
        c.setExpectedResult1(context.getString(R.string.challenge4_exp_result1));
        c.setExpectedResult2(context.getString(R.string.challenge4_exp_result2));
        c.setExpectedResult3(context.getString(R.string.challenge4_exp_result3));
        challenges.add(new PredefinedChallenge(c, context.getString(R.string.challenge4_desc), R.drawable.challenge_04, R.drawable.challenge_expanded_04));

        c = new Challenge(context.getString(R.string.challenge5_name));
        c.setCategoryType(Category.LEARNING);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1(context.getString(R.string.challenge5_reason1));
        c.setReason2(context.getString(R.string.challenge5_reason2));
        c.setReason3(context.getString(R.string.challenge5_reason3));
        c.setExpectedResult1(context.getString(R.string.challenge5_exp_result1));
        c.setExpectedResult2(context.getString(R.string.challenge5_exp_result2));
        c.setExpectedResult3(context.getString(R.string.challenge5_exp_result3));
        challenges.add(new PredefinedChallenge(c, context.getString(R.string.challenge5_desc), R.drawable.challenge_05, R.drawable.challenge_expanded_05));

        c = new Challenge(context.getString(R.string.challenge6_name));
        c.setCategoryType(Category.WORK);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1(context.getString(R.string.challenge6_reason1));
        c.setReason2(context.getString(R.string.challenge6_reason2));
        c.setReason3(context.getString(R.string.challenge6_reason3));
        c.setExpectedResult1(context.getString(R.string.challenge6_exp_result1));
        c.setExpectedResult2(context.getString(R.string.challenge6_exp_result2));
        c.setExpectedResult3(context.getString(R.string.challenge6_exp_result3));
        challenges.add(new PredefinedChallenge(c, context.getString(R.string.challenge6_desc), R.drawable.challenge_06, R.drawable.challenge_expanded_06));

        c = new Challenge(context.getString(R.string.challenge7_name));
        c.setCategoryType(Category.WORK);
        c.setDifficultyType(Difficulty.HARD);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1(context.getString(R.string.challenge7_reason1));
        c.setReason2(context.getString(R.string.challenge7_reason2));
        c.setReason3(context.getString(R.string.challenge7_reason3));
        c.setExpectedResult1(context.getString(R.string.challenge7_exp_result1));
        c.setExpectedResult2(context.getString(R.string.challenge7_exp_result2));
        c.setExpectedResult3(context.getString(R.string.challenge7_exp_result3));
        challenges.add(new PredefinedChallenge(c, context.getString(R.string.challenge7_desc), R.drawable.challenge_07, R.drawable.challenge_expanded_07));

        c = new Challenge(context.getString(R.string.challenge8_name));
        c.setCategoryType(Category.PERSONAL);
        c.setDifficultyType(Difficulty.NORMAL);
        c.setEndDate(LocalDate.now().plusWeeks(2));
        c.setReason1(context.getString(R.string.challenge8_reason1));
        c.setReason2(context.getString(R.string.challenge8_reason2));
        c.setReason3(context.getString(R.string.challenge8_reason3));
        c.setExpectedResult1(context.getString(R.string.challenge8_exp_result1));
        c.setExpectedResult2(context.getString(R.string.challenge8_exp_result2));
        c.setExpectedResult3(context.getString(R.string.challenge8_exp_result3));
        challenges.add(new PredefinedChallenge(c, context.getString(R.string.challenge8_desc), R.drawable.challenge_08, R.drawable.challenge_expanded_08));

        return challenges;
    }

    interface AccessTokenListener {
        void onAccessTokenReceived(String accessToken);
    }
}