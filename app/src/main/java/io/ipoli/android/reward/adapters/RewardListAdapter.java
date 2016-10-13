package io.ipoli.android.reward.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    public RewardListAdapter(List<RewardViewModel> viewModels, Bus eventBus) {
        this.viewModels = viewModels;
        this.eventBus = eventBus;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reward_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RewardViewModel vm = viewModels.get(holder.getAdapterPosition());
        Reward reward = vm.getReward();

        holder.contentLayout.setOnClickListener(v ->
                eventBus.post(new EditRewardRequestEvent(reward)));

        holder.moreMenu.setOnClickListener(v -> {
            eventBus.post(new ItemActionsShownEvent(EventSource.REWARDS));
            Context context = holder.itemView.getContext();
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.reward_actions_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.edit_reward:
                        eventBus.post(new EditRewardRequestEvent(reward));
                        return true;
                    case R.id.delete_reward:
                        eventBus.post(new DeleteRewardRequestEvent(reward));
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });

        holder.name.setText(reward.getName());
        if (TextUtils.isEmpty(reward.getDescription())) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setText(reward.getDescription());
            holder.description.setVisibility(View.VISIBLE);
        }
        holder.buy.setText(String.valueOf(reward.getPrice()));

        holder.buy.setEnabled(true);
        holder.buy.setOnClickListener(v -> eventBus.post(new BuyRewardEvent(reward)));
        if (vm.canBeBought()) {
            holder.buy.setBackgroundResource(R.color.colorAccent);
        } else {
            holder.buy.setBackgroundResource(R.color.md_grey_500);
        }
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.content_layout)
        public RelativeLayout contentLayout;

        @BindView(R.id.reward_name)
        TextView name;

        @BindView(R.id.reward_description)
        TextView description;

        @BindView(R.id.buy_reward)
        Button buy;

        @BindView(R.id.reward_more_menu)
        public ImageButton moreMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
