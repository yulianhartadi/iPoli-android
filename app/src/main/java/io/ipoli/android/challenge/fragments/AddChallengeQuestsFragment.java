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

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
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
import io.ipoli.android.challenge.events.NewChallengeQuestsPickedEvent;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/22/16.
 */
public class AddChallengeQuestsFragment extends BaseFragment {

    public static final int MIN_FILTER_QUERY_LEN = 2;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    ChallengePersistenceService challengePersistenceService;

    @BindView(R.id.root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.result_list)
    EmptyStateRecyclerView questList;

    private ChallengePickQuestListAdapter adapter;

    private List<Quest> selectedQuests = new ArrayList<>();
    private List<RepeatingQuest> selectedRepeatingQuests = new ArrayList<>();
    private List<PickQuestViewModel> allViewModels = new ArrayList<>();
    private SearchView searchView;

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

        adapter = new ChallengePickQuestListAdapter(getContext(), eventBus, new ArrayList<>(), true);
        questList.setAdapter(adapter);

        challengePersistenceService.listenForAllQuestsAndRepeatingQuests(result -> {

            allViewModels = new ArrayList<>();
            for (Quest q : result.second) {
                allViewModels.add(new PickQuestViewModel(q, q.getName(), q.getStartDate(), isSelected(q), false));
            }
            for (RepeatingQuest rq : result.first) {
                allViewModels.add(new PickQuestViewModel(rq, rq.getName(), rq.getRecurrence().getDtstartDate(), isSelected(rq), true));
            }

            String searchQuery = searchView != null ? searchView.getQuery().toString() : "";
            updateAdapter(searchQuery);
        });

        return view;
    }

    private void updateAdapter(String query) {
        filter(query, vms -> adapter.setViewModels(vms));
    }

    @Override
    public void onDestroyView() {
        challengePersistenceService.removeAllListeners();
        unbinder.unbind();
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

        List<PickQuestViewModel> viewModels = new ArrayList<>();
        for (PickQuestViewModel vm : allViewModels) {
            if (vm.getText().toLowerCase().contains(query.toLowerCase())) {
                viewModels.add(vm);
            }
        }

        Collections.sort(viewModels, (vm1, vm2) -> {
            LocalDate d1 = vm1.getStartDate();
            LocalDate d2 = vm2.getStartDate();
            if (d1 == null && d2 == null) {
                return -1;
            }

            if (d1 == null) {
                return 1;
            }

            if (d2 == null) {
                return -1;
            }

            if (d2.isAfter(d1)) {
                return 1;
            }

            if (d1.isAfter(d2)) {
                return -1;
            }

            return 0;
        });
        filterListener.onFilterCompleted(viewModels);
    }

    private boolean isSelected(Quest quest) {
        for (Quest q : selectedQuests) {
            if (q.getId().equals(quest.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSelected(RepeatingQuest repeatingQuest) {
        for (RepeatingQuest rq : selectedRepeatingQuests) {
            if (rq.getId().equals(repeatingQuest.getId())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_challenge_wizard_quests_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (StringUtils.isEmpty(newText)) {
                    updateAdapter("");
                    return true;
                }

                if (newText.trim().length() < MIN_FILTER_QUERY_LEN) {
                    return true;
                }
                updateAdapter(newText.trim());
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                List<Quest> quests = new ArrayList<>();
                List<RepeatingQuest> repeatingQuests = new ArrayList<>();
                for (PickQuestViewModel vm : allViewModels) {
                    if (vm.isSelected()) {
                        BaseQuest bq = vm.getBaseQuest();
                        if (bq instanceof Quest) {
                            quests.add((Quest) bq);
                        } else {
                            repeatingQuests.add((RepeatingQuest) bq);
                        }
                    }
                }
                postEvent(new NewChallengeQuestsPickedEvent(quests, repeatingQuests));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setSelectedQuests(List<Quest> quests, List<RepeatingQuest> repeatingQuests) {
        selectedQuests = quests;
        selectedRepeatingQuests = repeatingQuests;
        String searchQuery = searchView != null ? searchView.getQuery().toString() : "";
        updateAdapter(searchQuery);
    }

    public interface FilterListener {
        void onFilterCompleted(List<PickQuestViewModel> viewModels);
    }
}
