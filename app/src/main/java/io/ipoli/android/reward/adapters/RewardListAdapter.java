package io.ipoli.android.reward.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.formatters.DateFormatter;
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

    private final Context context;
    private final List<RewardViewModel> viewModels;
    private final Bus eventBus;

    private int[] colors = new int[]{
            R.color.md_green_300,
            R.color.md_indigo_300,
            R.color.md_blue_300,
            R.color.md_red_300,
            R.color.md_deep_orange_300,
            R.color.md_purple_300,
            R.color.md_orange_300,
            R.color.md_pink_300,
    };

    public RewardListAdapter(Context context, List<RewardViewModel> viewModels, Bus eventBus) {
        this.context = context;
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

//            popupMenu.inflate(R.menu.reward_actions_menu);


        holder.name.setText(reward.getName());
        if (TextUtils.isEmpty(reward.getDescription())) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setText(reward.getDescription());
            holder.description.setVisibility(View.VISIBLE);
        }

        holder.lastPurchase.setText("Last used: " + DateFormatter.formatWithoutYear(context, reward.getLastPurchaseDate()));
        holder.purchaseCount.setText(reward.getPurchaseCount() + " used");

        holder.price.setText(reward.getPrice() + " points");

        holder.rootContainer.setOnClickListener(v -> eventBus.post(new EditRewardRequestEvent(reward)));
        holder.edit.setOnClickListener(v -> eventBus.post(new EditRewardRequestEvent(reward)));
        holder.delete.setOnClickListener(v -> eventBus.post(new DeleteRewardRequestEvent(reward)));

        holder.buy.setEnabled(true);
        holder.buy.setOnClickListener(v -> eventBus.post(new BuyRewardEvent(reward)));
        if (vm.canBeBought()) {
            holder.buy.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            holder.buy.setTextColor(ContextCompat.getColor(context, R.color.md_grey_500));
        }

        GradientDrawable drawable = (GradientDrawable) holder.pictureBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(context, colors[position % colors.length]));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.reward_root_layout)
        ViewGroup rootContainer;

        @BindView(R.id.reward_name)
        TextView name;

        @BindView(R.id.reward_desc)
        TextView description;

        @BindView(R.id.reward_price)
        TextView price;

        @BindView(R.id.reward_last_purchase)
        TextView lastPurchase;

        @BindView(R.id.reward_purchase_count)
        TextView purchaseCount;

        @BindView(R.id.reward_buy)
        Button buy;

        @BindView(R.id.reward_edit)
        ImageButton edit;

        @BindView(R.id.reward_delete)
        ImageButton delete;

        @BindView(R.id.reward_picture_background)
        ImageView pictureBackground;

        @BindView(R.id.reward_picture)
        ImageView picture;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
