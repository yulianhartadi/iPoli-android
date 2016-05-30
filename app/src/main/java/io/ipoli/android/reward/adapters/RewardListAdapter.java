package io.ipoli.android.reward.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.reward.data.Reward;
import io.ipoli.android.reward.events.BuyRewardEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class RewardListAdapter extends RecyclerView.Adapter<RewardListAdapter.ViewHolder> {

    private final List<Reward> rewards;
    private final Bus eventBus;

    public RewardListAdapter(List<Reward> rewards, Bus eventBus) {
        this.rewards = rewards;
        this.eventBus = eventBus;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reward_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Reward reward = rewards.get(holder.getAdapterPosition());

        holder.name.setText(reward.getName());
        holder.buy.setText(String.valueOf(reward.getPrice()));

        holder.buy.setOnClickListener(v -> eventBus.post(new BuyRewardEvent(reward)));
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    public void addReward(int position, Reward reward) {
        rewards.add(position, reward);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.reward_name)
        TextView name;

        @BindView(R.id.buy_reward)
        Button buy;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
