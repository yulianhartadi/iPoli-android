package io.ipoli.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.FeedbackTapEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
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
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ShareQuestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.fragments.CalendarFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.fragments.RepeatingQuestsFragment;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String ACTION_QUEST_COMPLETE = "io.ipoli.android.intent.action.QUEST_COMPLETE";
    public static final String ACTION_ADD_QUEST = "io.ipoli.android.intent.action.ADD_QUEST";

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

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

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
                // replace to calendar fragment if current is not calendar fragment
                eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.NOTIFICATION));
            });
        } else if (isFromAction(ACTION_ADD_QUEST)) {
            setIntent(null);
            // start add activity
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
            case R.id.action_help:
//            HelpDialog.newInstance(Screen.values()[bottomBar.getCurrentTabPosition()]).show(getSupportFragmentManager());
                return true;

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
        Snackbar snackbar = Snackbar
                .make(rootContainer,
                        R.string.quest_complete,
                        Snackbar.LENGTH_SHORT);

        snackbar.setAction(R.string.share, view -> {
            eventBus.post(new ShareQuestEvent(e.quest, EventSource.SNACKBAR));
        });

        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                if (shouldShowRateDialog()) {
                    isRateDialogShown = true;
                    new RateDialog().show(getSupportFragmentManager());
                }
            }
        });

        snackbar.show();
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
        Snackbar
                .make(rootContainer,
                        R.string.quest_undone,
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        drawerLayout.closeDrawers();

        toolbarExpandContainer.setOnClickListener(null);
        appBar.setExpanded(false, false);
        appBar.setTag(false);
        calendarIndicator.setVisibility(View.GONE);
        String screenName = "";
        switch (item.getItemId()) {

            case R.id.home:
                calendarIndicator.setVisibility(View.VISIBLE);
                toolbarExpandContainer.setOnClickListener(MainActivity.this);
                screenName = "calendar";
                CalendarFragment calendarFragment = new CalendarFragment();
                toolbarCalendar.setListener(calendarFragment);
                changeCurrentFragment(calendarFragment, new SimpleDateFormat(getString(R.string.today_calendar_format), Locale.getDefault()).format(new Date()));
                // go to calendar
                break;

            case R.id.inbox:
                screenName = "inbox";
                changeCurrentFragment(new InboxFragment(), R.string.title_activity_inbox);

                break;
            case R.id.repeating_quests:
                screenName = "repeating_quests";
                changeCurrentFragment(new RepeatingQuestsFragment(), R.string.title_fragment_repeating_quests);
                break;

            case R.id.feedback:
                eventBus.post(new FeedbackTapEvent());
                EmailUtils.send(this, getString(R.string.feedback_email_subject), getString(R.string.feedback_email_chooser_title));
                break;
        }

        eventBus.post(new ScreenShownEvent(screenName));

        return true;
    }
}