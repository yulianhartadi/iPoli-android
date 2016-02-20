package io.ipoli.android.quest.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.ui.DividerItemDecoration;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.quest.PlanDayQuestAdapter;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.Status;
import io.ipoli.android.quest.events.DeleteQuestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class PlanDayActivity extends BaseActivity {

    @Bind(R.id.plan_day_container)
    CoordinatorLayout rootContainer;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.player_level)
    TextView playerLevel;

    @Inject
    Bus eventBus;

    @Bind(R.id.quest_list)
    RecyclerView questList;

    @Inject
    QuestPersistenceService questPersistenceService;

    private PlanDayQuestAdapter planDayQuestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_day);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        appComponent().inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        List<Quest> quests = getAllUnplanned();
        planDayQuestAdapter = new PlanDayQuestAdapter(this, quests, eventBus);
        questList.setAdapter(planDayQuestAdapter);
        questList.addItemDecoration(new DividerItemDecoration(this));

        ItemTouchCallback touchCallback = new ItemTouchCallback(planDayQuestAdapter, ItemTouchHelper.START | ItemTouchHelper.END);
        touchCallback.setLongPressDragEnabled(false);
        touchCallback.setSwipeStartDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.md_red_500)));
        touchCallback.setSwipeEndDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.md_blue_500)));
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);
    }

    private List<Quest> getAllUnplanned() {
        return questPersistenceService.findAllUnplanned();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        planDayQuestAdapter.updateQuests(getAllUnplanned());
    }

    @Subscribe
    public void onQuestDeleteRequest(final DeleteQuestRequestEvent e) {
        final Snackbar snackbar = Snackbar
                .make(rootContainer,
                        R.string.quest_removed,
                        Snackbar.LENGTH_LONG);

        final Quest quest = e.quest;

        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                QuestNotificationScheduler.stopAll(quest.getId(), PlanDayActivity.this);
                questPersistenceService.delete(quest);
                eventBus.post(new DeleteQuestEvent(quest));
                if (planDayQuestAdapter.getQuests().isEmpty()) {
                    finish();
                }
            }
        });

        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                planDayQuestAdapter.addQuest(e.position, quest);
                snackbar.setCallback(null);
                eventBus.post(new UndoDeleteQuestEvent(quest));
            }
        });

        snackbar.show();
    }

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        Quest q = e.quest;
        q.setStatus(Status.PLANNED.name());
        q.setDue(new Date());
        questPersistenceService.save(q);
        Toast.makeText(this, "Quest scheduled for today", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        Intent i = new Intent(this, EditQuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivityForResult(i, Constants.EDIT_QUEST_RESULT_REQUEST_CODE);
    }
}
