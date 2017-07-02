package io.ipoli.android;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.threeten.bp.LocalDate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.activities.MigrationActivity;
import io.ipoli.android.app.activities.SignInActivity;
import io.ipoli.android.app.events.AvatarCoinsTappedEvent;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.events.ContactUsTapEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FeedbackTapEvent;
import io.ipoli.android.app.events.FirebaseInviteCanceledEvent;
import io.ipoli.android.app.events.FirebaseInviteSentEvent;
import io.ipoli.android.app.events.InviteFriendsEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.rate.RateDialog;
import io.ipoli.android.app.rate.RateDialogConstants;
import io.ipoli.android.app.settings.SettingsActivity;
import io.ipoli.android.app.share.InviteFriendsDialog;
import io.ipoli.android.app.sync.AndroidCalendarSyncJobService;
import io.ipoli.android.app.ui.dialogs.DatePickerFragment;
import io.ipoli.android.app.ui.dialogs.TimePickerFragment;
import io.ipoli.android.app.ui.events.StartFabMenuIntentEvent;
import io.ipoli.android.app.utils.EmailUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.fragments.ChallengeListFragment;
import io.ipoli.android.feed.activities.AddPostActivity;
import io.ipoli.android.feed.fragments.FeedFragment;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.pet.PetActivity;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.PlayerCredentialChecker;
import io.ipoli.android.player.PlayerCredentialsHandler;
import io.ipoli.android.player.UpgradeDialog;
import io.ipoli.android.player.UpgradeDialog.OnUnlockListener;
import io.ipoli.android.player.UpgradeManager;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.OpenAvatarStoreRequestEvent;
import io.ipoli.android.player.fragments.GrowthFragment;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.commands.StartQuestCommand;
import io.ipoli.android.quest.commands.StopQuestCommand;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.DuplicateQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ShareQuestEvent;
import io.ipoli.android.quest.events.SnoozeQuestRequestEvent;
import io.ipoli.android.quest.events.StartQuestRequestEvent;
import io.ipoli.android.quest.events.StopQuestRequestEvent;
import io.ipoli.android.quest.fragments.CalendarFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.fragments.RepeatingQuestListFragment;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.events.EditRepeatingQuestRequestEvent;
import io.ipoli.android.reminder.data.Reminder;
import io.ipoli.android.reward.fragments.RewardListFragment;
import io.ipoli.android.store.StoreItemType;
import io.ipoli.android.store.Upgrade;
import io.ipoli.android.store.activities.StoreActivity;
import pub.devrel.easypermissions.EasyPermissions;

import static io.ipoli.android.Constants.RC_CALENDAR_PERM;
import static io.ipoli.android.Constants.SYNC_CALENDAR_JOB_ID;
import static io.ipoli.android.R.id.feed;
import static io.ipoli.android.app.App.hasPlayer;

