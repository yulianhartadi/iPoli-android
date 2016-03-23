package io.ipoli.android.quest.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.BottomBarUtil;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.HabitsAdapter;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class HabitsActivity extends BaseActivity {
    @Inject
    Bus eventBus;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.quest_list)
    RecyclerView questList;

    @Inject
    QuestPersistenceService questPersistenceService;

    private HabitsAdapter habitsAdapter;
    private BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);
        bottomBar = BottomBarUtil.getBottomBar(this, R.id.root_container, R.id.quest_list, savedInstanceState, BottomBarUtil.HABITS_TAB_INDEX);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(new SimpleDateFormat(getString(R.string.today_date_format), Locale.getDefault()).format(new Date()));
        }

        appComponent().inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        habitsAdapter = new HabitsAdapter(this, new ArrayList<Quest>(), eventBus);
        questList.setAdapter(habitsAdapter);

        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        ItemTouchCallback touchCallback = new ItemTouchCallback(habitsAdapter, 0, swipeFlags);
        touchCallback.setLongPressDragEnabled(false);
        touchCallback.setSwipeEndDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.md_green_500)));
        touchCallback.setSwipeStartDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.md_blue_500)));
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);
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
        updateQuests();
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        Quest q = e.quest;
        Date due = new Date();
        String toast = getString(R.string.quest_scheduled_for_today);
        if (DateUtils.isToday(e.quest.getDue())) {
            toast = getString(R.string.quest_scheduled_for_tomorrow);
            due = DateUtils.getTomorrow();
        }
        q.setDue(due);
        questPersistenceService.save(q);
        updateQuests();
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onQuestSnoozed(QuestSnoozedEvent e) {
        updateQuests();

    }

    private void updateQuests() {
        habitsAdapter.updateQuests(questPersistenceService.findAllPlanned());
    }
}
