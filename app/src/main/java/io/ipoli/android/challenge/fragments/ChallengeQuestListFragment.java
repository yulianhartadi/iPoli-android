package io.ipoli.android.challenge.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.EmptyStateRecyclerView;
import io.ipoli.android.challenge.adapters.ChallengeQuestListAdapter;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.persistence.RealmChallengePersistenceService;
import io.ipoli.android.challenge.viewmodels.ChallengeQuestViewModel;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmRepeatingQuestPersistenceService;
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

    private String challengeId;
    private ChallengePersistenceService challengePersistenceService;
    private QuestPersistenceService questPersistenceService;
    private RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    public static ChallengeQuestListFragment newInstance(String challengeId) {
        ChallengeQuestListFragment fragment = new ChallengeQuestListFragment();
        Bundle args = new Bundle();
        args.putString(CHALLENGE_ID, challengeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            challengeId = getArguments().getString(CHALLENGE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_challenge_quest_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);
        questList.setEmptyView(rootLayout, R.string.empty_inbox_text, R.drawable.ic_inbox_grey_24dp);

        challengePersistenceService = new RealmChallengePersistenceService(eventBus, getRealm());
        questPersistenceService = new RealmQuestPersistenceService(eventBus, getRealm());
        repeatingQuestPersistenceService = new RealmRepeatingQuestPersistenceService(eventBus, getRealm());

        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
//        HelpDialog.newInstance(R.layout.fragment_help_dialog_rewards, R.string.help_dialog_rewards_title, "rewards").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
//        List<Quest> quests = questPersistenceService.findNotCompletedNotRepeatingForChallenge(challengeId);
//        List<RepeatingQuest> repeatingQuests = repeatingQuestPersistenceService.findActiveForChallenge(challengeId);
        List<ChallengeQuestViewModel> viewModels = new ArrayList<>();
//        for(Quest q : quests) {
//            viewModels.add(new ChallengeQuestViewModel(q.getId(), q.getName(), Quest.getCategory(q), false));
//        }
//        for(RepeatingQuest rq : repeatingQuests) {
//            viewModels.add(new ChallengeQuestViewModel(rq.getId(), rq.getName(), RepeatingQuest.getCategory(rq), true));
//        }

        adapter = new ChallengeQuestListAdapter(getContext(), viewModels, eventBus);
        questList.setAdapter(adapter);
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
        challengePersistenceService.removeAllListeners();
        super.onDestroyView();
    }
}
