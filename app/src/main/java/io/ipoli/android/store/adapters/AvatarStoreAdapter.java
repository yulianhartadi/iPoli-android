package io.ipoli.android.store.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
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
import io.ipoli.android.store.events.BuyAvatarRequestEvent;
import io.ipoli.android.store.events.UseAvatarEvent;
import io.ipoli.android.store.viewmodels.AvatarViewModel;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/25/17.
 */

public class AvatarStoreAdapter extends EnterAnimationAdapter<AvatarStoreAdapter.ViewHolder>{
    private final Context context;
    private final Bus eventBus;
    private List<AvatarViewModel> viewModels;

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
    public int getItemCount() {
        return viewModels.size();
    }

    public void setViewModels(List<AvatarViewModel> avatarViewModels) {
        this.viewModels = avatarViewModels;
        notifyDataSetChanged();
    }

    @Override
    protected void doOnBindViewHolder(ViewHolder holder, int position) {
        AvatarViewModel vm = viewModels.get(holder.getAdapterPosition());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.price.getIconImageObject().getLayoutParams();
        params.gravity = Gravity.CENTER_VERTICAL;

        holder.name.setText(vm.getName());
        holder.picture.setImageDrawable(context.getDrawable(vm.getPicture()));
        holder.price.setText(vm.getPrice() + "");

        if (vm.isBought()) {
            holder.price.setText(context.getString(R.string.avatar_store_use_avatar).toUpperCase());
            holder.price.setIconResource((Drawable) null);
            holder.price.setOnClickListener(v -> eventBus.post(new UseAvatarEvent(vm.getAvatar())));
        } else {
            holder.price.setIconResource(context.getDrawable(R.drawable.ic_life_coin_white_24dp));
            holder.price.setText(String.valueOf(vm.getPrice()));
            holder.price.setOnClickListener(v -> eventBus.post(new BuyAvatarRequestEvent(vm.getAvatar())));
        }

        holder.container.setBackgroundColor(ContextCompat.getColor(context, colors[position % colors.length]));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.avatar_container)
        ViewGroup container;

        @BindView(R.id.avatar_name)
        TextView name;

        @BindView(R.id.avatar_picture)
        ImageView picture;

        @BindView(R.id.avatar_price)
        FancyButton price;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
