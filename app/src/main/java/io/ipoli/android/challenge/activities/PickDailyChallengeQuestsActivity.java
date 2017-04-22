package io.ipoli.android.challenge.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.tutorial.PickQuestViewModel;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.challenge.adapters.PickDailyChallengeQuestsAdapter;
import io.ipoli.android.challenge.events.DailyChallengeQuestsSelectedEvent;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.AddQuestButtonTappedEvent;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/22/16.
 */
public class PickDailyChallengeQuestsActivity extends BaseActivity implements OnDataChangedListener<List<Quest>> {

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @BindView(R.id.root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.quest_list)
    EmptyStateRecyclerView questList;

    private PickDailyChallengeQuestsAdapter pickQuestsAdapter;

    private List<Quest> previouslySelectedQuests = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_daily_challenge_quests);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        pickQuestsAdapter = new PickDailyChallengeQuestsAdapter(this, eventBus, new ArrayList<>());
        questList.setAdapter(pickQuestsAdapter);
        questList.setEmptyView(rootContainer, R.string.empty_daily_challenge_quests_text, R.drawable.ic_compass_grey_24dp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        questPersistenceService.listenForAllIncompleteOrMostImportantForDate(LocalDate.now(), this);
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

    @Override
    protected void onStop() {
        questPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_pick_daily_challenge_quests).setVisible(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.daily_challenge_quests_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveQuests();
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_pick_daily_challenge_quests, R.string.help_dialog_pick_daily_challenge_quests_title, "pick_daily_challenge_quests").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.add_quest)
    public void onAddQuest(View view) {
        eventBus.post(new AddQuestButtonTappedEvent(EventSource.PICK_DAILY_CHALLENGE_QUESTS));
        startActivity(new Intent(this, AddQuestActivity.class));
    }

    private void saveQuests() {
        List<BaseQuest> selectedBaseQuests = pickQuestsAdapter.getSelectedBaseQuests();
        eventBus.post(new DailyChallengeQuestsSelectedEvent(selectedBaseQuests.size()));
        if (selectedBaseQuests.size() > Constants.DAILY_CHALLENGE_QUEST_COUNT) {
            Toast.makeText(this, R.string.pick_max_3_miq, Toast.LENGTH_LONG).show();
            return;
        }

        List<Quest> questsToSave = new ArrayList<>();

        for (Quest q : previouslySelectedQuests) {
            if (!selectedBaseQuests.contains(q)) {
                q.setPriority(null);
                questsToSave.add(q);
            }
        }

        List<Quest> selectedQuests = new ArrayList<>();
        for (BaseQuest bq : selectedBaseQuests) {
            Quest q = (Quest) bq;
            q.setPriority(Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY);
            selectedQuests.add(q);
        }
        questsToSave.addAll(selectedQuests);

        questPersistenceService.save(questsToSave);

        if (!selectedBaseQuests.isEmpty()) {
            Toast.makeText(this, R.string.miq_saved, Toast.LENGTH_LONG).show();
        }

        finish();
    }

    @Override
    public void onDataChanged(List<Quest> quests) {
        previouslySelectedQuests.clear();
        List<PickQuestViewModel> viewModels = new ArrayList<>();
        for (Quest q : quests) {
            if (q.shouldBeDoneMultipleTimesPerDay()) {
                continue;
            }
            PickQuestViewModel vm = new PickQuestViewModel(q, q.getName());
            if (q.getPriority() == Quest.PRIORITY_MOST_IMPORTANT_FOR_DAY) {
                vm.select();
                previouslySelectedQuests.add(q);
            }
            if (q.isCompleted()) {
                vm.markCompleted();
            }
            viewModels.add(vm);
        }

        pickQuestsAdapter.setViewModels(viewModels);
    }
}
