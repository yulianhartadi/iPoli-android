package io.ipoli.android.quest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.events.CompleteQuestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.QuestCompleteRequestEvent;
import io.ipoli.android.quest.events.QuestUpdatedEvent;
import io.ipoli.android.quest.events.UndoCompleteQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.services.QuestTimerIntentService;
import io.ipoli.android.quest.services.UpdateQuestIntentService;

public class QuestListActivity extends BaseActivity {

    public static final String ACTION_QUEST_DONE = "io.ipoli.android.action.QUEST_DONE";
    public static final String ACTION_QUEST_CANCELED = "io.ipoli.android.action.QUEST_CANCELED";

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        List<Quest> quests = questPersistenceService.findAllPlannedForToday();
        questAdapter = new QuestAdapter(quests, eventBus);
        questList.setAdapter(questAdapter);

        int swipeFlags = ItemTouchHelper.END;
        ItemTouchCallback touchCallback = new ItemTouchCallback(questAdapter, swipeFlags);
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
        if (getIntent() == null || TextUtils.isEmpty(getIntent().getAction())) {
            return;
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        if (action.equals(ACTION_QUEST_DONE) || action.equals(ACTION_QUEST_CANCELED)) {

            dismissTimerNotification();
            String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            Quest q = questPersistenceService.findById(questId);

            Status newStatus = action.equals(ACTION_QUEST_DONE) ? Status.COMPLETED : Status.PLANNED;
            questAdapter.updateQuestStatus(q, newStatus);
        }
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        saveQuestsOrder();
        super.onPause();
    }

    private void saveQuestsOrder() {
        List<Quest> quests = questAdapter.getQuests();
        int order = 0;
        for (Quest q : quests) {
            q.setOrder(order);
            order++;
        }
        questPersistenceService.saveAll(quests);
    }

    @Subscribe
    public void onQuestCompleteRequest(final QuestCompleteRequestEvent e) {
        stopQuestTimer(e.quest);
        Intent i = new Intent(this, QuestCompleteActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        i.putExtra(Constants.POSITION_EXTRA_KEY, e.position);
        startActivityForResult(i, Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK || resultCode == RESULT_CANCELED) && requestCode == Constants.COMPLETE_QUEST_RESULT_REQUEST_CODE) {
            final int position = data.getIntExtra(Constants.POSITION_EXTRA_KEY, -1);
            final String id = data.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            final Quest q = questPersistenceService.findById(id);
            final Difficulty d = (Difficulty) data.getSerializableExtra(QuestCompleteActivity.DIFFICULTY_EXTRA_KEY);
            final String log = data.getStringExtra(QuestCompleteActivity.LOG_EXTRA_KEY);

            final Snackbar snackbar = Snackbar
                    .make(rootContainer,
                            getString(R.string.quest_complete),
                            Snackbar.LENGTH_LONG);

            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    q.setStatus(Status.COMPLETED.name());
                    q.setLog(log);
                    q.setDifficulty(d.name());
                    questPersistenceService.save(q);
                    eventBus.post(new CompleteQuestEvent(q));
                }

            });

            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position >= 0) {
                        questAdapter.putQuest(position, q);
                    } else {
                        questAdapter.addQuest(q);
                    }
                    snackbar.setCallback(null);
                    eventBus.post(new UndoCompleteQuestEvent(q));
                }
            });

            snackbar.show();
        } else if(resultCode == RESULT_OK && requestCode == Constants.EDIT_QUEST_RESULT_REQUEST_CODE) {
            final int position = data.getIntExtra(Constants.POSITION_EXTRA_KEY, -1);
            final String id = data.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            final Quest q = questPersistenceService.findById(id);
            if(DateUtils.isToday(q.getDue())) {
                questAdapter.updateQuest(position, q);
            } else {
                questAdapter.removeQuest(position);
            }
        }
    }

    @Subscribe
    public void onQuestUpdated(QuestUpdatedEvent e) {
        questPersistenceService.save(e.quest);
        Status status = Status.valueOf(e.quest.getStatus());
        if (status == Status.STARTED) {
            Intent intent = new Intent(this, UpdateQuestIntentService.class);
            intent.setAction(UpdateQuestIntentService.ACTION_START_QUEST);
            intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
            startService(intent);
            showSnackBar(R.string.quest_started);
        } else if (status == Status.PLANNED) {
            Quest q = e.quest;
            stopQuestTimer(q);
            showSnackBar(R.string.quest_stopped);
        }
    }

    private void stopQuestTimer(Quest q) {
        cancelUpdateTimerIntent(q);
        dismissTimerNotification();
    }

    private void cancelUpdateTimerIntent(Quest quest) {
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.cancel(getUpdateTimerPendingIntent(quest));
    }

    private void dismissTimerNotification() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(Constants.QUEST_TIMER_NOTIFICATION_ID);
    }

    private void showSnackBar(@StringRes int textRes) {
        Snackbar.make(rootContainer, getString(textRes), Snackbar.LENGTH_SHORT).show();
    }

    private PendingIntent getUpdateTimerPendingIntent(Quest quest) {
        Intent intent = new Intent(this, QuestTimerIntentService.class);
        intent.setAction(QuestTimerIntentService.ACTION_SHOW_QUEST_TIMER);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, quest.getId());
        return PendingIntent.getService(this, Constants.QUEST_UPDATE_TIMER_REQUEST_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        Intent i = new Intent(this, EditQuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.questId);
        i.putExtra(Constants.POSITION_EXTRA_KEY, e.position);
        startActivityForResult(i, Constants.EDIT_QUEST_RESULT_REQUEST_CODE);
    }
}
