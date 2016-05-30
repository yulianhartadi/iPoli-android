package io.ipoli.android.reward.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.ui.DividerItemDecoration;
import io.ipoli.android.reward.activities.AddRewardActivity;
import io.ipoli.android.reward.adapters.RewardListAdapter;
import io.ipoli.android.reward.data.Reward;
import io.ipoli.android.reward.events.BuyRewardEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class RewardListFragment extends Fragment {

    private Unbinder unbinder;

    @Inject
    Bus eventBus;

    private CoordinatorLayout rootContainer;

    @BindView(R.id.reward_list)
    RecyclerView rewardList;


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

        List<Reward> rewards = new ArrayList<>();
        rewards.add(new Reward("Eat a chocolate", 10));
        rewards.add(new Reward("Drink 3 in 1", 30));
        rewards.add(new Reward("Eat ice cream", 20));
        rewards.add(new Reward("Go on vacation", 100));

        RewardListAdapter adapter = new RewardListAdapter(rewards, eventBus);
        rewardList.setAdapter(adapter);
        rewardList.addItemDecoration(new DividerItemDecoration(getContext()));

        return view;
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
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.add_reward)
    public void onAddReward(View view) {
        startActivity(new Intent(getActivity(), AddRewardActivity.class));
    }

    @Subscribe
    public void onBuyReward(BuyRewardEvent e) {
        Reward r = e.reward;
        Snackbar.make(rootContainer, r.getPrice() + " coins spent", Snackbar.LENGTH_SHORT).show();
    }

}
