package io.ipoli.android.store.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.formatters.DateFormatter;
import io.ipoli.android.store.events.BuyPowerUpEvent;
import io.ipoli.android.store.viewmodels.PowerUpViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class PowerUpStoreAdapter extends EnterAnimationAdapter<PowerUpStoreAdapter.ViewHolder> {
    private final Context context;
    private final Bus eventBus;
    private List<PowerUpViewModel> viewModels;
    private final Set<Integer> enabledPowerUps;

    private int[] colors = new int[]{
            R.color.md_green_300,
            R.color.md_indigo_300,
            R.color.md_red_300,
            R.color.md_blue_300,
            R.color.md_deep_orange_300,
            R.color.md_purple_300,
            R.color.md_orange_300,
            R.color.md_pink_300,
    };

    public PowerUpStoreAdapter(Context context, Bus eventBus, List<PowerUpViewModel> viewModels, Set<Integer> enabledPowerUps) {
        this.context = context;
        this.eventBus = eventBus;
        this.viewModels = viewModels;
        this.enabledPowerUps = enabledPowerUps;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.power_up_store_item, parent, false));
    }

    @Override
    protected void doOnBindViewHolder(ViewHolder holder, int position) {
        PowerUpViewModel vm = viewModels.get(holder.getAdapterPosition());

        holder.title.setText(vm.getTitle());
        holder.shortDesc.setText(vm.getShortDescription());
        holder.longDesc.setText(vm.getLongDescription());
        holder.price.setText(context.getString(R.string.power_up_price, vm.getPrice()));
        holder.image.setImageResource(vm.getImage());
        holder.expand.setOnClickListener(v -> {
            int duration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
            if (holder.longDesc.getVisibility() == View.GONE) {
                TransitionManager.beginDelayedTransition(holder.container);
                holder.expand.animate().rotationBy(180).setDuration(duration);
                holder.longDesc.setAlpha(1.0f);
                holder.longDesc.setVisibility(View.VISIBLE);
            } else {
                holder.expand.animate().rotationBy(-180).setDuration(duration);
                holder.longDesc.animate().alpha(0.0f).setDuration(duration).withEndAction(() ->
                        holder.longDesc.setVisibility(View.GONE));
            }
        });

        if (vm.isEnabled()) {
            holder.enable.setVisibility(View.INVISIBLE);
            holder.expirationDate.setVisibility(View.VISIBLE);
            holder.expirationDate.setText(context.getString(R.string.power_up_expires_on, DateFormatter.format(context, vm.getExpirationDate())));
        } else if (vm.requiresUpgrade() && !enabledPowerUps.contains(vm.getRequiredUpgrade().code)) {
            holder.enable.setVisibility(View.INVISIBLE);
            holder.expirationDate.setVisibility(View.VISIBLE);
            String requiredTitle = context.getString(vm.getRequiredUpgrade().title);
            holder.expirationDate.setText(context.getString(R.string.requires_power_up_message, requiredTitle));
        } else {
            holder.enable.setVisibility(View.VISIBLE);
            holder.expirationDate.setVisibility(View.GONE);
        }

        holder.enable.setOnClickListener(v -> eventBus.post(new BuyPowerUpEvent(vm.getPowerUp())));

        GradientDrawable drawable = (GradientDrawable) holder.imageContainer.getBackground();
        drawable.setColor(ContextCompat.getColor(context, colors[position % colors.length]));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void setViewModels(List<PowerUpViewModel> upgradeViewModels) {
        viewModels = upgradeViewModels;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.power_up_container)
        CardView container;

        @BindView(R.id.power_up_title)
        TextView title;

        @BindView(R.id.power_up_short_desc)
        TextView shortDesc;

        @BindView(R.id.power_up_long_desc)
        TextView longDesc;

        @BindView(R.id.power_up_price)
        TextView price;

        @BindView(R.id.power_up_valid_until_date)
        TextView expirationDate;

        @BindView(R.id.power_up_image)
        ImageView image;

        @BindView(R.id.power_up_image_background)
        ImageView imageContainer;

        @BindView(R.id.power_up_enable)
        Button enable;

        @BindView(R.id.power_up_expand)
        ImageButton expand;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
