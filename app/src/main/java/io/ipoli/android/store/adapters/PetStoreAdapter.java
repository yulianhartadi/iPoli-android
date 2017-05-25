package io.ipoli.android.store.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.store.events.BuyPetRequestEvent;
import io.ipoli.android.store.viewmodels.PetViewModel;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class PetStoreAdapter extends RecyclerView.Adapter<PetStoreAdapter.ViewHolder> {
    private final Context context;
    private final Bus eventBus;
    private List<PetViewModel> petViewModels;

    public PetStoreAdapter(Context context, Bus eventBus, List<PetViewModel> petViewModels) {
        this.context = context;
        this.eventBus = eventBus;
        this.petViewModels = petViewModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pet_store_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PetViewModel vm = petViewModels.get(holder.getAdapterPosition());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.price.getIconImageObject().getLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL;

        holder.name.setText(vm.getDescription());
        holder.picture.setImageDrawable(context.getDrawable(vm.getPicture()));
        holder.pictureState.setImageDrawable(context.getDrawable(vm.getPictureState()));
        holder.price.setText(vm.getPrice() + "");

        holder.price.setOnClickListener(v -> eventBus.post(new BuyPetRequestEvent(vm)));
    }

    @Override
    public int getItemCount() {
        return petViewModels.size();
    }

    public void setViewModels(List<PetViewModel> petViewModels) {
        this.petViewModels = petViewModels;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.pet_name)
        TextView name;

        @BindView(R.id.pet_picture)
        ImageView picture;

        @BindView(R.id.pet_picture_state)
        ImageView pictureState;

        @BindView(R.id.pet_price)
        FancyButton price;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
