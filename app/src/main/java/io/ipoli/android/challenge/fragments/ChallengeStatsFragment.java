package io.ipoli.android.challenge.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class ChallengeStatsFragment extends BaseFragment {

    private static final String CHALLENGE_ID = "challenge_id";
    private Unbinder unbinder;

    @Inject
    Bus eventBus;

    private String challengeId;

    public static ChallengeStatsFragment newInstance(String challengeId) {
        ChallengeStatsFragment fragment = new ChallengeStatsFragment();
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
        View view = inflater.inflate(R.layout.fragment_challenge_stats, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

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
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
//        rewardPersistenceService.removeAllListeners();
        super.onDestroyView();
    }
}
