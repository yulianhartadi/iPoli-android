package io.ipoli.android.challenge.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/22/16.
 */
public class PickChallengeQuestsActivity extends BaseActivity {

    @Inject
    Bus eventBus;

    QuestPersistenceService questPersistenceService;

    @BindView(R.id.root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.result_list)
    EmptyStateRecyclerView questList;

//    private BasePickQuestAdapter<Quest> pickQuestsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_challenge_quests);
        ButterKnife.bind(this);
        appComponent().inject(this);
        questPersistenceService = new RealmQuestPersistenceService(eventBus, getRealm());

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

//        pickQuestsAdapter = new PickTutorialQuestsAdapter(this, eventBus, new ArrayList<>());
//        questList.setAdapter(pickQuestsAdapter);
//        questList.setEmptyView(rootContainer, R.string.empty_daily_challenge_quests_text, R.drawable.ic_compass_grey_24dp);
//        questPersistenceService.findAllIncompleteOrMostImportantForDate(LocalDate.now(), this);
    }

    @Override
    protected void onDestroy() {
        questPersistenceService.removeAllListeners();
        super.onDestroy();
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
//                saveQuests();
                return true;
            case R.id.action_help:
                HelpDialog.newInstance(R.layout.fragment_help_dialog_pick_daily_challenge_quests, R.string.help_dialog_pick_daily_challenge_quests_title, "pick_daily_challenge_quests").show(getSupportFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
