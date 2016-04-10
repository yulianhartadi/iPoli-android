package io.ipoli.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabClickListener;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.receivers.PlanDayReceiver;
import io.ipoli.android.app.receivers.ReviewDayReceiver;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.activities.QuestCompleteActivity;
import io.ipoli.android.quest.events.ColorLayoutEvent;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.fragments.AddQuestFragment;
import io.ipoli.android.quest.fragments.CalendarDayFragment;
import io.ipoli.android.quest.fragments.HabitsFragment;
import io.ipoli.android.quest.fragments.InboxFragment;
import io.ipoli.android.quest.fragments.OverviewFragment;

public class MainActivity extends BaseActivity {
    public static final int CALENDAR_TAB_INDEX = 0;
    public static final int OVERVIEW_TAB_INDEX = 1;
    public static final int ADD_QUEST_TAB_INDEX = 2;
    public static final int INBOX_TAB_INDEX = 3;
    public static final int HABITS_TAB_INDEX = 4;
    private int currentSelectedPosition = 0;

    private BottomBar bottomBar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        bottomBar = BottomBar.attachShy((CoordinatorLayout) findViewById(R.id.root_container),
                findViewById(R.id.content_container), savedInstanceState);

        bottomBar.setItems(
                new BottomBarTab(R.drawable.ic_event_white_24dp, "Calendar"),
                new BottomBarTab(R.drawable.ic_assignment_white_24dp, "Overview"),
                new BottomBarTab(R.drawable.ic_add_white_24dp, "AddQuest"),
                new BottomBarTab(R.drawable.ic_storage_white_24dp, "Inbox"),
                new BottomBarTab(R.drawable.ic_favorite_white_24dp, "Habits")
        );


        bottomBar.setOnTabClickListener(new OnTabClickListener() {
            @Override
            public void onTabSelected(int position) {
                colorLayout(QuestContext.LEARNING);

                switch (position) {
                    case CALENDAR_TAB_INDEX:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_container, new CalendarDayFragment()).commit();
                        break;
                    case OVERVIEW_TAB_INDEX:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_container, new OverviewFragment()).commit();
                        break;
                    case ADD_QUEST_TAB_INDEX:
                        boolean isForToday = currentSelectedPosition == CALENDAR_TAB_INDEX;
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_container, AddQuestFragment.newInstance(isForToday)).commit();
                        break;
                    case INBOX_TAB_INDEX:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_container, new InboxFragment()).commit();
                        break;
                    case HABITS_TAB_INDEX:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_container, new HabitsFragment()).commit();
                        break;
                }
                currentSelectedPosition = position;
            }

            @Override
            public void onTabReSelected(int position) {
            }
        });

        bottomBar.mapColorForTab(CALENDAR_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
        bottomBar.mapColorForTab(OVERVIEW_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
        bottomBar.mapColorForTab(ADD_QUEST_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
        bottomBar.mapColorForTab(INBOX_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
        bottomBar.mapColorForTab(HABITS_TAB_INDEX, ContextCompat.getColor(this, R.color.colorPrimary));
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
        if (getIntent() != null && (PlanDayReceiver.ACTION_REMIND_PLAN_DAY.equals(getIntent().getAction())
                || ReviewDayReceiver.ACTION_REMIND_REVIEW_DAY.equals(getIntent().getAction()))) {
            bottomBar.selectTabAtPosition(CALENDAR_TAB_INDEX, false);
        }
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
        View view = bottomBar.findViewById(R.id.bb_bottom_bar_item_container);
        view.setBackgroundColor(ContextCompat.getColor(this, context.resLightColor));
    }

    @Subscribe
    public void onShowQuestEvent(ShowQuestEvent e) {
        Intent i = new Intent(this, QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivity(i);
    }

    @Subscribe
    public void onQuestCompleteRequest(CompleteQuestRequestEvent e) {
        Intent i = new Intent(this, QuestCompleteActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivityForResult(i, Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE);
    }

}
