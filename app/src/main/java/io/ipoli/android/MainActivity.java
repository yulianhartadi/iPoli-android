package io.ipoli.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
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
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.squareup.otto.Subscribe;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.CalendarPermissionResponseEvent;
import io.ipoli.android.app.events.ContactUsTapEvent;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FeedbackTapEvent;
import io.ipoli.android.app.events.InviteFriendEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.SyncCalendarRequestEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.rate.RateDialog;
import io.ipoli.android.app.rate.RateDialogConstants;
import io.ipoli.android.app.ui.events.CloseToolbarCalendarEvent;
import io.ipoli.android.app.ui.events.HideLoaderEvent;
import io.ipoli.android.app.ui.events.NewTitleEvent;
import io.ipoli.android.app.ui.events.ShowLoaderEvent;
import io.ipoli.android.app.ui.events.ToolbarCalendarTapEvent;
import io.ipoli.android.app.utils.EmailUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.player.ExperienceForLevelGenerator;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.activities.PickAvatarActivity;
import io.ipoli.android.player.events.LevelDownEvent;
import io.ipoli.android.player.events.LevelUpEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ShareQuestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.fragments.CalendarFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.fragments.RepeatingQuestListFragment;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.reward.fragments.RewardListFragment;
import io.ipoli.android.tutorial.TutorialActivity;
import io.ipoli.android.tutorial.events.ShowTutorialEvent;

