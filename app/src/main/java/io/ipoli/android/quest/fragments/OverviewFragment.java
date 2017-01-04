package io.ipoli.android.quest.fragments;


import android.content.Intent;
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

import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.adapters.OverviewAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.persistence.OnDataChangedListener;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.viewmodels.QuestViewModel;

public class OverviewFragment extends BaseFragment implements OnDataChangedListener<List<Quest>> {

    @Inject
    Bus eventBus;

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
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.fragment_overview_title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        overviewAdapter = new OverviewAdapter(getContext(), new ArrayList<>(), eventBus);
        questList.setAdapter(overviewAdapter);
        questList.setEmptyView(rootContainer, R.string.empty_overview_text, R.drawable.ic_compass_grey_24dp);
        questPersistenceService.listenForPlannedNonAllDayBetween(new LocalDate(), new LocalDate().plusDays(7), this);

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
    public void onShowQuestEvent(ShowQuestEvent e) {
        Intent i = new Intent(getActivity(), QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivity(i);
    }

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        Quest q = e.quest;
        Date endDate = new Date();
        String toast = getString(R.string.quest_scheduled_for_today);
        if (e.quest.isScheduledForToday()) {
            toast = getString(R.string.quest_scheduled_for_tomorrow);
            endDate = DateUtils.getTomorrow();
        }
        final String toastMessage = toast;
        q.setEndDateFromLocal(endDate);
        questPersistenceService.update(q);
        Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataChanged(List<Quest> quests) {
        List<QuestViewModel> viewModels = new ArrayList<>();
        for (Quest q : quests) {
            if (q.isScheduledForToday() && q.shouldBeDoneMultipleTimesPerDay()) {
                viewModels.add(new QuestViewModel(getContext(), q));
            } else if (q.isScheduledForToday() || !q.shouldBeDoneMultipleTimesPerDay()) {
                viewModels.add(new QuestViewModel(getContext(), q));
            }
        }

        Collections.sort(viewModels, new Comparator<QuestViewModel>() {
            @Override
            public int compare(QuestViewModel qvm1, QuestViewModel qvm2) {
                Quest q1 = qvm1.getQuest();
                Quest q2 = qvm2.getQuest();
                if (q1.getEndDate().before(q2.getEndDate())) {
                    return -1;
                }
                if (q1.getEndDate().after(q2.getEndDate())) {
                    return 1;
                }
                if (qvm1.getQuest().getStartMinute() > qvm2.getQuest().getStartMinute()) {
                    return 1;
                }
                if (qvm1.getQuest().getStartMinute() < qvm2.getQuest().getStartMinute()) {
                    return -1;
                }
                return 0;
            }
        });
        overviewAdapter.updateQuests(viewModels);
    }
}
