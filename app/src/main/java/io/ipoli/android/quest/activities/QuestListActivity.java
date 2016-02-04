package io.ipoli.android.quest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.ui.DividerItemDecoration;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestAdapter;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class QuestListActivity extends BaseActivity {

    @Bind(R.id.quest_list_container)
    LinearLayout rootContainer;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    Bus eventBus;

    @Bind(R.id.quest_list)
    RecyclerView questList;

    @Inject
    QuestPersistenceService questPersistenceService;

    private QuestAdapter questAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        appComponent().inject(this);

        toolbar.setTitle(new SimpleDateFormat("'Today - 'EEE, d MMM", Locale.getDefault()).format(new Date()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.addItemDecoration(new DividerItemDecoration(this));
        questList.setLayoutManager(layoutManager);

        List<Quest> quests = questPersistenceService.findAllPlannedForToday();
        questAdapter = new QuestAdapter(this, quests, eventBus);
        questList.setAdapter(questAdapter);

        int swipeFlags = ItemTouchHelper.END;
        ItemTouchCallback touchCallback = new ItemTouchCallback(questAdapter, swipeFlags, 0);
        touchCallback.setLongPressDragEnabled(false);
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);
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
        resetQuestDataSet();
    }

    private void resetQuestDataSet() {
        List<Quest> quests = questPersistenceService.findAllPlannedForToday();
        if (quests.isEmpty()) {
            finish();
            return;
        }
        questAdapter.updateQuests(quests);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onQuestCompleteRequest(CompleteQuestRequestEvent e) {
        Intent i = new Intent(this, QuestCompleteActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivityForResult(i, Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK || resultCode == RESULT_CANCELED) && requestCode == Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE) {
            showSnackBar(R.string.quest_complete);
        } else if (resultCode == RESULT_OK && requestCode == Constants.EDIT_QUEST_RESULT_REQUEST_CODE) {
            final int position = data.getIntExtra(Constants.POSITION_EXTRA_KEY, -1);
            final String id = data.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            final Quest q = questPersistenceService.findById(id);
            if (DateUtils.isToday(q.getDue())) {
                resetQuestDataSet();
            } else {
                questAdapter.removeQuest(position);
            }
        }
    }

    @Subscribe
    public void onQuestUpdated(QuestUpdatedEvent e) {
        questPersistenceService.save(e.quest);
    }

    private void showSnackBar(@StringRes int textRes) {
        Snackbar.make(rootContainer, getString(textRes), Snackbar.LENGTH_SHORT).show();
    }
}
