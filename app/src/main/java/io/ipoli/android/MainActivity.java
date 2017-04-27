package io.ipoli.android;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.activities.MigrationActivity;
import io.ipoli.android.app.activities.SignInActivity;
import io.ipoli.android.app.events.AvatarCoinsTappedEvent;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.events.ContactUsTapEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FeedbackTapEvent;
import io.ipoli.android.app.events.FriendsInvitedEvent;
import io.ipoli.android.app.events.InviteFriendsCanceledEvent;
import io.ipoli.android.app.events.InviteFriendsEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.rate.RateDialog;
import io.ipoli.android.app.rate.RateDialogConstants;
import io.ipoli.android.app.settings.SettingsActivity;
import io.ipoli.android.app.share.ShareQuestDialog;
import io.ipoli.android.app.ui.dialogs.DatePickerFragment;
import io.ipoli.android.app.ui.dialogs.TimePickerFragment;
import io.ipoli.android.app.utils.EmailUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.fragments.ChallengeListFragment;
import io.ipoli.android.pet.PetActivity;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.activities.LevelUpActivity;
import io.ipoli.android.player.activities.PickAvatarPictureActivity;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.PickAvatarRequestEvent;
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
import io.ipoli.android.shop.activities.CoinStoreActivity;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final int PICK_PLAYER_PICTURE_REQUEST_CODE = 101;
    public static final int INVITE_FRIEND_REQUEST_CODE = 102;
    private static final int PROGRESS_BAR_MAX_VALUE = 100;

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

    Fragment currentFragment;

    private boolean isRateDialogShown;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private MenuItem navigationItemSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);

        if (!App.hasPlayer()) {
            finish();
            return;
        }

        int schemaVersion = localStorage.readInt(Constants.KEY_SCHEMA_VERSION);
        if (App.hasPlayer() && schemaVersion != Constants.SCHEMA_VERSION) {
            // should migrate
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
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        updatePlayerInDrawer(getPlayer());
    }

    private void onItemSelectedFromDrawer() {
        navigationView.setCheckedItem(navigationItemSelected.getItemId());

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
                source = EventSource.REPEATING_QUESTS;
                changeCurrentFragment(new RepeatingQuestListFragment());
                break;

            case R.id.challenges:
                source = EventSource.CHALLENGES;
                changeCurrentFragment(new ChallengeListFragment());
                break;

            case R.id.growth:
                source = EventSource.GROWTH;
                changeCurrentFragment(new GrowthFragment());
                break;

            case R.id.rewards:
                source = EventSource.REWARDS;
                changeCurrentFragment(new RewardListFragment());
                break;

            case R.id.store:
                source = EventSource.STORE;
                startActivity(new Intent(this, CoinStoreActivity.class));
                break;

            case R.id.invite_friends:
                inviteFriends();
                break;

            case R.id.settings:
                source = EventSource.SETTINGS;
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.feedback:
                eventBus.post(new FeedbackTapEvent());
                RateDialog.newInstance(RateDialog.State.FEEDBACK).show(getSupportFragmentManager());
                break;

            case R.id.contact_us:
                eventBus.post(new ContactUsTapEvent());
                EmailUtils.send(MainActivity.this, "Hi", localStorage.readString(Constants.KEY_PLAYER_ID), getString(R.string.contact_us_email_chooser_title));
                break;
        }

        if (source != null) {
            eventBus.post(new ScreenShownEvent(source));
        }
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
        String[] playerTitles = getResources().getStringArray(R.array.player_titles);
        String title = playerTitles[Math.min(playerLevel / 10, playerTitles.length - 1)];
        level.setText("Level " + playerLevel + ": " + title);

        TextView coins = (TextView) header.findViewById(R.id.player_coins);
        coins.setText(String.valueOf(player.getCoins()));
        coins.setOnClickListener(view -> {
            startActivity(new Intent(this, CoinStoreActivity.class));
            eventBus.post(new AvatarCoinsTappedEvent());
        });

        ProgressBar experienceBar = (ProgressBar) header.findViewById(R.id.player_experience);
        experienceBar.setMax(PROGRESS_BAR_MAX_VALUE);
        experienceBar.setProgress(getCurrentProgress(player));

        CircleImageView avatarPictureView = (CircleImageView) header.findViewById(R.id.player_picture);
        avatarPictureView.setImageResource(ResourceUtils.extractDrawableResource(MainActivity.this, player.getPicture()));
        avatarPictureView.setOnClickListener(v -> eventBus.post(new PickAvatarRequestEvent(EventSource.NAVIGATION_DRAWER)));

        TextView currentXP = (TextView) header.findViewById(R.id.player_current_xp);
        currentXP.setText(String.format(getString(R.string.nav_drawer_player_xp), player.getExperience()));
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

    private void updatePetInDrawer(Pet pet) {
        View header = navigationView.getHeaderView(0);

        CircleImageView petPictureView = (CircleImageView) header.findViewById(R.id.pet_picture);
        petPictureView.setImageResource(ResourceUtils.extractDrawableResource(MainActivity.this, pet.getPicture() + "_head"));
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
        return (int) (currentXP.divide(xpForNextLevel, 2, RoundingMode.HALF_UP).doubleValue() * PROGRESS_BAR_MAX_VALUE);
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
        currentFragment = fragment;
        getSupportFragmentManager().executePendingTransactions();
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        Quest q = e.quest;
        long experience = q.getExperience();
        long coins = q.getCoins();

        Snackbar snackbar = Snackbar
                .make(contentContainer,
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
    public void onShareQuest(ShareQuestEvent e) {
        ShareQuestDialog.show(this, e.quest, eventBus);
    }

    @Subscribe
    public void onLevelDown(LevelDownEvent e) {
        showLevelDownMessage(e.newLevel);
    }

    @Subscribe
    public void onStartQuestRequest(StartQuestRequestEvent e) {
        new StartQuestCommand(this, e.quest, questPersistenceService).execute();
    }

    @Subscribe
    public void onStopQuestRequest(StopQuestRequestEvent e) {
        new StopQuestCommand(this, e.quest, questPersistenceService).execute();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navigationItemSelected = item;
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void inviteFriends() {
        eventBus.post(new InviteFriendsEvent());
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invite_title))
                .setMessage(getString(R.string.invite_message))
                .setCustomImage(Uri.parse(Constants.INVITE_IMAGE_URL))
                .setCallToActionText(getString(R.string.invite_call_to_action))
                .build();
        startActivityForResult(intent, INVITE_FRIEND_REQUEST_CODE);
    }

    @Subscribe
    public void onPickAvatarRequest(PickAvatarRequestEvent e) {
        startActivityForResult(new Intent(MainActivity.this, PickAvatarPictureActivity.class), PICK_PLAYER_PICTURE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PLAYER_PICTURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String picture = data.getStringExtra(Constants.PICTURE_NAME_EXTRA_KEY);
            if (!TextUtils.isEmpty(picture)) {
                Player player = getPlayer();
                ImageView avatarImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.player_picture);
                avatarImage.setImageResource(ResourceUtils.extractDrawableResource(this, picture));
                player.setPicture(picture);
                playerPersistenceService.save(player);
            }
        }

        if (requestCode == INVITE_FRIEND_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String[] inviteIds = AppInviteInvitation.getInvitationIds(resultCode, data);
                if (inviteIds == null) {
                    inviteIds = new String[]{};
                }
                eventBus.post(new FriendsInvitedEvent(inviteIds));
            } else {
                eventBus.post(new InviteFriendsCanceledEvent());
            }
        }
    }

    public void startOverview() {
        changeCurrentFragment(new OverviewFragment());
    }
}