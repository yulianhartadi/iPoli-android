package io.ipoli.android.quest.fragments;


import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.ui.DividerItemDecoration;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.quest.adapters.InboxAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestedEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import rx.Observable;

public class InboxFragment extends RxFragment {

    @Inject
    Bus eventBus;

    CoordinatorLayout rootContainer;

    @BindView(R.id.quest_list)
    RecyclerView questList;

    @Inject
    QuestPersistenceService questPersistenceService;

    private InboxAdapter inboxAdapter;
    private Unbinder unbinder;
    private DeleteQuestRequestEvent currentDeleteQuestEvent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        rootContainer = (CoordinatorLayout) getActivity().findViewById(R.id.root_container);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_help) {
            HelpDialog.newInstance(R.layout.fragment_help_dialog_inbox, R.string.help_dialog_inbox_title, "inbox").show(getActivity().getSupportFragmentManager());
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        inboxAdapter = new InboxAdapter(getContext(), quests, eventBus);
        questList.setAdapter(inboxAdapter);
        questList.addItemDecoration(new DividerItemDecoration(getContext()));

        ItemTouchCallback touchCallback = new ItemTouchCallback(inboxAdapter, ItemTouchHelper.START | ItemTouchHelper.END);
        touchCallback.setLongPressDragEnabled(false);
        touchCallback.setSwipeStartDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.md_red_500)));
        touchCallback.setSwipeEndDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.md_blue_500)));
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);
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
        currentDeleteQuestEvent = e;
        final Snackbar snackbar = Snackbar
                .make(rootContainer,
                        R.string.quest_removed,
                        Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, view -> {
                    inboxAdapter.addQuest(currentDeleteQuestEvent.position, currentDeleteQuestEvent.quest);
                    questPersistenceService.saveRemoteObject(currentDeleteQuestEvent.quest).compose(bindToLifecycle()).subscribe();
                    eventBus.post(new UndoDeleteQuestEvent(currentDeleteQuestEvent.quest, EventSource.INBOX));
                });

        questPersistenceService.delete(e.quest).compose(bindToLifecycle()).subscribe(questId -> {
            snackbar.show();
        });
    }

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        Quest q = e.quest;
        q.setEndDate(new Date());
        questPersistenceService.save(q).compose(bindToLifecycle()).subscribe(quest -> {
            Toast.makeText(getContext(), "Quest scheduled for today", Toast.LENGTH_SHORT).show();
        });

    }

    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        updateQuests();
    }

}
