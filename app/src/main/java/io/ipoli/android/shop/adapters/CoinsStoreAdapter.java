package io.ipoli.android.shop.adapters;

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
import io.ipoli.android.shop.events.BuyCoinsTappedEvent;
import io.ipoli.android.shop.viewmodels.ProductViewModels;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class CoinsStoreAdapter extends RecyclerView.Adapter<CoinsStoreAdapter.ViewHolder> {

    private List<ProductViewModels> viewModels;
    private final Bus eventBus;

    public CoinsStoreAdapter(List<ProductViewModels> viewModels, Bus eventBus) {
        this.viewModels = viewModels;
        this.eventBus = eventBus;
    }

    @Override
    public CoinsStoreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.store_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ProductViewModels vm = viewModels.get(position);

        holder.name.setText(String.valueOf(vm.getValue()));
        holder.buy.setText(vm.getPrice());
        holder.buy.setOnClickListener(view -> eventBus.post(new BuyCoinsTappedEvent(vm.getSku())));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void setViewModels(List<ProductViewModels> viewModels) {
        this.viewModels = viewModels;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.product_name)
        TextView name;

        @BindView(R.id.product_buy)
        Button buy;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}