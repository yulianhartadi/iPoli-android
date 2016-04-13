package io.ipoli.android.quest.fragments;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.ui.DividerItemDecoration;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.activities.EditQuestActivity;
import io.ipoli.android.quest.adapters.InboxAdapter;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.DeleteQuestEvent;
import io.ipoli.android.quest.events.DeleteQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.events.UndoDeleteQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import rx.Observable;

public class InboxFragment extends Fragment {

    @Inject
    Bus eventBus;

    CoordinatorLayout rootContainer;

    @Bind(R.id.quest_list)
    RecyclerView questList;

    @Inject
    QuestPersistenceService questPersistenceService;

    private InboxAdapter inboxAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_inbox));

        rootContainer = (CoordinatorLayout) getActivity().findViewById(R.id.root_container);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        return view;
    }

    private void updateQuests() {
        getAllUnplanned().subscribe(quests -> {
            initQuestList(quests);
            addTutorialItem();
        });
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void addTutorialItem() {
//        Tutorial.getInstance(this).addItem(
//                new TutorialItem.Builder(this)
//                        .setState(Tutorial.State.TUTORIAL_INBOX_SWIPE)
//                        .setTarget(questList)
//                        .setFocusType(Focus.ALL)
//                        .setTargetPadding(-30)
//                        .enableDotAnimation(false)
//                        .dismissOnTouch(true)
//                        .build());
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
        return questPersistenceService.findAllUnplanned();
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
        final Snackbar snackbar = Snackbar
                .make(rootContainer,
                        R.string.quest_removed,
                        Snackbar.LENGTH_LONG);

        final Quest quest = e.quest;

        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                QuestNotificationScheduler.stopAll(quest.getId(), getContext());
                questPersistenceService.delete(quest);
                eventBus.post(new DeleteQuestEvent(quest));
            }
        });

        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inboxAdapter.addQuest(e.position, quest);
                snackbar.setCallback(null);
                eventBus.post(new UndoDeleteQuestEvent(quest));
            }
        });

        snackbar.show();
    }

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        Quest q = e.quest;
        q.setEndDate(new Date());
        questPersistenceService.save(q);
        Toast.makeText(getContext(), "Quest scheduled for today", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEditQuestRequest(EditQuestRequestEvent e) {
        Intent i = new Intent(getContext(), EditQuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, e.quest.getId());
        startActivity(i);
    }

    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        updateQuests();
    }

}
