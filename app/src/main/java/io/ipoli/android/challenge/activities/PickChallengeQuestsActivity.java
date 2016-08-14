package io.ipoli.android.challenge.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.NoNetworkConnectionEvent;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.adapters.ChallengePickQuestListAdapter;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.ui.events.QuestsPickedForChallengeEvent;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.tutorial.PickQuestViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/22/16.
 */
public class PickChallengeQuestsActivity extends BaseActivity {

    public static final int MIN_FILTER_QUERY_LEN = 3;

    @Inject
    Bus eventBus;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @BindView(R.id.root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.result_list)
    EmptyStateRecyclerView questList;

    private ChallengePickQuestListAdapter adapter;
    private Challenge challenge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getIntent() == null || TextUtils.isEmpty(getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY))) {
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_challenge_quests);
        ButterKnife.bind(this);
        appComponent().inject(this);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        challengePersistenceService.findById(getIntent().getStringExtra(Constants.CHALLENGE_ID_EXTRA_KEY), challenge -> {
            this.challenge = challenge;
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            questList.setLayoutManager(layoutManager);
            questList.setEmptyView(rootContainer, R.string.empty_daily_challenge_quests_text, R.drawable.ic_compass_grey_24dp);
            filter("", viewModels -> {
                adapter = new ChallengePickQuestListAdapter(PickChallengeQuestsActivity.this, eventBus, viewModels, true);
                questList.setAdapter(adapter);
            });

            eventBus.post(new ScreenShownEvent(EventSource.PICK_CHALLENGE_QUESTS));
        });
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

    private void filter(String query, FilterListener filterListener) {
        questPersistenceService.findIncompleteNotRepeatingNotForChallenge(query.trim(), challenge.getId(), quests -> {
            repeatingQuestPersistenceService.findActiveNotForChallenge(query.trim(), challenge, repeatingQuests -> {
                List<PickQuestViewModel> viewModels = new ArrayList<>();
                for (Quest q : quests) {
                    viewModels.add(new PickQuestViewModel(q, q.getName(), q.getStartDate(), false));
                }
                for (RepeatingQuest rq : repeatingQuests) {
                    viewModels.add(new PickQuestViewModel(rq, rq.getName(), rq.getRecurrence().getDtstartDate(), true));
                }

                Collections.sort(viewModels, (vm1, vm2) -> {
                    Date d1 = vm1.getStartDate();
                    Date d2 = vm2.getStartDate();
                    if (d1 == null && d2 == null) {
                        return -1;
                    }

                    if (d1 == null) {
                        return 1;
                    }

                    if (d2 == null) {
                        return -1;
                    }

                    if (d2.after(d1)) {
                        return 1;
                    }

                    if (d1.after(d2)) {
                        return -1;
                    }

                    return 0;
                });
                filterListener.onFilterCompleted(viewModels);
            });
        });
    }

    @Override
    protected void onStop() {
        challengePersistenceService.removeAllListeners();
        questPersistenceService.removeAllListeners();
        repeatingQuestPersistenceService.removeAllListeners();
        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_pick_daily_challenge_quests).setVisible(false);
        menu.findItem(R.id.action_help).setVisible(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.pick_challenge_quests_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (StringUtils.isEmpty(newText)) {
                    filter("", viewModels -> adapter.setViewModels(viewModels));
                    return true;
                }

                if (newText.trim().length() < MIN_FILTER_QUERY_LEN) {
                    return true;
                }
                filter(newText.trim(), viewModels -> adapter.setViewModels(viewModels));
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveQuests();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveQuests() {
        List<BaseQuest> baseQuests = adapter.getSelectedBaseQuests();
        if (baseQuests.isEmpty()) {
            return;
        }

        eventBus.post(new QuestsPickedForChallengeEvent(baseQuests.size()));

        List<Quest> quests = new ArrayList<>();
        List<RepeatingQuest> repeatingQuests = new ArrayList<>();
        for (BaseQuest bq : baseQuests) {
            if (bq instanceof Quest) {
                Quest q = (Quest) bq;
                q.setChallengeId(challenge.getId());
                quests.add(q);
            } else {
                RepeatingQuest rq = (RepeatingQuest) bq;
                rq.setChallengeId(challenge.getId());
                repeatingQuests.add(rq);
            }
        }

        if (!quests.isEmpty()) {
            questPersistenceService.save(quests);
        }
        if (!repeatingQuests.isEmpty()) {
            repeatingQuestPersistenceService.save(repeatingQuests);
        }
        finish();
    }

    public interface FilterListener {
        void onFilterCompleted(List<PickQuestViewModel> viewModels);
    }

    @Subscribe
    public void onNoNetworkConnection(NoNetworkConnectionEvent e) {
        showNoInternetActivity();
    }
}
