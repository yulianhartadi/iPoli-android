package io.ipoli.android.store.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.ipoli.android.R;
import io.ipoli.android.store.viewmodels.AvatarViewModel;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class AvatarStoreAdapter extends RecyclerView.Adapter<AvatarStoreAdapter.ViewHolder> {
    private final Context context;
    private final Bus eventBus;
    private List<AvatarViewModel> viewModels;

    public AvatarStoreAdapter(Context context, Bus eventBus, List<AvatarViewModel> viewModels) {
        this.context = context;
        this.eventBus = eventBus;
        this.viewModels = viewModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.avatar_store_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AvatarViewModel vm = viewModels.get(holder.getAdapterPosition());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.price.getIconImageObject().getLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL;

        holder.name.setText(vm.getName());
        holder.picture.setImageDrawable(context.getDrawable(vm.getPicture()));
        holder.price.setText(vm.getPrice() + "");

//        holder.price.setOnClickListener(v -> eventBus.post(new BuyPetRequestEvent(vm)));
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void setViewModels(List<AvatarViewModel> avatarViewModels) {
        this.viewModels = avatarViewModels;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.avatar_name)
        TextView name;

        @BindView(R.id.avatar_picture)
        CircleImageView picture;

        @BindView(R.id.avatar_price)
        FancyButton price;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
