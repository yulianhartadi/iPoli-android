package io.ipoli.android.store.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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
    private final Context context;
    private List<StoreViewModel> viewModels;
    private final Bus eventBus;

    private int[] colors = new int[]{
            R.color.md_blue_300,
            R.color.md_deep_orange_300,
            R.color.md_green_300,
            R.color.md_indigo_300,
            R.color.md_purple_300,
            R.color.md_red_300,
            R.color.md_orange_300,
            R.color.md_pink_300,
    };

    public StoreAdapter(Context context, Bus eventBus, List<StoreViewModel> viewModels) {
        this.context = context;
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
        holder.image.setImageResource(vm.getPicture());
        holder.container.setOnClickListener(v -> eventBus.post(new StoreItemSelectedEvent(vm.getType())));
        holder.rootContainer.setBackgroundColor(ContextCompat.getColor(context, colors[position % colors.length]));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
        @BindView(R.id.root_container)
        ViewGroup rootContainer;

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
