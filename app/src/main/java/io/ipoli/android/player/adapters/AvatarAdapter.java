package io.ipoli.android.player.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.ResourceUtils;
import io.ipoli.android.player.events.AvatarSelectedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/13/16.
 */
public class AvatarAdapter extends RecyclerView.Adapter {
    private final List<String> avatars;
    private final Bus eventBus;

    public AvatarAdapter(List<String> avatars, Bus eventBus) {
        this.avatars = avatars;
        this.eventBus = eventBus;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.avatar_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final String avatar = avatars.get(position);
        ImageView avatarView = (ImageView) holder.itemView.findViewById(R.id.avatar_image);
        avatarView.setImageResource(ResourceUtils.extractDrawableResource(holder.itemView.getContext(), avatar));

        holder.itemView.setOnClickListener(view -> eventBus.post(new AvatarSelectedEvent(avatar)));
    }

    @Override
    public int getItemCount() {
        return avatars.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
}
