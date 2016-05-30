package io.ipoli.android.reward.adapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchHelperAdapter;
import io.ipoli.android.app.ui.ItemTouchHelperViewHolder;
import io.ipoli.android.reward.data.Reward;
import io.ipoli.android.reward.events.BuyRewardEvent;
import io.ipoli.android.reward.events.DeleteRewardRequestEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/27/16.
 */
public class RewardListAdapter extends RecyclerView.Adapter<RewardListAdapter.ViewHolder> implements ItemTouchHelperAdapter {

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
        holder.price.setText(String.valueOf(reward.getPrice()));

        holder.price.setOnClickListener(v -> eventBus.post(new BuyRewardEvent(reward)));
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismissed(int position, int direction) {
        Reward r = rewards.get(position);
        rewards.remove(position);
        notifyItemRemoved(position);
        if (direction == ItemTouchHelper.START) {
            eventBus.post(new DeleteRewardRequestEvent(r, position));
        }
    }

    public void addReward(int position, Reward reward) {
        rewards.add(position, reward);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        @BindView(R.id.reward_name)
        TextView name;

        @BindView(R.id.reward_price)
        TextView price;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }

        @Override
        public void onItemSwipeStart(int direction) {
            if (direction == ItemTouchHelper.START) {
                showDelete();
            }
        }

        private void showDelete() {
            changeDeleteVisibility(View.VISIBLE);
        }

        private void hideScheduleForToday() {
            changeDeleteVisibility(View.GONE);
        }

        private void changeDeleteVisibility(int iconVisibility) {
            itemView.findViewById(R.id.reward_delete_container).setVisibility(iconVisibility);
        }

        @Override
        public void onItemSwipeStopped(int direction) {
            if (direction == ItemTouchHelper.START) {
                hideScheduleForToday();
            }
        }

        @Override
        public boolean isEndSwipeEnabled() {
            return false;
        }

        @Override
        public boolean isStartSwipeEnabled() {
            return true;
        }
    }
}
