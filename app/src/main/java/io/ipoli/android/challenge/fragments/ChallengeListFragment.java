package io.ipoli.android.challenge.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.ArrayList;
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
import io.ipoli.android.challenge.activities.EditChallengeActivity;
import io.ipoli.android.challenge.adapters.ChallengeListAdapter;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.persistence.RealmChallengePersistenceService;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class ChallengeListFragment extends BaseFragment implements OnDatabaseChangedListener<Challenge> {

    private Unbinder unbinder;

    @Inject
    Bus eventBus;

    @BindView(R.id.challenge_list)
    EmptyStateRecyclerView challengeList;

    @BindView(R.id.root_container)
    CoordinatorLayout rootLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private ChallengePersistenceService challengePersistenceService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_challenge_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((MainActivity) getActivity()).initToolbar(toolbar, R.string.title_fragment_challenges);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        challengeList.setLayoutManager(layoutManager);
        challengeList.setEmptyView(rootLayout, R.string.empty_text_chanllenge, R.drawable.ic_sword_grey_24dp);

        ChallengeListAdapter adapter = new ChallengeListAdapter(getActivity(), new ArrayList<>(), eventBus);
        challengeList.setAdapter(adapter);

        challengePersistenceService = new RealmChallengePersistenceService(eventBus, getRealm());
        challengePersistenceService.findAllNotCompleted(this);

        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_rewards, R.string.help_dialog_rewards_title, "rewards").show(getActivity().getSupportFragmentManager());
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
    public void onDestroyView() {
        unbinder.unbind();
        challengePersistenceService.close();
        super.onDestroyView();
    }

    @OnClick(R.id.add_challenge)
    public void onAddChallenge(View view) {
        startActivity(new Intent(getContext(), EditChallengeActivity.class));
    }

    @Override
    public void onDatabaseChanged(List<Challenge> results) {
        ChallengeListAdapter adapter = new ChallengeListAdapter(getActivity(), results, eventBus);
        challengeList.setAdapter(adapter);
    }
}