package io.ipoli.android.quest.fragments;


import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.ui.FabMenuView;
import io.ipoli.android.app.ui.events.FabMenuTappedEvent;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.quest.adapters.OverviewAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.viewmodels.QuestViewModel;

public class OverviewFragment extends BaseFragment implements OnDataChangedListener<SortedMap<LocalDate, List<Quest>>> {

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @BindView(R.id.quest_list)
    EmptyStateRecyclerView questList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.root_container)
    CoordinatorLayout rootContainer;

    @BindView(R.id.fab_menu)
    FabMenuView fabMenu;

    @Inject
    QuestPersistenceService questPersistenceService;

    private OverviewAdapter overviewAdapter;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.fragment_overview_title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        overviewAdapter = new OverviewAdapter(getContext(), eventBus);
        questList.setAdapter(overviewAdapter);
        questList.setEmptyView(rootContainer, R.string.empty_overview_text, R.drawable.ic_compass_grey_24dp);
        questPersistenceService.listenForPlannedNonAllDayBetween(LocalDate.now().minusDays(1), LocalDate.now().plusDays(7), this);

        fabMenu.addFabClickListener(name -> eventBus.post(new FabMenuTappedEvent(name, EventSource.OVERVIEW)));
        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_overview, R.string.help_dialog_overview_title, "overview").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onDestroyView() {
        questPersistenceService.removeAllListeners();
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

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        Quest q = e.quest;
        LocalDate scheduledDate = LocalDate.now();
        String toast = getString(R.string.quest_scheduled_for_today);
        if (e.quest.isScheduledForToday()) {
            toast = getString(R.string.quest_scheduled_for_tomorrow);
            scheduledDate = scheduledDate.plusDays(1);
        }
        final String toastMessage = toast;
        q.setScheduledDate(scheduledDate);
        questPersistenceService.save(q);
        Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataChanged(SortedMap<LocalDate, List<Quest>> dateToQuests) {
        SortedMap<LocalDate, List<QuestViewModel>> viewModels = new TreeMap<>();

        for (Map.Entry<LocalDate, List<Quest>> entry : dateToQuests.entrySet()) {
            List<QuestViewModel> vms = new ArrayList<>();
            for (Quest quest : entry.getValue()) {
                vms.add(new QuestViewModel(getContext(), quest, shouldUse24HourFormat()));
            }
            viewModels.put(entry.getKey(), vms);
        }
        overviewAdapter.updateQuests(viewModels);
    }
}
