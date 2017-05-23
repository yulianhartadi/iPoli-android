package io.ipoli.android.store.adapters;

import android.support.transition.TransitionManager;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.store.events.BuyUpgradeEvent;
import io.ipoli.android.store.viewmodels.UpgradeViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/23/17.
 */

public class UpgradeStoreAdapter extends RecyclerView.Adapter<UpgradeStoreAdapter.ViewHolder> {
    private final Bus eventBus;
    private final List<UpgradeViewModel> viewModels;

    public UpgradeStoreAdapter(Bus eventBus, List<UpgradeViewModel> viewModels) {
        this.eventBus = eventBus;
        this.viewModels = viewModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.upgrade_store_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UpgradeViewModel vm = viewModels.get(holder.getAdapterPosition());

        holder.title.setText(vm.getTitle());
        holder.shortDesc.setText(vm.getShortDescription());
        holder.longDesc.setText(vm.getLongDescription());
        holder.price.setText(vm.getPrice() + " life coins");
        holder.image.setImageResource(vm.getImage());
        holder.expand.setOnClickListener(v -> {
            if(holder.longDesc.getVisibility() == View.GONE) {
                TransitionManager.beginDelayedTransition(holder.container);
                holder.longDesc.setVisibility(View.VISIBLE);
                holder.expand.setImageResource(R.drawable.ic_expand_less_black_24dp);
            } else {
                TransitionManager.beginDelayedTransition(holder.container);
                holder.longDesc.setVisibility(View.GONE);
                holder.expand.setImageResource(R.drawable.ic_expand_more_black_24dp);
            }
        });

        holder.buy.setOnClickListener(v -> eventBus.post(new BuyUpgradeEvent(vm.getUpgrade())));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.upgrade_container)
        CardView container;

        @BindView(R.id.upgrade_title)
        TextView title;

        @BindView(R.id.upgrade_short_desc)
        TextView shortDesc;

        @BindView(R.id.upgrade_long_desc)
        TextView longDesc;

        @BindView(R.id.upgrade_price)
        TextView price;

        @BindView(R.id.upgrade_image)
        ImageView image;

        @BindView(R.id.upgrade_buy)
        Button buy;

        @BindView(R.id.upgrade_expand)
        ImageButton expand;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
