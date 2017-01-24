package io.ipoli.android.challenge.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.tutorial.PickQuestViewModel;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.challenge.adapters.ChallengePickQuestListAdapter;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/22/16.
 */
public class PickChallengeQuestsFragment extends BaseFragment {

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

    @BindView(R.id.result_list)
    EmptyStateRecyclerView questList;

    private ChallengePickQuestListAdapter adapter;

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_challenge_quests, container, false);
        unbinder = ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);
        questList.setEmptyView(rootContainer, R.string.empty_daily_challenge_quests_text, R.drawable.ic_compass_grey_24dp);
        filter("", viewModels -> {
            adapter = new ChallengePickQuestListAdapter(getContext(), eventBus, viewModels, true);
            questList.setAdapter(adapter);
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        challengePersistenceService.removeAllListeners();
        questPersistenceService.removeAllListeners();
        repeatingQuestPersistenceService.removeAllListeners();
        super.onDestroyView();
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
        questPersistenceService.findIncompleteNotRepeating(query.trim(), quests -> {
            repeatingQuestPersistenceService.findActive(query.trim(), repeatingQuests -> {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pick_challenge_quests_menu, menu);

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
//                saveQuests();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*private void saveQuests() {
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
            questPersistenceService.update(quests);
        }
        if (!repeatingQuests.isEmpty()) {
            repeatingQuestPersistenceService.updateChallengeId(repeatingQuests);
        }
//        finish();
    }*/

    public interface FilterListener {
        void onFilterCompleted(List<PickQuestViewModel> viewModels);
    }
}
