package io.ipoli.android.challenge.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
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
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ScreenShownEvent;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.challenge.activities.ChallengeActivity;
import io.ipoli.android.challenge.activities.PickChallengeQuestsActivity;
import io.ipoli.android.challenge.adapters.ChallengeQuestListAdapter;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.events.RemoveBaseQuestFromChallengeEvent;
import io.ipoli.android.challenge.viewmodels.ChallengeQuestViewModel;
import io.ipoli.android.quest.data.BaseQuest;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class ChallengeQuestListFragment extends BaseFragment {

    private static final String CHALLENGE_ID = "challenge_id";
    private Unbinder unbinder;

    @Inject
    Bus eventBus;

    @BindView(R.id.root_container)
    ViewGroup rootLayout;

    @BindView(R.id.quest_list)
    EmptyStateRecyclerView questList;

    private ChallengeQuestListAdapter adapter;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    private List<Quest> quests = new ArrayList<>();
    private List<RepeatingQuest> repeatingQuests = new ArrayList<>();
    private Challenge challenge;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_challenge_quest_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        challenge = ((ChallengeActivity) getActivity()).getChallenge();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);
        questList.setEmptyView(rootLayout, R.string.empty_daily_challenge_quests_text, R.drawable.ic_compass_grey_24dp);
        adapter = new ChallengeQuestListAdapter(getContext(), new ArrayList<>(), eventBus);
        questList.setAdapter(adapter);

        questPersistenceService.findIncompleteNotRepeatingForChallenge(challenge, results -> {
            quests = results;
            onQuestListUpdated();
        });
        repeatingQuestPersistenceService.findActiveForChallenge(challenge, results -> {
            repeatingQuests = results;
            onQuestListUpdated();
        });

        eventBus.post(new ScreenShownEvent(EventSource.CHALLENGE_QUEST_LIST));

        return view;
    }

    private void onQuestListUpdated() {
        List<ChallengeQuestViewModel> viewModels = new ArrayList<>();
        for (Quest q : quests) {
            viewModels.add(new ChallengeQuestViewModel(q, false));
        }
        for (RepeatingQuest rq : repeatingQuests) {
            viewModels.add(new ChallengeQuestViewModel(rq, true));
        }
        adapter.setViewModels(viewModels);
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_challenge, R.string.help_dialog_challenge_title, "challenge").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @OnClick(R.id.add_quests)
    public void onAddQuestsClick(View v) {
        Intent intent = new Intent(getContext(), PickChallengeQuestsActivity.class);
        intent.putExtra(Constants.CHALLENGE_ID_EXTRA_KEY, challenge.getId());
        startActivity(intent);
    }

    @Subscribe
    public void onRemoveBaseQuestFromChallenge(RemoveBaseQuestFromChallengeEvent e) {
        BaseQuest bq = e.baseQuest;
        if (bq instanceof Quest) {
            Quest q = (Quest) bq;
            q.setChallenge(null);
            questPersistenceService.save(q);
        } else {
            RepeatingQuest rq = (RepeatingQuest) bq;
            rq.setChallenge(null);
            repeatingQuestPersistenceService.save(rq);
        }
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        questPersistenceService.removeAllListeners();
        repeatingQuestPersistenceService.removeAllListeners();
        super.onDestroyView();
    }
}
