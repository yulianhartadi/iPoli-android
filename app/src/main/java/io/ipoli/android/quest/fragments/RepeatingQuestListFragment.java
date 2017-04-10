package io.ipoli.android.quest.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.ui.FabMenuView;
import io.ipoli.android.app.ui.events.FabMenuTappedEvent;
import io.ipoli.android.quest.activities.RepeatingQuestActivity;
import io.ipoli.android.quest.adapters.RepeatingQuestListAdapter;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.ShowRepeatingQuestEvent;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.viewmodels.RepeatingQuestViewModel;

public class RepeatingQuestListFragment extends BaseFragment implements OnDataChangedListener<List<RepeatingQuest>> {

    @Inject
    Bus eventBus;

    @BindView(R.id.root_container)
    CoordinatorLayout rootLayout;

    @BindView(R.id.quest_list)
    EmptyStateRecyclerView questList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab_menu)
    FabMenuView fabMenu;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;
    private Unbinder unbinder;
    private RepeatingQuestListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_repeating_quest_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.title_fragment_repeating_quests);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        RepeatingQuestListAdapter repeatingQuestListAdapter = new RepeatingQuestListAdapter(getContext(), new ArrayList<>(), eventBus);
        questList.setEmptyView(rootLayout, R.string.empty_repeating_quests_text, R.drawable.ic_repeat_grey_24dp);
        questList.setAdapter(repeatingQuestListAdapter);
        repeatingQuestPersistenceService.listenForAllNonAllDayActiveRepeatingQuests(this);

        fabMenu.addFabClickListener(name -> eventBus.post(new FabMenuTappedEvent(name, EventSource.REPEATING_QUESTS)));
        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_repeating_quests, R.string.help_dialog_repeating_quests_title, "repeating_quests").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onDestroyView() {
        questPersistenceService.removeAllListeners();
        repeatingQuestPersistenceService.removeAllListeners();
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

    @Override
    public void onDataChanged(List<RepeatingQuest> quests) {
        if (quests.isEmpty()) {
            adapter = new RepeatingQuestListAdapter(getContext(), new ArrayList<>(), eventBus);
            questList.setAdapter(adapter);
            return;
        }
        List<RepeatingQuestViewModel> viewModels = new ArrayList<>();
        for (RepeatingQuest rq : quests) {
            viewModels.add(new RepeatingQuestViewModel(rq));
        }
        adapter = new RepeatingQuestListAdapter(getContext(), viewModels, eventBus);
        questList.setAdapter(adapter);
    }

    @Subscribe
    public void onShowRepeatingQuestEvent(ShowRepeatingQuestEvent e) {
        Intent i = new Intent(getActivity(), RepeatingQuestActivity.class);
        i.putExtra(Constants.REPEATING_QUEST_ID_EXTRA_KEY, e.repeatingQuest.getId());
        startActivity(i);
    }
}