public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnDataChangedListener<Player>,
        EasyPermissions.PermissionCallbacks {

    public static final int INVITE_FRIEND_REQUEST_CODE = 102;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.content_container)
    View contentContainer;

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    FeedPersistenceService feedPersistenceService;

    @Inject
    UpgradeManager upgradeManager;

    @Inject
    PlayerCredentialsHandler playerCredentialsHandler;

    private boolean isRateDialogShown;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private MenuItem navigationItemSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);

        if (!hasPlayer()) {
            finish();
            return;
        }

        if (shouldMigratePlayer()) {
            startActivity(new Intent(this, MigrationActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        localStorage.increment(Constants.KEY_APP_RUN_COUNT);

        isRateDialogShown = false;

        navigationView.setNavigationItemSelectedListener(this);

        startCalendar();

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                navigationItemSelected = null;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (navigationItemSelected == null) {
                    return;
                }
                onItemSelectedFromDrawer();
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        if (!getPlayer().getAndroidCalendars().isEmpty() &&
                !EasyPermissions.hasPermissions(this, Manifest.permission.READ_CALENDAR)) {
            EasyPermissions.requestPermissions(this, getString(R.string.allow_read_calendars_perm_reason_disable_option), RC_CALENDAR_PERM, Manifest.permission.READ_CALENDAR);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int i, List<String> list) {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(SYNC_CALENDAR_JOB_ID);
        JobInfo jobInfo = new JobInfo.Builder(SYNC_CALENDAR_JOB_ID,
                new ComponentName(this, AndroidCalendarSyncJobService.class))
                .setOverrideDeadline(0)
                .build();
        jobScheduler.schedule(jobInfo);
    }

    @Override
    public void onPermissionsDenied(int i, List<String> list) {
        //intentional
    }

    private boolean shouldMigratePlayer() {
        int firebaseSchemaVersion = localStorage.readInt(Constants.KEY_SCHEMA_VERSION);
        if (firebaseSchemaVersion > 0 && firebaseSchemaVersion <= Constants.FIREBASE_LAST_SCHEMA_VERSION) {
            return true;
        }
        if (getPlayer().getSchemaVersion() != Constants.SCHEMA_VERSION) {
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (shouldMigratePlayer()) {
            return;
        }
        playerPersistenceService.listen(this);
    }

    @Override
    protected void onStop() {
        playerPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Override
    public void onDataChanged(Player player) {
        updatePlayerInDrawer(player);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    private void onItemSelectedFromDrawer() {

        EventSource source = null;
        switch (navigationItemSelected.getItemId()) {

            case R.id.home:
                source = EventSource.CALENDAR;
                startCalendar();
                break;

            case R.id.overview:
                source = EventSource.OVERVIEW;
                startOverview();
                break;

            case R.id.inbox:
                source = EventSource.INBOX;
                changeCurrentFragment(new InboxFragment());
                break;

            case R.id.repeating_quests:
                if (upgradeManager.isLocked(Upgrade.REPEATING_QUESTS)) {
                    showUpgradeDialog(Upgrade.REPEATING_QUESTS, new RepeatingQuestListFragment());
                    return;
                }
                source = EventSource.REPEATING_QUESTS;
                changeCurrentFragment(new RepeatingQuestListFragment());
                break;

            case R.id.challenges:
                if (upgradeManager.isLocked(Upgrade.CHALLENGES)) {
                    showUpgradeDialog(Upgrade.CHALLENGES, new ChallengeListFragment());
                    return;
                }
                source = EventSource.CHALLENGES;
                changeCurrentFragment(new ChallengeListFragment());
                break;

            case R.id.growth:
                if (upgradeManager.isLocked(Upgrade.GROWTH)) {
                    showUpgradeDialog(Upgrade.GROWTH, new GrowthFragment());
                    return;
                }
                source = EventSource.GROWTH;
                changeCurrentFragment(new GrowthFragment());
                break;

            case R.id.rewards:
                source = EventSource.REWARDS;
                changeCurrentFragment(new RewardListFragment());
                break;

            case feed:
                source = EventSource.FEED;
                changeCurrentFragment(new FeedFragment());
                break;

            case R.id.store:
                source = EventSource.STORE;
                startActivity(new Intent(this, StoreActivity.class));
                break;

            case R.id.invite_friends:
                source = EventSource.INVITE_FRIENDS;
                inviteFriends();
                break;

            case R.id.settings:
                source = EventSource.SETTINGS;
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.feedback:
                eventBus.post(new FeedbackTapEvent());
                source = EventSource.FEEDBACK;
                RateDialog.newInstance(RateDialog.State.FEEDBACK).show(getSupportFragmentManager());
                break;

            case R.id.contact_us:
                eventBus.post(new ContactUsTapEvent());
                source = EventSource.CONTACT_US;
                EmailUtils.send(MainActivity.this, "Hi", localStorage.readString(Constants.KEY_PLAYER_ID), getString(R.string.contact_us_email_chooser_title));
                break;
        }

        navigationView.setCheckedItem(navigationItemSelected.getItemId());
        if (source != null) {
            eventBus.post(new ScreenShownEvent(this, source));
        }
    }

    private void showUpgradeDialog(Upgrade upgrade, Fragment fragment) {
        UpgradeDialog.newInstance(upgrade, new OnUnlockListener() {
            @Override
            public void onUnlock() {
                changeCurrentFragment(fragment);
                navigationView.setCheckedItem(navigationItemSelected.getItemId());
            }
        }).show(getSupportFragmentManager());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePlayerInDrawer(Player player) {

        View header = navigationView.getHeaderView(0);
        TextView level = (TextView) header.findViewById(R.id.player_level);
        int playerLevel = player.getLevel();
        String title = player.getTitle(getResources().getStringArray(R.array.player_titles));
        level.setText(String.format(getString(R.string.player_level), playerLevel, title));

        populateHeaderCoins(player, header);
        populateHeaderPoints(player, header);
        populateHeaderXP(player, header);

        ProgressBar experienceBar = (ProgressBar) header.findViewById(R.id.player_experience);
        experienceBar.setMax(Constants.XP_BAR_MAX_VALUE);
        experienceBar.setProgress(getCurrentProgress(player));

        CircleImageView avatarPictureView = (CircleImageView) header.findViewById(R.id.player_avatar);
        avatarPictureView.setImageResource(player.getCurrentAvatar().picture);
        avatarPictureView.setOnClickListener(v -> {
            eventBus.post(new OpenAvatarStoreRequestEvent(EventSource.NAVIGATION_DRAWER));
            Intent intent = new Intent(this, StoreActivity.class);
            intent.putExtra(StoreActivity.START_ITEM_TYPE, StoreItemType.AVATARS.name());
            startActivity(intent);
        });

        updatePetInDrawer(player.getPet());

        Button signIn = (Button) header.findViewById(R.id.sign_in);
        if (player.isAuthenticated()) {
            signIn.setVisibility(View.GONE);
            signIn.setOnClickListener(null);
        } else {
            signIn.setVisibility(View.VISIBLE);
            signIn.setOnClickListener(v -> startActivity(new Intent(this, SignInActivity.class)));
        }
    }

    private void populateHeaderCoins(Player player, View header) {
        TextView coins = (TextView) header.findViewById(R.id.player_coins);
        coins.setText(formatValue(player.getCoins()));
        coins.setOnClickListener(view -> {
            Intent intent = new Intent(this, StoreActivity.class);
            intent.putExtra(StoreActivity.START_ITEM_TYPE, StoreItemType.COINS.name());
            startActivity(intent);
            eventBus.post(new AvatarCoinsTappedEvent());
        });
    }

    private void populateHeaderPoints(Player player, View header) {
        TextView rewardPoints = (TextView) header.findViewById(R.id.player_reward_points);
        rewardPoints.setText(formatValue(player.getRewardPoints()));
        rewardPoints.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, R.string.reward_points_description, Toast.LENGTH_LONG).show());
    }

    private void populateHeaderXP(Player player, View header) {
        TextView currentXP = (TextView) header.findViewById(R.id.player_current_xp);
        currentXP.setText(getString(R.string.nav_drawer_player_xp, formatValue(Long.valueOf(player.getExperience()))));
    }

    private String formatValue(Long value) {
        String valString = String.valueOf(value);
        if (value < 1000) {
            return valString;
        }
        String main = valString.substring(0, valString.length() - 3);
        String result = main;
        char tail = valString.charAt(valString.length() - 3);
        if (tail != '0') {
            result += "." + tail;
        }
        return getString(R.string.big_value_format, result);
    }

    private void updatePetInDrawer(Pet pet) {
        View header = navigationView.getHeaderView(0);

        CircleImageView petPictureView = (CircleImageView) header.findViewById(R.id.pet_picture);
        petPictureView.setImageResource(pet.getCurrentAvatar().headPicture);
        petPictureView.setOnClickListener(v -> startActivity(new Intent(this, PetActivity.class)));

        ImageView petStateView = (ImageView) header.findViewById(R.id.pet_state);
        GradientDrawable drawable = (GradientDrawable) petStateView.getBackground();
        drawable.setColor(ContextCompat.getColor(this, pet.getStateColor()));
    }

    private int getCurrentProgress(Player player) {
        int currentLevel = player.getLevel();
        BigInteger requiredXPForCurrentLevel = ExperienceForLevelGenerator.forLevel(currentLevel);
        BigDecimal xpForNextLevel = new BigDecimal(ExperienceForLevelGenerator.forLevel(currentLevel + 1).subtract(requiredXPForCurrentLevel));
        BigDecimal currentXP = new BigDecimal(new BigInteger(player.getExperience()).subtract(requiredXPForCurrentLevel));
        return (int) (currentXP.divide(xpForNextLevel, 2, RoundingMode.HALF_UP).doubleValue() * Constants.XP_BAR_MAX_VALUE);
    }

    public void startCalendar() {
        changeCurrentFragment(new CalendarFragment());
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    private void changeCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container, fragment).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        Quest q = e.quest;
        long experience = q.getExperience();
        long coins = q.getCoins();

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.root_container),
                        getString(R.string.quest_complete_with_bounty, experience, coins),
                        Snackbar.LENGTH_LONG);

        snackbar.setAction(R.string.share, view -> {
            eventBus.post(new ShareQuestEvent(q, EventSource.SNACKBAR));
        });

        snackbar.show();

        if (shouldShowRateDialog()) {
            isRateDialogShown = true;
            new RateDialog().show(getSupportFragmentManager());
        }
    }

    @Subscribe
    public void onShareQuest(ShareQuestEvent e) {
        Player player = getPlayer();

        PlayerCredentialChecker.Status status = PlayerCredentialChecker.checkStatus(player);
        if (status != PlayerCredentialChecker.Status.AUTHORIZED) {
            playerCredentialsHandler.authorizeAccess(player, status, PlayerCredentialsHandler.Action.SHARE_QUEST,
                    this, findViewById(R.id.root_container));
            return;
        }

        Intent addPostIntent = new Intent(this, AddPostActivity.class);
        addPostIntent.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivity(addPostIntent);
    }

    private boolean shouldShowRateDialog() {
        int appRun = localStorage.readInt(Constants.KEY_APP_RUN_COUNT);
        if (isRateDialogShown || appRun < RateDialogConstants.MIN_APP_RUN_FOR_RATE_DIALOG ||
                !localStorage.readBool(RateDialogConstants.KEY_SHOULD_SHOW_RATE_DIALOG, true)) {
            return false;
        }
        return new Random().nextBoolean();
    }

    @Subscribe
    public void onUndoCompletedQuest(UndoCompletedQuestEvent e) {
        Quest q = e.quest;
        String text = getString(q.getScheduledDate() == null ? R.string.quest_undone_to_inbox : R.string.quest_undone, e.experience, e.coins);
        Snackbar.make(contentContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    public void initToolbar(Toolbar toolbar, @StringRes int title) {
        setSupportActionBar(toolbar);
        toolbar.setTitle(title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        actionBarDrawerToggle.syncState();
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        Intent i = new Intent(this, EditQuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.questId);
        startActivity(i);
    }

    @Subscribe
    public void onEditRepeatingQuestRequest(EditRepeatingQuestRequestEvent e) {
        Intent i = new Intent(this, EditQuestActivity.class);
        i.putExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY, e.repeatingQuest.getId());
        startActivity(i);
    }

    @Subscribe
    public void onDuplicateQuestRequest(DuplicateQuestRequestEvent e) {
        boolean showAction = e.source != EventSource.OVERVIEW;
        if (e.date == null) {
            DatePickerFragment fragment = DatePickerFragment.newInstance(LocalDate.now(), true,
                    date -> duplicateQuest(e.quest, date, showAction));
            fragment.show(getSupportFragmentManager());
        } else {
            duplicateQuest(e.quest, e.date, showAction);
        }
    }

    private void duplicateQuest(Quest quest, LocalDate scheduledDate, boolean showAction) {
        boolean isForSameDay = quest.getScheduledDate().isEqual(scheduledDate);
        quest.setId(null);
        quest.setCreatedAt(new Date().getTime());
        quest.setUpdatedAt(new Date().getTime());
        quest.setActualStartDate(null);
        quest.setStartDate(scheduledDate);
        quest.setEndDate(scheduledDate);
        quest.setScheduledDate(scheduledDate);
        quest.setCompletedAtMinute(null);
        quest.setCompletedAtDate(null);
        quest.setCompletedCount(0);
        if (isForSameDay) {
            quest.setStartMinute(null);
        }
        List<Reminder> reminders = quest.getReminders();
        List<Reminder> newReminders = new ArrayList<>();
        int notificationId = new Random().nextInt();
        for (Reminder r : reminders) {
            newReminders.add(new Reminder(r.getMinutesFromStart(), String.valueOf(notificationId)));
        }
        quest.setReminders(newReminders);
        eventBus.post(new NewQuestEvent(quest, EventSource.CALENDAR));

        Snackbar snackbar = Snackbar.make(contentContainer, R.string.quest_duplicated, Snackbar.LENGTH_LONG);

        if (!isForSameDay && showAction) {
            snackbar.setAction(R.string.view, view -> {
                Time scrollToTime = null;
                if (quest.getStartMinute() != null) {
                    scrollToTime = Time.of(quest.getStartMinute());
                }
                eventBus.post(new CalendarDayChangedEvent(scheduledDate, scrollToTime, CalendarDayChangedEvent.Source.DUPLICATE_QUEST_SNACKBAR));
            });
        }

        snackbar.show();
    }

    @Subscribe
    public void onSnoozeQuestRequest(SnoozeQuestRequestEvent e) {
        boolean showAction = e.source != EventSource.OVERVIEW;
        Quest quest = e.quest;
        if (e.showDatePicker) {
            pickDateAndSnoozeQuest(quest, showAction);
        } else if (e.showTimePicker) {
            pickTimeAndSnoozeQuest(quest, showAction);
        } else {
            boolean isDateChanged = false;
            if (e.minutes > 0) {
                int newMinutes = quest.getStartMinute() + e.minutes;
                if (newMinutes >= Time.MINUTES_IN_A_DAY) {
                    newMinutes = newMinutes % Time.MINUTES_IN_A_DAY;
                    quest.setScheduledDate(quest.getScheduledDate().plusDays(1));
                    isDateChanged = true;
                }
                quest.setStartMinute(newMinutes);

            } else {
                isDateChanged = true;
                quest.setScheduledDate(e.date);
            }
            saveSnoozedQuest(quest, isDateChanged, showAction);
        }
    }

    private void pickTimeAndSnoozeQuest(Quest quest, boolean showAction) {
        Time time = quest.hasStartTime() ? Time.of(quest.getStartMinute()) : null;
        TimePickerFragment.newInstance(false, time, newTime -> {
            quest.setStartMinute(newTime.toMinuteOfDay());
            saveSnoozedQuest(quest, false, showAction);
        }).show(getSupportFragmentManager());
    }

    private void pickDateAndSnoozeQuest(Quest quest, boolean showAction) {
        DatePickerFragment.newInstance(LocalDate.now(), true, date -> {
            quest.setScheduledDate(date);
            saveSnoozedQuest(quest, true, showAction);
        }).show(getSupportFragmentManager());
    }

    private void saveSnoozedQuest(Quest quest, boolean isDateChanged, boolean showAction) {
        questPersistenceService.save(quest);
        String message = getString(R.string.quest_snoozed);
        if (quest.getScheduledDate() == null) {
            message = getString(R.string.quest_moved_to_inbox);
        }

        Snackbar snackbar = Snackbar.make(contentContainer, message, Snackbar.LENGTH_LONG);

        if (isDateChanged && showAction) {
            snackbar.setAction(R.string.view, view -> {
                if (quest.getScheduledDate() == null) {
                    changeCurrentFragment(new InboxFragment());
                } else {
                    Time scrollToTime = null;
                    if (quest.getStartMinute() != null) {
                        scrollToTime = Time.of(quest.getStartMinute());
                    }
                    eventBus.post(new CalendarDayChangedEvent(quest.getScheduledDate(), scrollToTime, CalendarDayChangedEvent.Source.SNOOZE_QUEST_SNACKBAR));
                }
            });
        }

        snackbar.show();
    }

    @Subscribe
    public void onLevelDown(LevelDownEvent e) {
        showLevelDownMessage(e.newLevel);
    }

    @Subscribe
    public void onStartQuestRequest(StartQuestRequestEvent e) {
        if (upgradeManager.isLocked(Upgrade.TIMER)) {
            UpgradeDialog.newInstance(Upgrade.TIMER).show(getSupportFragmentManager());
            return;
        }

        new StartQuestCommand(this, e.quest, questPersistenceService).execute();
    }

    @Subscribe
    public void onStopQuestRequest(StopQuestRequestEvent e) {
        new StopQuestCommand(this, e.quest, questPersistenceService).execute();
    }

    @Subscribe
    public void onStartFabMenuIntent(StartFabMenuIntentEvent e) {
        switch (e.fabName) {
            case REPEATING_QUEST:
                if (upgradeManager.isLocked(Upgrade.REPEATING_QUESTS)) {
                    showUpgradeDialogForFabItem(Upgrade.REPEATING_QUESTS, e.intent);
                    return;
                }
                startActivity(e.intent);
                return;
            case CHALLENGE:
                if (upgradeManager.isLocked(Upgrade.CHALLENGES)) {
                    showUpgradeDialogForFabItem(Upgrade.CHALLENGES, e.intent);
                    return;
                }
                startActivity(e.intent);
                return;
            default:
                startActivity(e.intent);
        }
    }

    private void showUpgradeDialogForFabItem(Upgrade upgrade, Intent intent) {
        UpgradeDialog.newInstance(upgrade, new OnUnlockListener() {
            @Override
            public void onUnlock() {
                startActivity(intent);
            }
        }).show(getSupportFragmentManager());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navigationItemSelected = item;
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void inviteFriends() {
        eventBus.post(new InviteFriendsEvent());
        InviteFriendsDialog inviteFriendsDialog = new InviteFriendsDialog();
        inviteFriendsDialog.show(getSupportFragmentManager());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INVITE_FRIEND_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String[] inviteIds = AppInviteInvitation.getInvitationIds(resultCode, data);
                if (inviteIds == null) {
                    inviteIds = new String[]{};
                }
                eventBus.post(new FirebaseInviteSentEvent(inviteIds));
            } else {
                eventBus.post(new FirebaseInviteCanceledEvent());
            }
        }
    }

    public void startOverview() {
        changeCurrentFragment(new OverviewFragment());
    }
}