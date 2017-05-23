package io.ipoli.android.store.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.store.events.StoreItemSelectedEvent;
import io.ipoli.android.store.viewmodels.StoreViewModel;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
    private List<StoreViewModel> viewModels;
    private final Bus eventBus;

    public StoreAdapter(Bus eventBus, List<StoreViewModel> viewModels) {
        this.viewModels = viewModels;
        this.eventBus = eventBus;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.store_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StoreViewModel vm = viewModels.get(holder.getAdapterPosition());

        holder.title.setText(vm.getTitle());
        holder.image.setImageResource(vm.getImage());
        holder.container.setOnClickListener(v -> eventBus.post(new StoreItemSelectedEvent(vm.getType())));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{

        @BindView(R.id.sore_item_container)
        ViewGroup container;

        @BindView(R.id.store_item_title)
        TextView title;

        @BindView(R.id.store_item_image)
        ImageView image;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
