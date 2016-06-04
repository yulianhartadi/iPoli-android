package io.ipoli.android.quest.fragments;


import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.List;

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
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.ui.DividerItemDecoration;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.quest.adapters.InboxAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestedEvent;
import io.ipoli.android.quest.events.QuestCompletedEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import rx.Observable;

public class InboxFragment extends BaseFragment {

    @Inject
    Bus eventBus;

    @BindView(R.id.root_container)
    CoordinatorLayout rootLayout;

    @BindView(R.id.quest_list)
    EmptyStateRecyclerView questList;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    QuestPersistenceService questPersistenceService;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.title_fragment_inbox);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_inbox, R.string.help_dialog_inbox_title, "inbox").show(getActivity().getSupportFragmentManager());
    }

    private void updateQuests() {
        getAllUnplanned().subscribe(this::initQuestList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initQuestList(List<Quest> quests) {
        InboxAdapter inboxAdapter = new InboxAdapter(getContext(), quests, eventBus);
        questList.setEmptyView(rootLayout, R.string.empty_inbox_text, R.drawable.ic_inbox_grey_24dp);
        questList.setAdapter(inboxAdapter);
        questList.addItemDecoration(new DividerItemDecoration(getContext()));
    }

    private Observable<List<Quest>> getAllUnplanned() {
        return questPersistenceService.findAllUnplanned().compose(bindToLifecycle());
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
    public void onQuestDeleteRequest(final DeleteQuestRequestEvent e) {
        eventBus.post(new DeleteQuestRequestedEvent(e.quest, EventSource.INBOX));
        questPersistenceService.delete(e.quest).compose(bindToLifecycle()).subscribe(questId -> {
            Snackbar
                    .make(rootLayout,
                            R.string.quest_removed,
                            Snackbar.LENGTH_SHORT)
                    .show();
            updateQuests();
        });
    }

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        Quest q = e.quest;
        q.setEndDate(new Date());
        questPersistenceService.save(q).compose(bindToLifecycle()).subscribe(quest -> {
            Toast.makeText(getContext(), "Quest scheduled for today", Toast.LENGTH_SHORT).show();
            updateQuests();
        });
    }

    @Subscribe
    public void onQuestCompleted(QuestCompletedEvent e) {
        updateQuests();
    }


    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        updateQuests();
    }

}
