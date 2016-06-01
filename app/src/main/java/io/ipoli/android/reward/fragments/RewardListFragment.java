package io.ipoli.android.reward.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.ui.DividerItemDecoration;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.persistence.RewardPersistenceService;
import io.ipoli.android.reward.activities.RewardActivity;
import io.ipoli.android.reward.adapters.RewardListAdapter;
import io.ipoli.android.reward.data.Reward;
import io.ipoli.android.reward.events.BuyRewardEvent;
import io.ipoli.android.reward.events.EditRewardRequestEvent;
import io.ipoli.android.reward.viewmodels.RewardViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class RewardListFragment extends BaseFragment {

    private Unbinder unbinder;

    @Inject
    Bus eventBus;

    @Inject
    RewardPersistenceService rewardPersistenceService;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    private CoordinatorLayout rootContainer;

    @BindView(R.id.reward_list)
    RecyclerView rewardList;

    private RewardListAdapter rewardListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reward_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        rootContainer = (CoordinatorLayout) getActivity().findViewById(R.id.root_container);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rewardList.setLayoutManager(layoutManager);

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
        updateRewards();
    }

    private void updateRewards() {
        playerPersistenceService.find().compose(bindToLifecycle()).subscribe(player -> {
            int coins = player.getCoins();
            rewardPersistenceService.findAll().compose(bindToLifecycle()).subscribe(rewards -> {
                List<RewardViewModel> rewardViewModels = new ArrayList<>();
                for(Reward r : rewards) {
                    rewardViewModels.add(new RewardViewModel(r, (r.getPrice() <= coins)));
                }

                rewardListAdapter = new RewardListAdapter(rewardViewModels, eventBus);
                rewardList.setAdapter(rewardListAdapter);
                rewardList.addItemDecoration(new DividerItemDecoration(getContext()));
            });
        });
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.add_reward)
    public void onAddReward(View view) {
        startActivity(new Intent(getActivity(), RewardActivity.class));
    }

    @Subscribe
    public void onBuyReward(BuyRewardEvent e) {
        playerPersistenceService.find().compose(bindToLifecycle()).subscribe(player -> {
            Reward r = e.reward;
            if(player.getCoins() - r.getPrice() < 0) {
                showTooExpensiveMessage();
                return;
            }
            player.setCoins(player.getCoins() - r.getPrice());
            playerPersistenceService.saveRemoteObject(player).compose(bindToLifecycle()).subscribe(p -> {
                updateRewards();
                Snackbar.make(rootContainer, r.getPrice() + " coins spent", Snackbar.LENGTH_SHORT).show();
            });
        });
    }

    private void showTooExpensiveMessage() {
        Toast.makeText(getContext(), R.string.reward_too_expensive, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEditRewardRequest(EditRewardRequestEvent e) {
        Intent i = new Intent(getContext(), RewardActivity.class);
        i.putExtra(Constants.REWARD_ID_EXTRA_KEY, e.reward.getId());
        startActivity(i);
    }

    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        updateRewards();
    }
}
