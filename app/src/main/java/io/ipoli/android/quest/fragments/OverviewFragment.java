package io.ipoli.android.quest.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.adapters.OverviewAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestedEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.viewmodels.QuestViewModel;

public class OverviewFragment extends BaseFragment {
    @Inject
    Bus eventBus;

    @BindView(R.id.root_container)
    RelativeLayout rootLayout;

    @BindView(R.id.quest_list)
    EmptyStateRecyclerView questList;

    CoordinatorLayout rootContainer;

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

        rootContainer = (CoordinatorLayout) getActivity().findViewById(R.id.root_container);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        overviewAdapter = new OverviewAdapter(getContext(), new ArrayList<>(), eventBus);
        questList.setAdapter(overviewAdapter);
        questList.setEmptyView(rootLayout, R.string.empty_overview_text, R.drawable.ic_compass_grey_24dp);


        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overview_menu, menu);
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
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        updateQuests();
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
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
        q.setEndDate(endDate);
        questPersistenceService.save(q).compose(bindToLifecycle()).subscribe(quest -> {
            Toast.makeText(getContext(), toastMessage, Toast.LENGTH_SHORT).show();
            updateQuests();
        });
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        updateQuests();
    }

    @Subscribe
    public void onQuestDeleteRequest(final DeleteQuestRequestEvent e) {
        eventBus.post(new DeleteQuestRequestedEvent(e.quest, EventSource.INBOX));
        questPersistenceService.delete(e.quest).compose(bindToLifecycle()).subscribe(questId -> {
            Snackbar
                    .make(rootContainer,
                            R.string.quest_removed,
                            Snackbar.LENGTH_SHORT)
                    .show();
            updateQuests();
        });
    }

    @OnClick(R.id.add_quest)
    public void onAddQuest(View view) {
        startActivity(new Intent(getActivity(), AddQuestActivity.class));
    }

    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        updateQuests();
    }

    private void updateQuests() {
        questPersistenceService.findPlannedNonAllDayBetween(new LocalDate(), new LocalDate().plusDays(7)).compose(bindToLifecycle()).subscribe(quests -> {

            List<QuestViewModel> viewModels = new ArrayList<>();
            List<Quest> recurrent = new ArrayList<>();
            for (Quest q : quests) {
                if (q.isScheduledForToday() && q.getRepeatingQuest() != null && !TextUtils.isEmpty(q.getRepeatingQuest().getRecurrence().getDailyRrule())) {
                    recurrent.add(q);
                } else {
                    viewModels.add(new QuestViewModel(getContext(), q, 1, 1));
                }
            }

            Map<String, List<Quest>> map = new HashMap<>();
            for (Quest q : recurrent) {
                String key = q.getRepeatingQuest().getId();
                if (map.get(key) == null) {
                    map.put(key, new ArrayList<>());
                }
                map.get(key).add(q);
            }

            for (String key : map.keySet()) {
                Quest q = map.get(key).get(0);
                RepeatingQuest rq = q.getRepeatingQuest();
                try {
                    Recur recur = new Recur(rq.getRecurrence().getDailyRrule());
                    int repeatCount = recur.getCount();
                    int remainingCount = map.get(key).size();
                    viewModels.add(new QuestViewModel(getContext(), q, repeatCount, remainingCount));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Collections.sort(viewModels, new Comparator<QuestViewModel>() {
                @Override
                public int compare(QuestViewModel lhs, QuestViewModel rhs) {
                    Quest lq = lhs.getQuest();
                    Quest rq = rhs.getQuest();
                    if (lq.getEndDate().before(rq.getEndDate())) {
                        return -1;
                    }
                    if (lq.getEndDate().after(rq.getEndDate())) {
                        return 1;
                    }
                    return lhs.getQuest().getStartMinute() > rhs.getQuest().getStartMinute() ? 1 : -1;
                }
            });
            overviewAdapter.updateQuests(viewModels);
        });
    }
}
