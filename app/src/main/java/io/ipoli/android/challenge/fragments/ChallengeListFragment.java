package io.ipoli.android.challenge.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
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
import io.ipoli.android.challenge.activities.ChallengeActivity;
import io.ipoli.android.challenge.activities.EditChallengeActivity;
import io.ipoli.android.challenge.activities.PickChallengeActivity;
import io.ipoli.android.challenge.adapters.ChallengeListAdapter;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.challenge.events.ChallengeCompletedEvent;
import io.ipoli.android.challenge.events.ShowChallengeEvent;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.ui.events.EditChallengeRequestEvent;
import io.ipoli.android.app.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class ChallengeListFragment extends BaseFragment implements OnDataChangedListener<List<Challenge>> {

    private Unbinder unbinder;

    @Inject
    Bus eventBus;

    @BindView(R.id.challenge_list)
    EmptyStateRecyclerView challengeList;

    @BindView(R.id.root_container)
    CoordinatorLayout rootLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab_menu)
    FabMenuView fabMenu;

    @Inject
    ChallengePersistenceService challengePersistenceService;

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

        challengePersistenceService.listenForAll(this);

        fabMenu.addFabClickListener(name -> eventBus.post(new FabMenuTappedEvent(name, EventSource.CHALLENGES)));
        return view;
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_challenges, R.string.help_dialog_challenges_title, "challenges").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.challenge_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_challenge:
                startActivity(new Intent(getContext(), PickChallengeActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        challengePersistenceService.removeAllListeners();
        super.onDestroyView();
    }

    @Override
    public void onDataChanged(List<Challenge> results) {
        ChallengeListAdapter adapter = new ChallengeListAdapter(getActivity(), results, eventBus);
        challengeList.setAdapter(adapter);
    }

    @Subscribe
    public void onEditChallengeRequest(EditChallengeRequestEvent e) {
        Intent i = new Intent(getActivity(), EditChallengeActivity.class);
        i.putExtra(Constants.CHALLENGE_ID_EXTRA_KEY, e.challenge.getId());
        startActivity(i);
    }

    @Subscribe
    public void onShowChallenge(ShowChallengeEvent e) {
        Intent i = new Intent(getActivity(), ChallengeActivity.class);
        i.putExtra(Constants.CHALLENGE_ID_EXTRA_KEY, e.challenge.getId());
        startActivity(i);
    }

    @Subscribe
    public void onChallengeCompleted(ChallengeCompletedEvent e) {
        Snackbar
                .make(rootLayout,
                        getString(R.string.challenge_complete, e.challenge.getName()),
                        Snackbar.LENGTH_LONG).show();
    }
}