public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String ACTION_QUEST_COMPLETE = "io.ipoli.android.intent.action.QUEST_COMPLETE";
    public static final String ACTION_ADD_QUEST = "io.ipoli.android.intent.action.ADD_QUEST";
    public static final int PICK_PLAYER_AVATAR = 101;
    private static final int PROGRESS_BAR_MAX_VALUE = 100;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.content_container)
    View contentContainer;

    @BindView(R.id.loading_container)
    View loadingContainer;

    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;

    @BindView(R.id.loading_message)
    TextView loadingMessage;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.toolbar_expand_container)
    View toolbarExpandContainer;

    @BindView(R.id.toolbar_calendar)
    CompactCalendarView toolbarCalendar;

    @BindView(R.id.toolbar_calendar_indicator)
    ImageView calendarIndicator;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    Fragment currentFragment;

    private boolean isRateDialogShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appComponent().inject(this);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        LocalStorage localStorage = LocalStorage.of(this);
        if (localStorage.readBool(Constants.KEY_SHOULD_SHOW_TUTORIAL, true)) {
            localStorage.saveBool(Constants.KEY_SHOULD_SHOW_TUTORIAL, false);
            startTutorial();
        }

        toolbarCalendar.setCurrentDate(new Date());

        loadingIndicator.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);

        isRateDialogShown = false;

        navigationView.setNavigationItemSelectedListener(this);

        changeToCalendarFragment();
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                updatePlayerInDrawer();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        updatePlayerInDrawer();
    }

    private void updatePlayerInDrawer() {

        if (navigationView.getHeaderCount() < 1) {
            return;
        }
        Player player = playerPersistenceService.findSync();
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
        avatarView.setOnClickListener(v -> {
            startActivityForResult(new Intent(MainActivity.this, PickAvatarActivity.class), PICK_PLAYER_AVATAR);
        });

        TextView currentXP = (TextView) header.findViewById(R.id.player_current_xp);
        currentXP.setText(String.format(getString(R.string.nav_drawer_player_xp), player.getExperience()));
    }

    private int getCurrentProgress(Player player) {
        int currentLevel = player.getLevel();
        BigInteger requiredXPForCurrentLevel = ExperienceForLevelGenerator.forLevel(currentLevel);
        BigDecimal xpForNextLevel = new BigDecimal(ExperienceForLevelGenerator.forLevel(currentLevel + 1).subtract(requiredXPForCurrentLevel));
        BigDecimal currentXP = new BigDecimal(new BigInteger(player.getExperience()).subtract(requiredXPForCurrentLevel));
        return (int) (currentXP.divide(xpForNextLevel, 2, RoundingMode.HALF_UP).doubleValue() * PROGRESS_BAR_MAX_VALUE);
    }

    private void changeToCalendarFragment() {
        appBar.setExpanded(false, false);
        appBar.setTag(false);

        calendarIndicator.setVisibility(View.VISIBLE);
        toolbarExpandContainer.setOnClickListener(this);
        CalendarFragment calendarFragment = new CalendarFragment();
        toolbarCalendar.setListener(calendarFragment);
        changeCurrentFragment(calendarFragment, new SimpleDateFormat(getString(R.string.today_calendar_format), Locale.getDefault()).format(new Date()));
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        if (isFromAction(ACTION_QUEST_COMPLETE)) {
            String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            setIntent(null);
            questPersistenceService.findById(questId).subscribe(quest -> {
                changeToCalendarFragment();
                eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.NOTIFICATION));
            });
        } else if (isFromAction(ACTION_ADD_QUEST)) {
            setIntent(null);
            startActivity(new Intent(this, AddQuestActivity.class));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_overview:
                toolbarExpandContainer.setOnClickListener(null);
                appBar.setExpanded(false, false);
                appBar.setTag(false);
                calendarIndicator.setVisibility(View.GONE);
                changeCurrentFragment(new OverviewFragment(), R.string.overview_title);
                return true;

            case R.id.action_calendar:
                calendarIndicator.setVisibility(View.VISIBLE);
                toolbarExpandContainer.setOnClickListener(MainActivity.this);
                CalendarFragment calendarFragment = new CalendarFragment();
                toolbarCalendar.setListener(calendarFragment);
                changeCurrentFragment(calendarFragment, new SimpleDateFormat(getString(R.string.today_calendar_format), Locale.getDefault()).format(new Date()));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeCurrentFragment(Fragment fragment, @StringRes int title) {
        changeCurrentFragment(fragment, getString(title));
    }

    private void changeCurrentFragment(Fragment fragment, String title) {
        toolbarTitle.setText(title);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container, fragment).commit();
        currentFragment = fragment;
        getSupportFragmentManager().executePendingTransactions();
    }

    private void colorLayout(QuestContext context) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, context.resDarkColor));
    }

    @Subscribe
    public void onShowQuestEvent(ShowQuestEvent e) {
        Intent i = new Intent(this, QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivity(i);
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        Quest q = e.quest;
        long experience = q.getExperience();
        long coins = q.getCoins();

        Snackbar snackbar = Snackbar
                .make(rootContainer,
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
        LocalStorage localStorage = LocalStorage.of(this);
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

        Snackbar
                .make(rootContainer,
                        getString(R.string.quest_undone, experience, coins),
                        Snackbar.LENGTH_SHORT)
                .show();
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        Intent i = new Intent(this, EditQuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivity(i);
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
    public void onNewTitle(NewTitleEvent e) {
        toolbarTitle.setText(e.text);
    }

    @Override
    public void onClick(View v) {
        boolean isExpanded = (boolean) appBar.getTag();
        calendarIndicator.animate().rotation(isExpanded ? 0 : 180).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        appBar.setExpanded(!isExpanded, true);
        appBar.setTag(!isExpanded);
        eventBus.post(new ToolbarCalendarTapEvent(!isExpanded));
    }

    @Subscribe
    public void onCurrentDayChanged(CurrentDayChangedEvent e) {
        if (e.source == CurrentDayChangedEvent.Source.CALENDAR) {
            return;
        }
        toolbarCalendar.setCurrentDate(e.date.toDate());
    }

    @Subscribe
    public void onCloseToolbarCalendar(CloseToolbarCalendarEvent e) {
        boolean isExpanded = (boolean) appBar.getTag();
        if (isExpanded) {
            calendarIndicator.animate().rotation(0).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            appBar.setExpanded(false, true);
        }
        appBar.setTag(false);
    }

    @Subscribe
    public void onLevelUp(LevelUpEvent e) {
        showLevelUpMessage(e.newLevel);
    }

    @Subscribe
    public void onLevelDown(LevelDownEvent e) {
        showLevelDownMessage(e.newLevel);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        navigationView.setCheckedItem(item.getItemId());
        drawerLayout.closeDrawers();

        toolbarExpandContainer.setOnClickListener(null);
        appBar.setExpanded(false, false);
        appBar.setTag(false);
        calendarIndicator.setVisibility(View.GONE);
        EventSource source = null;
        switch (item.getItemId()) {

            case R.id.home:
                calendarIndicator.setVisibility(View.VISIBLE);
                toolbarExpandContainer.setOnClickListener(MainActivity.this);
                source = EventSource.CALENDAR;
                CalendarFragment calendarFragment = new CalendarFragment();
                toolbarCalendar.setListener(calendarFragment);
                changeCurrentFragment(calendarFragment, new SimpleDateFormat(getString(R.string.today_calendar_format), Locale.getDefault()).format(new Date()));
                break;

            case R.id.inbox:
                source = EventSource.INBOX;
                changeCurrentFragment(new InboxFragment(), R.string.title_activity_inbox);

                break;
            case R.id.repeating_quests:
                source = EventSource.REPEATING_QUESTS;
                changeCurrentFragment(new RepeatingQuestListFragment(), R.string.title_fragment_repeating_quests);
                break;

//            case R.id.challenges:
//                source = EventSource.CHALLENGES;
//                changeCurrentFragment(new ChallengeListFragment(), R.string.title_fragment_challenges);
//                break;

            case R.id.rewards:
                source = EventSource.REWARDS;
                changeCurrentFragment(new RewardListFragment(), R.string.title_fragment_rewards);
                break;

            case R.id.invite_friends:
                eventBus.post(new InviteFriendEvent());
                inviteFriend();
                break;
            case R.id.sync_calendars:
                checkForCalendarPermission();
                break;
            case R.id.tutorial:
                eventBus.post(new ShowTutorialEvent());
                startTutorial();
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

    private void checkForCalendarPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALENDAR},
                    Constants.READ_CALENDAR_PERMISSION_REQUEST_CODE);
        } else {
            eventBus.post(new SyncCalendarRequestEvent(EventSource.OPTIONS_MENU));
            Toast.makeText(this, R.string.import_calendar_events_started, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.READ_CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                eventBus.post(new CalendarPermissionResponseEvent(CalendarPermissionResponseEvent.Response.GRANTED, EventSource.OPTIONS_MENU));
                eventBus.post(new SyncCalendarRequestEvent(EventSource.TUTORIAL));
                Toast.makeText(this, R.string.import_calendar_events_started, Toast.LENGTH_SHORT).show();
            } else if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                eventBus.post(new CalendarPermissionResponseEvent(CalendarPermissionResponseEvent.Response.DENIED, EventSource.OPTIONS_MENU));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PLAYER_AVATAR) {
            String avatar = data.getStringExtra(Constants.AVATAR_NAME_EXTRA_KEY);
            if (!TextUtils.isEmpty(avatar)) {
                ImageView avatarImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.player_image);
                avatarImage.setImageResource(ResourceUtils.extractDrawableResource(this, avatar));
                Player player = playerPersistenceService.findSync();
                player.setAvatar(avatar);
                playerPersistenceService.saveSync(player);
            }
        }
    }
}