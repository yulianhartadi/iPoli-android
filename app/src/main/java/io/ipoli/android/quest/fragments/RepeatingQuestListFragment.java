package io.ipoli.android.quest.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.DateTime;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.activities.AddQuestActivity;
import io.ipoli.android.quest.adapters.RepeatingQuestListAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.events.DeleteRepeatingQuestRequestEvent;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmRepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.quest.viewmodels.RepeatingQuestViewModel;
import rx.Observable;

public class RepeatingQuestListFragment extends BaseFragment implements OnDatabaseChangedListener<RepeatingQuest> {

    @Inject
    Bus eventBus;

    @BindView(R.id.root_container)
    CoordinatorLayout rootLayout;

    @BindView(R.id.quest_list)
    EmptyStateRecyclerView questList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    QuestPersistenceService questPersistenceService;

    private RepeatingQuestListAdapter repeatingQuestListAdapter;
    private Unbinder unbinder;

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

        repeatingQuestListAdapter = new RepeatingQuestListAdapter(getContext(), new ArrayList<>(), eventBus);
        questList.setAdapter(repeatingQuestListAdapter);
        questList.setEmptyView(rootLayout, R.string.empty_repeating_quests_text, R.drawable.ic_autorenew_grey_24dp);
        questPersistenceService = new RealmQuestPersistenceService(eventBus, getRealm());
        repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, getRealm());
        repeatingQuestPersistenceService.findAllNonAllDayActiveRepeatingQuests(this);
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
        questPersistenceService.close();
        repeatingQuestPersistenceService.close();
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

    @Nullable
    private RepeatingQuestViewModel createViewModel(RepeatingQuest rq) {
        try {
            Recurrence recurrence = rq.getRecurrence();
            Recur recur = new Recur(recurrence.getRrule());

            LocalDate from, to;
            if (recur.getFrequency().equals(Recur.MONTHLY)) {
                from = LocalDate.now().dayOfMonth().withMinimumValue();
                to = LocalDate.now().dayOfMonth().withMaximumValue();
            } else {
                from = LocalDate.now().dayOfWeek().withMinimumValue();
                to = LocalDate.now().dayOfWeek().withMaximumValue();
            }

            int completedCount = (int) questPersistenceService.countCompletedQuests(rq, from, to);
            Date todayStartOfDay = DateUtils.toStartOfDayUTC(LocalDate.now());

            // we subtract 1 ms because getNextDate excludes equal dates
            DateTime seed = new DateTime(recurrence.getDtstart().getTime() - 1);

            DateTime startDate = new DateTime(todayStartOfDay.getTime());
            java.util.Date nextDate = recur.getNextDate(seed, startDate);

            if (DateUtils.isTodayUTC(nextDate)) {
                int completedForToday = (int) questPersistenceService.countCompletedQuests(rq, LocalDate.now(), LocalDate.now());
                int timesPerDay = recurrence.getTimesPerDay();
                if (completedForToday >= timesPerDay) {
                    Date tomorrowStartOfDay = DateUtils.toStartOfDayUTC(LocalDate.now().plusDays(1));
                    nextDate = recur.getNextDate(seed, new DateTime(tomorrowStartOfDay.getTime()));
                    if (recurrence.getDtend() != null && nextDate.after(recurrence.getDtend())) {
                        nextDate = null;
                    }
                }
            }
            return new RepeatingQuestViewModel(rq, completedCount, recur, nextDate);
        } catch (ParseException e) {
            return null;
        }
    }

    @Subscribe
    public void onDeleteRepeatingQuestRequest(final DeleteRepeatingQuestRequestEvent e) {
        final RepeatingQuest repeatingQuest = e.repeatingQuest;
        repeatingQuest.markDeleted();
        markQuestsDeleted(repeatingQuest).flatMap(ignored ->
                repeatingQuestPersistenceService.saveRemoteObject(repeatingQuest)).compose(bindToLifecycle()).subscribe(o -> {
        }, error -> {
        }, () -> {
            Snackbar
                    .make(rootLayout,
                            R.string.repeating_quest_removed,
                            Snackbar.LENGTH_SHORT).show();
        });
    }

    private Observable<List<Quest>> markQuestsDeleted(RepeatingQuest repeatingQuest) {
        List<Quest> quests = questPersistenceService.findAllForRepeatingQuest(repeatingQuest);
        for (Quest q : quests) {
            q.markDeleted();
        }
        return questPersistenceService.saveRemoteObjects(quests);
    }

    @OnClick(R.id.add_repeating_quest)
    public void onAddRepeatingQuest(View view) {
        startActivity(new Intent(getActivity(), AddQuestActivity.class));
    }

    @Override
    public void onDatabaseChanged(List<RepeatingQuest> quests) {
        List<RepeatingQuestViewModel> viewModels = new ArrayList<>();
        for (RepeatingQuest rq : quests) {
            RepeatingQuestViewModel vm = createViewModel(rq);
            if (vm != null) {
                viewModels.add(vm);
            }
        }
        repeatingQuestListAdapter.updateQuests(viewModels);
    }
}
