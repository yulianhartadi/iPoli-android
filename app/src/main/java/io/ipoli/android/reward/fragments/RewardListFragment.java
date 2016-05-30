package io.ipoli.android.reward.fragments;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.reward.activities.AddRewardActivity;
import io.ipoli.android.reward.adapters.RewardListAdapter;
import io.ipoli.android.reward.data.Reward;
import io.ipoli.android.reward.events.BuyRewardEvent;
import io.ipoli.android.reward.events.DeleteRewardRequestEvent;

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

        List<Reward> rewards = new ArrayList<>();
        rewards.add(new Reward("Eat a chocolate", 10));
        rewards.add(new Reward("Drink 3 in 1", 30));
        rewards.add(new Reward("Eat ice cream", 20));
        rewards.add(new Reward("Go on vacation", 100));

        rewardListAdapter = new RewardListAdapter(rewards, eventBus);
        rewardList.setAdapter(rewardListAdapter);
        rewardList.addItemDecoration(new DividerItemDecoration(getContext()));

        int swipeFlags = ItemTouchHelper.START;
        ItemTouchCallback touchCallback = new ItemTouchCallback(rewardListAdapter, 0, swipeFlags);
        touchCallback.setLongPressDragEnabled(false);
        touchCallback.setSwipeStartDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.md_red_500)));
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(rewardList);

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

    @Subscribe
    public void onDeleteRewardRequest(DeleteRewardRequestEvent e) {
        final Snackbar snackbar = Snackbar
                .make(rootContainer,
                        "Reward removed",
                        Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, view -> {
                    rewardListAdapter.addReward(e.position, e.reward);
//                    questPersistenceService.saveRemoteObject(currentDeleteQuestEvent.quest).compose(bindToLifecycle()).subscribe();
//                    eventBus.post(new UndoDeleteQuestEvent(currentDeleteQuestEvent.quest, EventSource.INBOX));
                });

//        questPersistenceService.delete(e.quest).compose(bindToLifecycle()).subscribe(questId -> {
            snackbar.show();
//        });
    }

}
