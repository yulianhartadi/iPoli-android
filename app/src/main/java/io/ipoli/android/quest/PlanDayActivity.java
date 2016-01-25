package io.ipoli.android.quest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.events.DeleteQuestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.QuestDeleteRequestEvent;
import io.ipoli.android.quest.events.QuestsPlannedEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class PlanDayActivity extends BaseActivity {

    @Bind(R.id.plan_day_container)
    LinearLayout rootContainer;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appComponent().inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        List<Quest> quests = questPersistenceService.findAllUncompleted();
        resetDueDateForIncompleteQuests(quests);
        planDayQuestAdapter = new PlanDayQuestAdapter(quests, eventBus);
        questList.setAdapter(planDayQuestAdapter);

        int swipeFlags = ItemTouchHelper.START;
        ItemTouchCallback touchCallback = new ItemTouchCallback(planDayQuestAdapter, swipeFlags);
        touchCallback.setLongPressDragEnabled(false);
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);
    }

    private void resetDueDateForIncompleteQuests(List<Quest> quests) {
        for (Quest q : quests) {
            if (q.getDue() != null && DateUtils.isBeforeToday(q.getDue())) {
                q.setDue(null);
                questPersistenceService.save(q);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.save_quests:
                List<Quest> plannedQuests = planDayQuestAdapter.getQuests();
                for (Quest q : plannedQuests) {
                    q.setDue(new Date());
                    questPersistenceService.save(q);
                }
                eventBus.post(new QuestsPlannedEvent(plannedQuests));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quest_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
    }

    @Subscribe
    public void onQuestDeleteRequest(final QuestDeleteRequestEvent e) {
        final Snackbar snackbar = Snackbar
                .make(rootContainer,
                        R.string.quest_removed,
                        Snackbar.LENGTH_LONG);

        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                questPersistenceService.delete(e.quest);
                eventBus.post(new DeleteQuestEvent(e.quest));
            }
        });

        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                planDayQuestAdapter.addQuest(e.position, e.quest);
                snackbar.setCallback(null);
                eventBus.post(new UndoDeleteQuestEvent(e.quest));
            }
        });

        snackbar.show();
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        Intent i = new Intent(this, EditQuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.questId);
        startActivity(i);
    }
}
