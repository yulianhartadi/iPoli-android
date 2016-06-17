package io.ipoli.android.reward.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.events.ItemActionsShownEvent;
import io.ipoli.android.reward.data.Reward;
import io.ipoli.android.reward.events.BuyRewardEvent;
import io.ipoli.android.reward.events.DeleteRewardRequestEvent;
import io.ipoli.android.reward.events.EditRewardRequestEvent;
import io.ipoli.android.reward.viewmodels.RewardViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class RewardListAdapter extends RecyclerView.Adapter<RewardListAdapter.ViewHolder> {

    private final List<RewardViewModel> viewModels;
    private final Bus eventBus;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public RewardListAdapter(List<RewardViewModel> viewModels, Bus eventBus) {
        this.viewModels = viewModels;
        this.eventBus = eventBus;
        viewBinderHelper.setOpenOnlyOne(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reward_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RewardViewModel vm = viewModels.get(holder.getAdapterPosition());
        Reward reward = vm.getReward();

        viewBinderHelper.bind(holder.swipeLayout, reward.getId());
        holder.swipeLayout.setSwipeListener(new SwipeRevealLayout.SimpleSwipeListener(){
            @Override
            public void onOpened(SwipeRevealLayout view) {
                super.onOpened(view);
                eventBus.post(new ItemActionsShownEvent(EventSource.REWARDS));
            }
        });

        holder.itemView.setOnClickListener(v ->
                eventBus.post(new EditRewardRequestEvent(reward)));

        holder.delete.setOnClickListener(v ->
                eventBus.post(new DeleteRewardRequestEvent(reward)));

        holder.edit.setOnClickListener(v ->
                eventBus.post(new EditRewardRequestEvent(reward)));

        holder.name.setText(reward.getName());
        holder.buy.setText(String.valueOf(reward.getPrice()));

        if (vm.canBeBought()) {
            holder.buy.setEnabled(true);
            holder.buy.setBackgroundResource(R.color.colorAccent);
            holder.buy.setOnClickListener(v -> eventBus.post(new BuyRewardEvent(reward)));
        } else {
            holder.buy.setEnabled(false);
            holder.buy.setBackgroundResource(R.color.md_grey_500);
            holder.buy.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.reward_name)
        TextView name;

        @BindView(R.id.buy_reward)
        Button buy;

        @BindView(R.id.swipe_layout)
        public SwipeRevealLayout swipeLayout;

        @BindView(R.id.edit)
        public ImageButton edit;

        @BindView(R.id.delete)
        public ImageButton delete;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
