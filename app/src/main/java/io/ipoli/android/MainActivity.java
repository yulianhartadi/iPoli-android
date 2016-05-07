package io.ipoli.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.events.CurrentDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.events.UndoCompletedQuestEvent;
import io.ipoli.android.app.ui.events.HideLoaderEvent;
import io.ipoli.android.app.ui.events.NewTitleEvent;
import io.ipoli.android.app.ui.events.ShowLoaderEvent;
import io.ipoli.android.app.ui.events.ToolbarCalendarTapEvent;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.events.ColorLayoutEvent;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ShareQuestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.fragments.AddQuestFragment;
import io.ipoli.android.quest.fragments.CalendarFragment;
import io.ipoli.android.quest.fragments.HabitsFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final int CALENDAR_TAB_INDEX = 0;
    public static final int OVERVIEW_TAB_INDEX = 1;
    public static final int ADD_QUEST_TAB_INDEX = 2;
    public static final int INBOX_TAB_INDEX = 3;
    public static final int HABITS_TAB_INDEX = 4;

    public static final String ACTION_QUEST_DONE = "io.ipoli.android.intent.action.QUEST_DONE";

    @IdRes
    private int currentSelectedItem = 0;

    private BottomBar bottomBar;

    @Bind(R.id.root_container)
    CoordinatorLayout rootContainer;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.content_container)
    View contentContainer;

    @Bind(R.id.loading_container)
    View loadingContainer;

    @Bind(R.id.loading_indicator)
    ProgressBar loadingIndicator;

    @Bind(R.id.loading_message)
    TextView loadingMessage;

    @Bind(R.id.toolbar_title)
    TextView toolbarTitle;

    @Bind(R.id.toolbar_expand_container)
    View toolbarExpandContainer;

    @Bind(R.id.toolbar_calendar)
    CompactCalendarView toolbarCalendar;

    @Bind(R.id.toolbar_calendar_indicator)
    ImageView calendarIndicator;

    @Bind(R.id.appbar)
    AppBarLayout appBar;

    @Inject
    QuestPersistenceService questPersistenceService;

    Fragment currentFragment;

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

        initBottomBar(savedInstanceState);
        toolbarCalendar.setCurrentDate(new Date());


        loadingIndicator.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void initBottomBar(Bundle savedInstanceState) {
        bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.noTopOffset();
        bottomBar.setItemsFromMenu(R.menu.bottom_bar_menu, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                toolbarExpandContainer.setOnClickListener(null);
                appBar.setExpanded(false, false);
                appBar.setTag(false);
                calendarIndicator.setVisibility(View.GONE);
                String screenName = "";
                resetLayoutColors();

                switch (menuItemId) {
                    case R.id.calendar:
                        calendarIndicator.setVisibility(View.VISIBLE);
                        toolbarExpandContainer.setOnClickListener(MainActivity.this);
                        screenName = "calendar";
                        CalendarFragment calendarFragment = new CalendarFragment();
                        toolbarCalendar.setListener(calendarFragment);
                        currentFragment = calendarFragment;
                        toolbarTitle.setText(new SimpleDateFormat(getString(R.string.today_calendar_format), Locale.getDefault()).format(new Date()));
                        break;
                    case R.id.overview:
                        screenName = "overview";
                        currentFragment = new OverviewFragment();
                        toolbarTitle.setText(R.string.overview_title);
                        break;
                    case R.id.add_quest:
                        screenName = "add_quest";
                        boolean isForToday = currentSelectedItem == R.id.calendar;
                        currentFragment = AddQuestFragment.newInstance(isForToday);
                        toolbarTitle.setText(R.string.title_activity_add_quest);
                        break;
                    case R.id.inbox:
                        screenName = "inbox";
                        currentFragment = new InboxFragment();
                        toolbarTitle.setText(R.string.title_activity_inbox);
                        break;
                    case R.id.habits:
                        screenName = "habits";
                        currentFragment = new HabitsFragment();
                        toolbarTitle.setText(R.string.title_fragment_habits);
                        break;
                }


                if (currentFragment == null) {
                    return;
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_container, currentFragment).commit();
                currentSelectedItem = menuItemId;
                eventBus.post(new ScreenShownEvent(screenName));
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {

            }
        });


        bottomBar.mapColorForTab(CALENDAR_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
        bottomBar.mapColorForTab(OVERVIEW_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
        bottomBar.mapColorForTab(ADD_QUEST_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
        bottomBar.mapColorForTab(INBOX_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
        bottomBar.mapColorForTab(HABITS_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void resetLayoutColors() {
        colorLayout(QuestContext.LEARNING);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        bottomBar.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        if (getIntent() != null && ACTION_QUEST_DONE.equals(getIntent().getAction())) {
            String questId = getIntent().getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            setIntent(null);
            questPersistenceService.findById(questId).subscribe(quest -> {
                bottomBar.selectTabAtPosition(CALENDAR_TAB_INDEX, false);
                eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.NOTIFICATION));
            });
        }
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

    @Subscribe
    public void onColorLayout(ColorLayoutEvent e) {
        QuestContext context = e.questContext;
        colorLayout(context);
    }

    private void colorLayout(QuestContext context) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, context.resLightColor));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, context.resDarkColor));
        View view = bottomBar.findViewById(R.id.bb_bottom_bar_outer_container);
        view.setBackgroundColor(ContextCompat.getColor(this, context.resLightColor));
    }

    @Subscribe
    public void onShowQuestEvent(ShowQuestEvent e) {
        Intent i = new Intent(this, QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivity(i);
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        bottomBar.post(() -> {
            Snackbar snackbar = Snackbar
                    .make(rootContainer,
                            R.string.quest_complete,
                            Snackbar.LENGTH_SHORT);

            snackbar.setAction(R.string.share, view -> {
                eventBus.post(new ShareQuestEvent(e.quest, EventSource.SNACKBAR));
            });

            snackbar.show();
        });
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
}