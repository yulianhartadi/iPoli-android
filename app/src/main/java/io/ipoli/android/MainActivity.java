package io.ipoli.android;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.LocalDate;

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
import io.ipoli.android.app.events.ContactUsTapEvent;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FeedbackTapEvent;
import io.ipoli.android.app.events.InviteFriendEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.rate.RateDialog;
import io.ipoli.android.app.rate.RateDialogConstants;
import io.ipoli.android.app.share.ShareQuestDialog;
import io.ipoli.android.app.ui.events.HideLoaderEvent;
import io.ipoli.android.app.ui.events.ShowLoaderEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.EmailUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.challenge.fragments.ChallengeListFragment;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.activities.PickAvatarActivity;
import io.ipoli.android.player.activities.SignInActivity;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.PickAvatarRequestEvent;
import io.ipoli.android.player.fragments.GrowthFragment;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.commands.StartQuestCommand;
import io.ipoli.android.quest.commands.StopQuestCommand;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.DuplicateQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.NewQuestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ShareQuestEvent;
import io.ipoli.android.quest.events.StartQuestRequestEvent;
import io.ipoli.android.quest.events.StopQuestRequestEvent;
import io.ipoli.android.quest.fragments.CalendarFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.fragments.RepeatingQuestListFragment;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.dialogs.DatePickerFragment;
import io.ipoli.android.quest.ui.events.AddQuestRequestEvent;
import io.ipoli.android.quest.ui.events.EditRepeatingQuestRequestEvent;
import io.ipoli.android.reminders.data.Reminder;
import io.ipoli.android.reward.fragments.RewardListFragment;
import io.ipoli.android.settings.SettingsFragment;
import io.ipoli.android.tutorial.TutorialActivity;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String ACTION_QUEST_COMPLETE = "io.ipoli.android.intent.action.QUEST_COMPLETE";
    public static final String ACTION_ADD_QUEST_FROM_WIDGET = "io.ipoli.android.intent.action.ADD_QUEST_FROM_WIDGET";
    public static final int PICK_PLAYER_AVATAR_REQUEST_CODE = 101;
    private static final int PROGRESS_BAR_MAX_VALUE = 100;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.content_container)
    View contentContainer;

    @BindView(R.id.loading_container)
    View loadingContainer;

    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;

    @BindView(R.id.loading_message)
    TextView loadingMessage;

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
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appComponent().inject(this);
        ButterKnife.bind(this);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        if (localStorage.readBool(Constants.KEY_SHOULD_SHOW_TUTORIAL, true)) {
            localStorage.saveBool(Constants.KEY_SHOULD_SHOW_TUTORIAL, false);
            startTutorial();
        }

        loadingIndicator.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);

        isRateDialogShown = false;

        navigationView.setNavigationItemSelectedListener(this);

        startCalendar();
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                updatePlayerInDrawer();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        updatePlayerInDrawer();
    }

    @Override
    protected void onStop() {
        questPersistenceService.removeAllListeners();
        playerPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePlayerInDrawer() {

        if (navigationView.getHeaderCount() < 1) {
            return;
        }
        playerPersistenceService.listen(player -> {
            this.player = player;
            View header = navigationView.getHeaderView(0);
            TextView level = (TextView) header.findViewById(R.id.player_level);
            level.setText(String.format(getString(R.string.nav_header_player_level), player.getLevel()));

            TextView coins = (TextView) header.findViewById(R.id.player_coins);
            coins.setText(String.valueOf(player.getCoins()));

            ProgressBar experienceBar = (ProgressBar) header.findViewById(R.id.player_experience);
            experienceBar.setMax(PROGRESS_BAR_MAX_VALUE);
            experienceBar.setProgress(getCurrentProgress(player));

            CircleImageView avatarView = (CircleImageView) header.findViewById(R.id.player_image);
            avatarView.setImageResource(ResourceUtils.extractDrawableResource(MainActivity.this, player.getAvatar()));
            avatarView.setOnClickListener(v -> eventBus.post(new PickAvatarRequestEvent(EventSource.NAVIGATION_DRAWER)));

            TextView currentXP = (TextView) header.findViewById(R.id.player_current_xp);
            currentXP.setText(String.format(getString(R.string.nav_drawer_player_xp), player.getExperience()));
        });
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
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        if (isFromAction(ACTION_QUEST_COMPLETE)) {
            String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            setIntent(null);
            questPersistenceService.findById(questId, quest -> {
                startCalendar();
                eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.NOTIFICATION));
            });
        } else if (isFromAction(ACTION_ADD_QUEST_FROM_WIDGET)) {
            eventBus.post(new AddQuestRequestEvent(EventSource.WIDGET));
            setIntent(null);
            startActivity(new Intent(this, EditQuestActivity.class));
        }
    }

    private boolean isFromAction(String action) {
        return getIntent() != null && action.equals(getIntent().getAction());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
                        getString(R.string.quest_complete, experience, coins),
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
        long experience = q.getExperience();
        long coins = q.getCoins();
        String text = getString(q.getEndDate() == null ? R.string.quest_undone_to_inbox : R.string.quest_undone, experience, coins);
        Snackbar
                .make(contentContainer,
                        text,
                        Snackbar.LENGTH_SHORT)
                .show();
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
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
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
        if (e.date == null) {
            DatePickerFragment fragment = DatePickerFragment.newInstance(new Date(), true, date -> {
                duplicateQuest(e.quest, date);
            });
            fragment.show(getSupportFragmentManager());
        } else {
            duplicateQuest(e.quest, e.date);
        }
    }

    private void duplicateQuest(Quest quest, Date date) {
        boolean isForSameDay = DateUtils.isSameDay(quest.getEndDate(), date);
        quest.setId(null);
        quest.setCreatedAt(new Date().getTime());
        quest.setUpdatedAt(new Date().getTime());
        quest.setActualStartDate(null);
        quest.setEndDateFromLocal(date);
        quest.setCompletedAtMinute(null);
        quest.setCompletedAtDate(null);
        if (isForSameDay) {
            quest.setStartMinute(null);
        }
        List<Reminder> reminders = quest.getReminders();
        List<Reminder> newReminders = new ArrayList<>();
        int notificationId = new Random().nextInt();
        for (Reminder r : reminders) {
            newReminders.add(new Reminder(r.getMinutesFromStart(), notificationId));
        }
        eventBus.post(new NewQuestEvent(quest, newReminders, EventSource.CALENDAR));
        Snackbar snackbar = Snackbar
                .make(contentContainer,
                        R.string.quest_duplicated,
                        Snackbar.LENGTH_LONG);

        if (!isForSameDay) {
            snackbar.setAction(R.string.view, view -> {
                Time scrollToTime = null;
                if(!isForSameDay && quest.getStartMinute() > -1) {
                    scrollToTime = Time.of(quest.getStartMinute());
                }
                eventBus.post(new CurrentDayChangedEvent(new LocalDate(date.getTime()), scrollToTime, CurrentDayChangedEvent.Source.CALENDAR));
            });
        }

        snackbar.show();
    }

    @Subscribe
    public void onShowLoader(ShowLoaderEvent e) {
        if (!TextUtils.isEmpty(e.message)) {
            loadingMessage.setText(e.message);
        } else {
            loadingMessage.setText(R.string.loading_message);
        }
        loadingContainer.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
    }

    @Subscribe
    public void onHideLoader(HideLoaderEvent e) {
        loadingContainer.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        navigationView.setCheckedItem(item.getItemId());

        EventSource source = null;
        switch (item.getItemId()) {

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

            case R.id.invite_friends:
                eventBus.post(new InviteFriendEvent());
                inviteFriend();
                break;

            case R.id.settings:
                source = EventSource.SETTINGS;
                changeCurrentFragment(new SettingsFragment());
                break;

            case R.id.feedback:
                eventBus.post(new FeedbackTapEvent());
                RateDialog.newInstance(RateDialog.State.FEEDBACK).show(getSupportFragmentManager());
                break;

            case R.id.contact_us:
                eventBus.post(new ContactUsTapEvent());
                EmailUtils.send(this, getString(R.string.contact_us_email_subject), getString(R.string.contact_us_email_chooser_title));
                break;
        }

        if (source != null) {
            eventBus.post(new ScreenShownEvent(source));
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void startTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
    }

    private void inviteFriend() {
        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(Constants.FACEBOOK_APP_LINK)
                    .setPreviewImageUrl(Constants.FACEBOOK_INVITE_IMAGE_URL)
                    .build();
            AppInviteDialog.show(this, content);
        } else {
            Toast.makeText(this, R.string.show_invite_failed, Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onPickAvatarRequest(PickAvatarRequestEvent e) {
        startActivityForResult(new Intent(MainActivity.this, PickAvatarActivity.class), PICK_PLAYER_AVATAR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PLAYER_AVATAR_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String avatar = data.getStringExtra(Constants.AVATAR_NAME_EXTRA_KEY);
            if (!TextUtils.isEmpty(avatar)) {
                ImageView avatarImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.player_image);
                avatarImage.setImageResource(ResourceUtils.extractDrawableResource(this, avatar));
                player.setAvatar(avatar);
                playerPersistenceService.save(player);
            }
        }
    }

    public void startOverview() {
        changeCurrentFragment(new OverviewFragment());
    }
}